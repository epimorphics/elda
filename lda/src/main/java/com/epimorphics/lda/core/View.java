/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$

    File:        Template.java
    Created by:  Dave Reynolds
    Created on:  4 Feb 2010
*/

package com.epimorphics.lda.core;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.core.View.ChainTree;
import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.query.ExpansionPoints;
import com.epimorphics.lda.rdfq.Any;
import com.epimorphics.lda.rdfq.RDFQ;
import com.epimorphics.lda.rdfq.URINode;
import com.epimorphics.lda.rdfq.Variable;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.sources.Source;
import com.epimorphics.lda.support.PrefixLogger;
import com.epimorphics.lda.support.PropertyChain;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Represents a view which selects which parts of a result 
 * resource to show, defined by a (possibly) ordered list of properties.
 * 
 * @author <a href="mailto:der@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public class View {
	
    static Logger log = LoggerFactory.getLogger(View.class);

    protected final List<PropertyChain> chains = new ArrayList<PropertyChain>();

    public static final String SHOW_ALL = "all";
    
    public static final String SHOW_BASIC = "basic";

    public static final String SHOW_DESCRIPTION = "description";
    
    public static final String SHOW_DEFAULT_INTERNAL = "default";
    
    protected static final List<PropertyChain> emptyChain = new ArrayList<PropertyChain>();

    /**
        View that does DESCRIBE plus labels of all objects.
    */
    public static final View ALL = new View( false, SHOW_ALL, Type.T_ALL );

	/**
	    Property chains: [RDF.type] and [RDFS.label].
	*/
	static final List<PropertyChain> BasicChains = 
		Arrays.asList( new PropertyChain( RDF.type ), new PropertyChain( RDFS.label ) );
    
	/**
        View that does rdf:type and rdfs:label.
    */
    // public static final View BASIC = new View( false, SHOW_BASIC, Type.T_BASIC );
    public static final View BASIC = new View( false, SHOW_BASIC, Type.T_CHAINS, BasicChains );
    
    /**
        View that does DESCRIBE.
    */
    public static final View DESCRIBE = new View(false, SHOW_DESCRIPTION, Type.T_DESCRIBE );
    
    private static Map<Resource, View> builtins = new HashMap<Resource, View>();
    
    static {
    	builtins.put( API.basicViewer, BASIC );
    	builtins.put( API.describeViewer, DESCRIBE );
    	builtins.put( API.labelledDescribeViewer, ALL );
    }
    
    /**
        Answer the built-in view with the given URI, or null if there
        isn't one.
    */
    public static View getBuiltin( Resource r ) {
    	return builtins.get(r);
    }
    
	protected boolean doesFiltering = true;
	
	public static enum Type { T_DESCRIBE, T_ALL, T_CHAINS };
	
	protected Type type = Type.T_DESCRIBE;
    
	protected String name = null;
	
    public View() {
    	this(true);
    }
    
    public View( Type type ) {
    	this( true, null, type );
    }
    
    public View( String name ) {
    	this( false, name, Type.T_CHAINS );
    }
    
    public View( boolean doesFiltering ) {
    	this( doesFiltering, null, Type.T_DESCRIBE );
    }
    
    public View( boolean doesFiltering, String name, Type type ) {
    	this( doesFiltering, name, type, emptyChain );
    }
    
    public View( boolean doesFiltering, String name, Type type, List<PropertyChain> initial ) {
    	this.type = type;
    	this.name = name;
    	this.doesFiltering = doesFiltering;
    	this.chains.addAll( initial );
    }
    
    @Override public boolean equals( Object other ) {
    	return other instanceof View && same( (View) other );
    }
    
    private boolean same(View other) {
		return this.type == other.type && this.chains.equals( other.chains );
	}

	public String name(){
    	return name;
    }
    
    public Type getType() {
    	return type;
    }
    
    public Set<PropertyChain> chains() {
    	return new HashSet<PropertyChain>( chains );
    }
    
    /**
        Answer true iff this view does filtering, ie, is not ALL.
    */
    public boolean doesFiltering() { return doesFiltering; }
    
    /**
        Answer this view if it is ALL, otherwise a new view that
        does the same filtering and is mutable without affecting the
        original.
    */
    public View copy() {
    	View r = new View( doesFiltering, null, type ).addFrom( this );
    	// System.err.println( ">> copying " + this + " => " + r );
		return r;
    }
    
    /**
        Answer this view after modifying it to contain all the property
        chains defined by <code>spec</code>.
    */
    public View addViewFromRDFList(Resource spec, ShortnameService sns) {
    	cannotUpdateALL();		
    	doesFiltering = true;
        if (spec.canAs(RDFList.class)) {
        	List<Property> properties = new ArrayList<Property>();
            RDFList list = spec.as(RDFList.class);
            for (Iterator<RDFNode> i = list.iterator(); i.hasNext();) {
                properties.add( i.next().as( Property.class ) ); 
            }
            chains.add( new PropertyChain( properties ) );
        } else {
            String uri = spec.asResource().getURI();
            chains.add( new PropertyChain( uri ) );
        }
        if (chains.size() > 0) type = Type.T_CHAINS;
        return this;
    }

	private void cannotUpdateALL() {
		if (this == ALL) throw new IllegalArgumentException( "the view ALL cannot be updated." );
	}
    
    /**
        Answer this view after updating it with the given property string.
        The string may end in ".*", in which case the corresponding URI is
        marked as an expansion point. The property name may be dotted; it
        defines a property chain.
    */
    public View addViewFromParameterValue(String prop, ExpansionPoints ep, ShortnameService sns) {
    	cannotUpdateALL();		
    	doesFiltering = true;
    	boolean expansion = false;
        if (prop.endsWith(".*")) {
            prop = prop.substring(0, prop.length()-2);
            expansion = true;
        }
        // System.err.println( ">> aTFPV: prop = " + prop );
        List<Property> chain = ShortnameService.Util.expandProperties( prop, sns );
        // System.err.println( ">> aTFPV: chain = " + chain );
        chains.add( new PropertyChain( chain ) );
        String uri = sns.expand(prop);
        if (uri == null) {
        	if (expansion) throw new QueryParseException("Can't expand view property: " + prop);
        	expansion = false;
        }
        if (expansion) ep.addExpansion(uri);
        // if (chains.size() > 0) type = Type.T_CHAINS;
        return this;
    }
    
    /**
        Answer this view after updating it by adding all the property chains
        of the argument view, which must not be null.
    */
    public View addFrom( View t ) {
    	if (t == null) throw new IllegalArgumentException( "addFrom does not accept null views" );
    	cannotUpdateALL();
    	chains.addAll( t.chains );
    	if (chains.size() > 0) doesFiltering = true;
    	if (chains.size() > 0) type = Type.T_CHAINS;
        return this;
    }
    
    /**
        Answer a string describing this view. It will show the list of
        property chains, possibly in an abbreviated form.
    */
    @Override public String toString() {
    	return 
    		(doesFiltering ? "only " : "ALL." )
    		+ " " + type + " "
    		+ chains.toString().replaceAll( ",", ",\n  " )
    		;    	
    }

    /**
        Filter the given model to contain only the subgraph which consists
        of the triples starting with the root nodes and extending down this
        views property chains. Answer the new restricted model. If this
        view is ALL, answers the given model with no filtering. 
    */
	public Model applyTo( Model source, List<Resource> roots ) {
        Model result = doesFiltering() && false
        	? ChainScanner.onlyMatchingChains( source, roots, chains ) // TODO this may well be dead dead dead
        	: source
        	;
        return result;
	}

	public static class State {
		
		final String select;
		final List<Resource> roots;
		final Model m; 
		final List<Source> sources;
		final VarSupply vars;
		
		public State( String select, List<Resource> roots, Model m, List<Source> sources, VarSupply vars ) {
			this.select = select;
			this.roots = roots;
			this.m = m; 
			this.sources = sources;
			this.vars = vars;
		}
	}
	
	public String fetchDescriptions( State s ) {
//		log.info( "fetchDescriptionsFor: sources = " + sources + " using " + this );
		long zero = System.currentTimeMillis();
		switch (type) {
			case T_DESCRIBE: {
				String detailsQuery = fetchByGivenPropertyChains( s, chains ); 
				return fetchBareDescriptions( s ); 
			}
				
			case T_ALL:	{	
				String detailsQuery = fetchBareDescriptions( s ); 				
				String chainsQuery = fetchByGivenPropertyChains( s, chains ); 
			    addAllObjectLabels( s );
			    return detailsQuery;
			}

			case T_CHAINS:	{
				String detailsQuery = fetchByGivenPropertyChains( s, chains ); 
				log.debug( "T_CHAINS took " + ((System.currentTimeMillis() - zero)/1000.0) + "s" );
				return detailsQuery;
			}
				
			default:
				EldaException.Broken( "unknown view type " + type );				
		}
		return "# should be a query here.";
	}
	
	private String fetchByGivenPropertyChains( State s, List<PropertyChain> chains ) { 
		if (chains.isEmpty()) 
			return "CONSTRUCT {} WHERE {}\n";
		boolean uns = useNestedSelect(s) && s.select.length() > 0;
		return uns && false
			? fetchChainsByNestedSelect( s, chains ) 
			: fetchChainsByRepeatedClauses( s, chains )
			;
	}

	private boolean useNestedSelect( State st ) {
		return Source.Util.allSupportNestedSelect( st.sources );
	}
	
	boolean oldWay = false;
	
	private String fetchChainsByRepeatedClauses( State s, List<PropertyChain> chains ) { 
		if (oldWay) {
			StringBuilder construct = new StringBuilder();
			PrefixLogger pl = new PrefixLogger( s.m );
			construct.append( "CONSTRUCT {" );
			List<Variable> varsInOrder = new ArrayList<Variable>();		
			for (Resource r: new HashSet<Resource>( s.roots)) {
				for (PropertyChain c: chains) {
					construct.append( "\n  " );
					buildConstructClause( pl, construct, RDFQ.uri( r.getURI() ), c, s.vars, varsInOrder );
				}
			}
		//
			construct.append( "\n} WHERE {" );
			String union = "";
			for (Resource r: new HashSet<Resource>( s.roots)) {
				for (PropertyChain c: chains) {
					construct.append( "\n  " ).append( union );
					buildWhereClause( pl, construct, RDFQ.uri( r.getURI() ), c, s.vars, varsInOrder );
					union = "UNION ";
				}
			}
			construct.append( "\n}" );
		//
			String prefixes = pl.writePrefixes( new StringBuilder() ).toString();
			String queryString = prefixes + construct.toString();
			// System.err.println( ">> Query string [old-style] is " + queryString );
			Query constructQuery = QueryFactory.create( queryString );
			for (Source x: s.sources) s.m.add( x.executeConstruct( constructQuery ) );
			return queryString;
		} else {
			// the new way
			StringBuilder construct = new StringBuilder();
			PrefixLogger pl = new PrefixLogger( s.m );
			construct.append( "CONSTRUCT {\n" );
		//
			StringBuilder sb = new StringBuilder();
			String union = "";
			
			for (Resource r: new HashSet<Resource>( s.roots)) {
				sb.append( union ).append( " { " );
//				sharingPropertyChains( sb, pl, RDFQ.uri( r.getURI() ), s, chains );
				ChainTrees chainys = makeChainTrees( RDFQ.uri( r.getURI() ), s, chains );
				String u = renderChainy( sb, pl, "", chainys );
				sb.append( "}" );
				union = "UNION";
			}
			
			ChainTrees chainTrees = new ChainTrees();
			for (Resource r: new HashSet<Resource>( s.roots )) {
				chainTrees.addAll( makeChainTrees( RDFQ.uri( r.getURI() ), s, chains ) );
			}
			
			
			String template = sb.toString().replaceAll( "OPTIONAL \\{ \\}", "" ).replaceAll( "\\}", "}\n" );
		//
			String cons = template.replaceAll( "UNION|OPTIONAL|[{}]", "" );
//			System.err.println( ">> cons: " + cons );
			// construct.append( cons );
			chainTrees.renderTriples( construct, pl );
		//
			construct.append( "\n} WHERE {\n" );
			// construct.append( template );
			renderChainy( construct, pl, "", chainTrees );
			construct.append( "\n}" );
		//
			String prefixes = pl.writePrefixes( new StringBuilder() ).toString();
			String queryString = prefixes + construct.toString();
//			System.err.println( ">> Query string is [new-style]" + queryString );
			Query constructQuery = QueryFactory.create( queryString );
			for (Source x: s.sources) s.m.add( x.executeConstruct( constructQuery ) );
			return queryString;
		}
	}

	static final Pattern SELECT = Pattern.compile( "SELECT", Pattern.CASE_INSENSITIVE );
	
	private String fetchChainsByNestedSelect( State st, List<PropertyChain> chains ) { 
		if (oldWay) {
			PrefixLogger pl = new PrefixLogger( st.m );
			StringBuilder construct = new StringBuilder();
		//
			Matcher m = SELECT.matcher( st.select );
			if (!m.find()) EldaException.Broken( "No SELECT in nested query." );
			int s = m.start();
			String selection = st.select.substring( s );
			String selectPrefixes = st.select.substring(0, s);
		//
			final Any r = RDFQ.var( "?item" );
			construct.append( "CONSTRUCT {" );		
			List<Variable> varsInOrder = new ArrayList<Variable>();
			for (PropertyChain c: chains) {
				construct.append( "\n  " );
				buildConstructClause( pl, construct, r, c, st.vars, varsInOrder );
			}
		//
			construct.append( "\n} WHERE {\n" );
			construct.append( "  {" ).append( selection.replaceAll( "\n", "\n    " ) ).append( "\n}" );
		//
			String union = "";
			for (PropertyChain c: chains) {
				construct.append( "\n  " ).append( union );
				buildWhereClause( pl, construct, r, c, st.vars, varsInOrder );
				union = "UNION ";
			}
			construct.append( "\n}" );
		//
			String prefixes = pl.writePrefixes( new StringBuilder() ).toString();
			String queryString = selectPrefixes + prefixes + construct.toString();
			// System.err.println( ">> QUERY:\n" + queryString );
			Query constructQuery = QueryFactory.create( queryString );
			for (Source x: st.sources) st.m.add( x.executeConstruct( constructQuery ) );
			return queryString;
		} else {
			PrefixLogger pl = new PrefixLogger( st.m );
			StringBuilder construct = new StringBuilder();
		//
			Matcher m = SELECT.matcher( st.select );
			if (!m.find()) EldaException.Broken( "No SELECT in nested query." );
			int s = m.start();
			String selection = st.select.substring( s );
			String selectPrefixes = st.select.substring(0, s);
			ChainTrees trees = makeChainTrees( RDFQ.var( "?item" ), st, chains );
		//
			construct.append( "CONSTRUCT {" );
			trees.renderTriples( construct, pl );
			construct.append( "\n} WHERE {\n" );			
			construct.append( "  {" ).append( selection.replaceAll( "\n", "\n    " ) ).append( "\n}" );
			renderChainy( construct, pl, "", trees );			
			construct.append( "\n}" );
		//
			String prefixes = pl.writePrefixes( new StringBuilder() ).toString();
			String queryString = selectPrefixes + prefixes + construct.toString();
			// System.err.println( ">> QUERY:\n" + queryString );
			try { Query constructQuery = QueryFactory.create( queryString );
			for (Source x: st.sources) st.m.add( x.executeConstruct( constructQuery ) ); }
			catch (RuntimeException e) {
				System.err.println( ">> OOPS: " + queryString );
				throw e;
			}
			return queryString;
		}
	}
	
	@SuppressWarnings("serial") static class ChainTrees extends ArrayList<ChainTree> {
		
		public void renderTriples( StringBuilder sb, PrefixLogger pl ) {
			for (ChainTree c: this) c.renderTriples( sb, pl );
		}
	}
	
	static class ChainTree {
		
		protected final RDFQ.Triple triple;
		protected final ChainTrees followers;
		
		ChainTree( RDFQ.Triple triple, ChainTrees followers ) {
			this.triple = triple;
			this.followers = followers;
		}
		
		public void renderTriples( StringBuilder sb, PrefixLogger pl ) {
			sb.append( triple.asSparqlTriple(pl) ).append( " .\n" );
			followers.renderTriples( sb, pl );
		}
	}
	
	private String renderChainy( StringBuilder sb, PrefixLogger pl, String u, ChainTrees chains ) {
		for (ChainTree c: chains) {
			sb.append( u );
			renderChainy( sb, pl, 0, c );
			u = "UNION ";
		}
		return u;
	}

	private void renderChainy( StringBuilder sb, PrefixLogger pl, int depth, ChainTree c ) {
		boolean isComplex = c.followers.size() > 0;
		for (int i = 0; i < depth; i += 1) sb.append( "  " );
		if (isComplex) sb.append( "{" );
		sb.append( "{ " ).append( c.triple.asSparqlTriple(pl) ).append( " . } " );
		if (isComplex) {
			sb.append( " OPTIONAL {\n" );
			for (ChainTree cc: c.followers) {
				renderChainy( sb, pl, depth + 1, cc );
			}
			for (int i = 0; i < depth; i += 1) sb.append( "  " );
			sb.append( "}" );
		}
		if (isComplex) sb.append( "}" );
		sb.append( "\n" );
	}

	private ChainTrees makeChainTrees( Any r, State st, List<PropertyChain> chains ) {
		Map<Property, List<PropertyChain>> them = new HashMap<Property, List<PropertyChain>>();
		//
			for (PropertyChain chain: chains) {
				List<Property> properties = chain.getProperties();
				if (properties.size() > 0) {
					Property key = properties.get(0);
					PropertyChain rest = tail(chain);
					List<PropertyChain> entries = them.get(key);
					if (entries == null) them.put( key, entries = new ArrayList<PropertyChain>() );
					entries.add( rest );
				}
			}		
		ChainTrees result = new ChainTrees();
		for (Map.Entry<Property, List<PropertyChain>> entry: them.entrySet()) {
			Variable nv = st.vars.newVar();
			RDFQ.Triple triple = RDFQ.triple( r, RDFQ.uri( entry.getKey().getURI() ), nv );
			ChainTrees followers = makeChainTrees( nv, st, entry.getValue() );
			result.add( new ChainTree( triple, followers ) );
		}
		return result;
	}

