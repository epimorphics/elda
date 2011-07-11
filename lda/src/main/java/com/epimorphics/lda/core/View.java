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

import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.query.ExpansionPoints;
import com.epimorphics.lda.rdfq.Any;
import com.epimorphics.lda.rdfq.RDFQ;
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

    /**
        View that does DESCRIBE plus labels of all objects.
    */
    public static final View ALL = new View( false, SHOW_ALL, Type.T_ALL );
    
    /**
        View that does rdf:type and rdfs:label.
    */
    public static final View BASIC = new View( false, SHOW_BASIC, Type.T_BASIC );
    
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
	
	static enum Type { T_DESCRIBE, T_ALL, T_CHAINS, T_BASIC };
	
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
    	this.type = type;
    	this.name = name;
    	this.doesFiltering = doesFiltering;
    }
    
    public String name(){
    	return name;
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
            RDFList list = spec.as(RDFList.class);
            for (Iterator<RDFNode> i = list.iterator(); i.hasNext();) {
                String uri = sns.normalizeResource( i.next() ).getURI();
                chains.add( new PropertyChain( uri ) );
            }
        } else {
            String uri = sns.normalizeResource(spec).getURI();
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
        if (chains.size() > 0) type = Type.T_CHAINS;
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
//		System.err.println( ">> applyTo: view = " + this );
        Model result = doesFiltering() && false
        	? ChainScanner.onlyMatchingChains( source, roots, chains ) // TODO this may well be dead dead dead
        	: source
        	;
//        result.write( System.err, "Turtle" );
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
		switch (type) {
			case T_DESCRIBE: 
				return fetchBareDescriptions( s ); 
				
			case T_ALL:	{	
				String detailsQuery = fetchBareDescriptions( s ); 
			    addAllObjectLabels( s ); // m, sources );
			    return detailsQuery;
			}

			case T_CHAINS:	{
				long zero = System.currentTimeMillis();
				String detailsQuery = fetchByGivenPropertyChains( s, chains ); 
				long time = System.currentTimeMillis() - zero;
				log.debug( "T_CHAINS took " + (time/1000.0) + "s" );
				return detailsQuery;
			}
				
			case T_BASIC: {
				long zero = System.currentTimeMillis();
				String detailsQuery = fetchByGivenPropertyChains( s, BasicChains ); 
				long time = System.currentTimeMillis() - zero;
				log.debug( "T_BASIC took " + (time/1000.0) + "s" );
				return detailsQuery;
			}
				
			default:
				EldaException.Broken( "unknown view type " + type );				
		}
		return "# should be a query here.";
	}

	/**
	    Property chains: [RDF.type] and [RDFS.label].
	*/
	static final List<PropertyChain> BasicChains = 
		Arrays.asList( new PropertyChain( RDF.type ), new PropertyChain( RDFS.label ) );
	
	private String fetchByGivenPropertyChains( State s, List<PropertyChain> chains ) { 
		boolean uns = useNestedSelect(s) && s.select.length() > 0;
		log.info(uns ? "chains: using nested selects" : "chains: using repeated clauses");
		// System.err.println( ">> useNestedSelect: " + useNestedSelect(s) );
		// System.err.println( ">> uns: " + uns + " (select = '" + s.select + "')" );
		return uns
			? fetchChainsByNestedSelect( s, chains ) 
			: fetchChainsByRepeatedClauses( s, chains )
			;
	}

	private boolean useNestedSelect( State st ) {
		if (allSupportNestedSelect( st.sources )) return true;
		return false;
	}

	private boolean allSupportNestedSelect( List<Source> sources ) {
		for (Source s: sources) if (!s.supportsNestedSelect()) return false;
		return true;
	}

	private String fetchChainsByRepeatedClauses( State s, List<PropertyChain> chains ) { 
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
		Query constructQuery = QueryFactory.create( queryString );
		for (Source x: s.sources) s.m.add( x.executeConstruct( constructQuery ) );
		return queryString;
	}
	
	private String fetchChainsByNestedSelect( State st, List<PropertyChain> chains ) { 
		PrefixLogger pl = new PrefixLogger( st.m );
		StringBuilder construct = new StringBuilder();
		int s = st.select.indexOf( "\nSELECT" );
		String selection = st.select.substring( s + 1 );
		String selectPrefixes = st.select.substring(0, s + 1);
	//
		Any r = RDFQ.var( "?item" );
		construct.append( "CONSTRUCT {" );		
		List<Variable> varsInOrder = new ArrayList<Variable>();
		for (PropertyChain c: chains) {
			construct.append( "\n  " );
			buildConstructClause( pl, construct, r, c, st.vars, varsInOrder );
		}
	//
		construct.append( "\n} WHERE {" );
		String hack_selection = hackVars( selection );
		// System.err.println( ">> nested select: hacking round ARQ bug: " + hack_selection );
		construct.append( "{" ).append( hack_selection ).append( "}" );
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
	}

	private String hackVars( String selection ) {
		if (true) return selection;
		// System.err.println( ">> SELECTION: " + selection + "\nEND\n" );
		String vars = varsOf( selection.substring( selection.indexOf( "item" ) + 5 ) );
		return selection.replaceFirst( "\n", "" + vars + "\n" );
	}

	private String varsOf( String selection ) {
		String result = "";
		Pattern p = Pattern.compile( "\\?[A-Za-z_0-9]+" );
		Matcher m = p.matcher( selection );
		while (m.find()) {
			String var = m.group();
			if (!var.equals( "?item" ) && !result.contains( var ))
				result += " " + var;
			
		}
			
		return result;
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
		PrefixLogger pl = new PrefixLogger( s.m );
		String describe = "DESCRIBE";
		for (Resource r: new HashSet<Resource>( s.roots )) { // TODO
			describe += "\n  " + pl.present( r.getURI() );
		}
		describe = pl.writePrefixes( new StringBuilder() ).toString() + describe;
		Query describeQuery = QueryFactory.create( describe );
		for (Source x: s.sources) s.m.add( x.executeDescribe( describeQuery ) );
		long time = System.currentTimeMillis() - zero;
		log.debug( "fetchBareDescriptions took " + (time/1000.0) + "s" );
		return describe;
	}		
	
	private void addAllObjectLabels( State s ) { 
		long zero = System.currentTimeMillis();
		String construct = "PREFIX rdfs: <" + RDFS.getURI() + ">\nCONSTRUCT { ?x rdfs:label ?l }\nWHERE\n{";
		String union = "";
		for (RDFNode n: s.m.listObjects().toList()) {
			if (n.isURIResource()) {
				construct += union + "{?x rdfs:label ?l. FILTER(?x = <" + n.asNode().getURI() + ">)" + "}";
				union = "\nUNION ";
			}
		}
		construct += "}\n";
		Query constructQuery = QueryFactory.create( construct );
		for (Source x: s.sources) s.m.add( x.executeConstruct( constructQuery ) );
		long time = System.currentTimeMillis() - zero;
		log.debug( "addAllObjectLabels took " + (time/1000.0) + "s" );
	}
}