//	private void sharingPropertyChains( StringBuilder sb, PrefixLogger pl, Any r, State st, List<PropertyChain> chains ) {
//		Map<Property, List<PropertyChain>> them = new HashMap<Property, List<PropertyChain>>();
//	//
//		for (PropertyChain chain: chains) {
//			List<Property> properties = chain.getProperties();
//			if (properties.size() > 0) {
//				Property key = properties.get(0);
//				PropertyChain rest = tail(chain);
//				List<PropertyChain> entries = them.get(key);
//				if (entries == null) them.put( key, entries = new ArrayList<PropertyChain>() );
//				entries.add( rest );
//			}
//		}
//	//
//		String union = "";
//		for (Map.Entry<Property, List<PropertyChain>> entry: them.entrySet()) {
//			Variable nv = st.vars.newVar();
//			String property = RDFQ.uri( entry.getKey().getURI() ).asSparqlTerm( pl );
//			sb.append( union ).append( "{" );
//			union = " UNION ";
//			sb.append( r.asSparqlTerm( pl ) ).append( " " ).append( property ).append( " " ).append( nv.asSparqlTerm( pl ) );
//			sb.append( " . OPTIONAL {" );
//			sharingPropertyChains( sb, pl, nv, st, entry.getValue() );
//			sb.append( " } }" );
//		}
//	}

	private PropertyChain tail(PropertyChain chain) {
		List<Property> properties = chain.getProperties();
		return new PropertyChain( properties.subList( 1, properties.size() ) );
	}

	private void buildConstructClause( PrefixLogger pl, StringBuilder construct, Any r, PropertyChain c, VarSupply vs, List<Variable> varsInOrder ) {
		String S = pl.present( r );
		for (Property p: c.getProperties()) {
			Variable v = vs.newVar();
			varsInOrder.add( v );
			String V = v.asSparqlTerm( pl );
			construct.append( S );
			construct.append( " " ).append( pl.present( p.getURI() ) );
			construct.append( " " ).append( V );
			S = " . " + V;
		}
		construct.append( " ." );
	}
	
	private void buildWhereClause( PrefixLogger pl, StringBuilder construct, Any r, PropertyChain c, VarSupply vs, List<Variable> varsInOrder ) {
		String S = pl.present( r );
		construct.append( "{" );
		for (Property p: c.getProperties()) {
			String V = next( varsInOrder) .asSparqlTerm( pl );
			construct.append( S );
			construct.append( " " ).append( pl.present( p.getURI() ) );
			construct.append( " " ).append( V );
			S = " OPTIONAL { " + V;
		}
		for (int i = 0; i < c.getProperties().size(); i += 1) construct.append( "}" );
	}

	private Variable next(List<Variable> varsInOrder) {
		return varsInOrder.remove(0);
	}

	private String fetchBareDescriptions( State s ) { 
		long zero = System.currentTimeMillis();
		String describe = fetchUntimedByDescribe( s.m, s.roots, s.sources );
		long time = System.currentTimeMillis() - zero;
		log.debug( "fetchBareDescriptions took " + (time/1000.0) + "s" );
		return describe;
	}

	private String fetchUntimedByDescribe( Model sm, List<Resource> allRoots, List<Source> sources ) {
		List<List<Resource>> chunks = chunkify( allRoots );
		StringBuilder describe = new StringBuilder();
		for (List<Resource> roots: chunks) {
			PrefixLogger pl = new PrefixLogger( sm );
			describe.setLength(0);
			describe.append( "DESCRIBE" );
			for (Resource r: new HashSet<Resource>( roots )) { // TODO
				describe.append( "\n  " ).append( pl.present( r.getURI() ) );
			}
			String query = pl.writePrefixes( new StringBuilder() ).toString() + describe;
			Query describeQuery = QueryFactory.create( query );
			for (Source x: sources) sm.add( x.executeDescribe( describeQuery ) );
		}
		return describe.toString();
	}		
	
	static final int CHUNK_SIZE = 1000;
	static final boolean slice_describes = false;
	
	private List<List<Resource>> chunkify(List<Resource> roots) {
		List<List<Resource>> result = new ArrayList<List<Resource>>();
		if (slice_describes) {
			int size = roots.size();
			for (int i = 0; i < size; i += CHUNK_SIZE) {
				int lim = Math.min( size, i + CHUNK_SIZE );
				result.add( roots.subList( i, lim ) );
			}
			if (result.size() > 1)
				log.debug( "large DESCRIBE: " + result.size() + " chunks of size " + CHUNK_SIZE );
		} else {
			result.add( roots );
		}
		return result;
	}

	private void addAllObjectLabels( State s ) { 
		long zero = System.currentTimeMillis();
		StringBuilder sb = new StringBuilder();
		sb.append( "PREFIX rdfs: <" ).append( RDFS.getURI() ).append(">\nCONSTRUCT { ?x rdfs:label ?l }\nWHERE\n{" );
		String union = "";
		for (RDFNode n: s.m.listObjects().toList()) {
			if (n.isURIResource()) {
				sb.append( union )
					.append( "{?x rdfs:label ?l. FILTER(?x = <" )
					.append( n.asNode().getURI() )
					.append( ">)" + "}" );
				union = "\nUNION ";
			}
		}
		sb.append( "}\n" );
		Query constructQuery = QueryFactory.create( sb.toString() );
		long midTime = System.currentTimeMillis();
		for (Source x: s.sources) s.m.add( x.executeConstruct( constructQuery ) );
		long aTime = midTime - zero, bTime = System.currentTimeMillis() - midTime; 
		log.debug( "addAllObjectLabels took " + (aTime/1000.0) + "s making, " + (bTime/1000.0) + "s fetching." );
	}
}

