/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$

    File:        Template.java
    Created by:  Dave Reynolds
    Created on:  4 Feb 2010
*/

package com.epimorphics.lda.core;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.rdfq.RDFQ;
import com.epimorphics.lda.rdfq.SparqlSupport;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.sources.Source;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.specs.PropertyExpiryTimes;
import com.epimorphics.lda.support.*;
import com.epimorphics.lda.vocabularies.API;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PrefixMapping;
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
    public static final View ALL = new View( SHOW_ALL, Type.T_ALL );

	/**
	    Property chains: [RDF.type] and [RDFS.label].
	*/
	static final List<PropertyChain> BasicChains = 
		Arrays.asList( new PropertyChain( RDF.type ), new PropertyChain( RDFS.label ) );
    
	/**
        View that does rdf:type and rdfs:label.
    */
    // public static final View BASIC = new View( false, SHOW_BASIC, Type.T_BASIC );
    public static final View BASIC = new View( SHOW_BASIC, Type.T_CHAINS, BasicChains );
    
    /**
        View that does DESCRIBE.
    */
    public static final View DESCRIBE = new View( SHOW_DESCRIPTION, Type.T_DESCRIBE );
    
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
	
	public static enum Type { T_DESCRIBE, T_ALL, T_CHAINS, T_TEMPLATE };
	
	protected Type type = Type.T_DESCRIBE;
    
	protected String name = null;
	
	protected String template = null;
	
	protected String labelPropertyURI = RDFS.label.getURI();
	
    public View() {
    	this( "anon", Type.T_DESCRIBE );
    }
    
    public View( String name ) {
    	this( name, Type.T_CHAINS );
    }
    
    public View( String name, Type type ) {
    	this( name, type, emptyChain );
    }
    
    public View( String name, Type type, List<PropertyChain> initial ) {
    	this.type = type;
    	this.name = name;
    	this.chains.addAll( initial );
    }

	public static View newTemplateView( String viewName, String template ) {
		View result = new View( viewName, Type.T_TEMPLATE );
		result.template = template;
		return result;
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

	public String nameWithoutCopy() {
		int dotCopy = name.indexOf(".copy");
		return dotCopy < 0 ? name : name.substring(0, dotCopy);
	}
    
    public Type getType() {
    	return type;
    }
    
    public Set<PropertyChain> chains() {
    	return new HashSet<PropertyChain>( chains );
    }
    
    /**
        Answer this view if it is ALL, otherwise a new view that
        does the same filtering and is mutable without affecting the
        original.
    */
    public View copy() {
    	View r = new View( this.name + ".copy", type ).addFrom( this );
		return r;
    }

	public boolean isTemplateView() {
		return type == Type.T_TEMPLATE;
	}

	public String getTemplate() {
		return template;
	}
	
    /**
        Set the describe label used by this viewer. The viewer type
        becomes ALL.
    */
	public void setDescribeLabel( String labelPropertyURI ) {
		this.type = Type.T_ALL;
		this.labelPropertyURI = labelPropertyURI;
	}
    
    /**
        Answer this view after modifying it to contain all the property
        chains defined by <code>spec</code>.
    */
    public View addViewFromRDFList(Resource spec, ShortnameService sns) {
    	cannotUpdateALL();		
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
        The property name may be dotted; it defines a property chain.
    */
    public View addViewFromParameterValue( String prop, ShortnameService sns ) {
    	cannotUpdateALL();
        List<Property> chain = ShortnameService.Util.expandProperties( prop, sns );
        chains.add( new PropertyChain( chain ) );
        return this;
    }
    
    /**
        Answer this view after updating it by adding all the property chains
        of the argument view (which must not be null).
    */
    public View addFrom( View t ) {
    	if (t == null) throw new IllegalArgumentException( "addFrom does not accept null views" );
    	cannotUpdateALL();
    	chains.addAll( t.chains );
    	this.labelPropertyURI = t.labelPropertyURI;
    	if (chains.size() > 0) type = Type.T_CHAINS;
    	if (t.type == Type.T_ALL) type = Type.T_ALL;
    	template = t.template;
        return this;
    }
    
    /**
        Answer a string describing this view. It will show the list of
        property chains, possibly in an abbreviated form.
    */
    @Override public String toString() {
    	return type + " " + chains.toString().replaceAll( ",", ",\n  " );    	
    }
    
	public static class State {
		
//		final String select;
		final List<Resource> roots;
		final Model m; 
		final List<Source> sources;
		final VarSupply vars;
		final String graphName;
		
		public State
			( // String select
			 List<Resource> roots
			, Model m
			, List<Source> sources
			, VarSupply vars
			, String graphName
			) {
//			this.select = select;
			this.roots = roots;
			this.m = m; 
			this.sources = sources;
			this.vars = vars;
			this.graphName = graphName;
		}
		
		void beginGraph(StringBuilder sb) {
			if (graphName != null) sb.append(" GRAPH <" + graphName + "> {");
		}
		
		void endGraph(StringBuilder sb) {
			if (graphName != null) sb.append("}");
		}
	}
	
	public String fetchDescriptions( Controls c, State s ) {
		Times t = c.times;
		switch (type) {
			case T_DESCRIBE: {
				String detailsQuery = fetchByGivenPropertyChains( s, chains ); 
				String describeQuery = describeBySelectedItems( s, s.roots );
				t.addToViewQuerySize( detailsQuery );
				t.addToViewQuerySize( describeQuery );
				return describeQuery; 
			}
				
			case T_ALL:	{	
				String detailsQuery = describeBySelectedItems( s, s.roots ); 				
				String chainsQuery = fetchByGivenPropertyChains( s, chains ); 
			    addAllObjectLabels( c, s );
				t.addToViewQuerySize( detailsQuery );
				t.addToViewQuerySize( chainsQuery );
			    return detailsQuery;
			}

			case T_CHAINS:	{
				String detailsQuery = fetchByGivenPropertyChains( s, chains ); 
				t.addToViewQuerySize( detailsQuery );
				return detailsQuery;
			}
				
			default:
				EldaException.Broken( "unknown view type " + type );				
		}
		return "# should be a query here.";
	}
	
	private String fetchByGivenPropertyChains( State st, List<PropertyChain> chains ) { 
		PrefixLogger pl = new PrefixLogger( st.m );
		StringBuilder construct = new StringBuilder();
	//
		ChainTrees trees = ChainTree.make( RDFQ.var( "?item" ), st, chains );		
	//
		construct.append( "CONSTRUCT {" );
		trees.renderTriples( construct, pl );
		construct.append( "\n} WHERE {\n" );	
		construct.append( "  { VALUES ?item { " );
	//
		for (Resource r: new HashSet<Resource>( st.roots )) 
			construct.append( "\n  " ).append( pl.present( r.getURI() ) );
	//
		construct.append( "\n} }\n" );
		st.beginGraph(construct);
		trees.renderWhere( construct, pl, "" );			
		st.endGraph(construct);
		construct.append( "\n}" );
	//	
//		System.err.println("\n>> QUERY:\n" + construct + "\n");
	//
		String prefixes = pl.writePrefixes( new StringBuilder() ).toString();
		String queryString = prefixes + construct.toString();
		// System.err.println( ">> QUERY:\n" + queryString );
		Query constructQuery = QueryFactory.create( queryString );
		for (Source x: st.sources) st.m.add( x.executeConstruct( constructQuery ) ); 
		return queryString;
	}		
	
	private String describeBySelectedItems(State s, List<Resource> allRoots) {
		String query = createDescribeQueryForItems( s, allRoots );
		Query describeQuery = QueryFactory.create( query );
		for (Source x: s.sources) s.m.add( x.executeDescribe( describeQuery ) );
		return query.toString();
	}

	public static String createDescribeQueryForItems( State s, List<Resource> allRoots ) {
		PrefixMapping pm = s.m;
		StringBuilder describe = new StringBuilder();
		PrefixLogger pl = new PrefixLogger( pm );
		describe.append( "DESCRIBE " );
		for (Resource r: new HashSet<Resource>( allRoots )) 
			describe.append( "\n  " ).append( pl.present( r.getURI() ) );
		String query = pl.writePrefixes( new StringBuilder() ).toString() + describe;
		return query;
	}	

	private void addAllObjectLabels( Controls c, State s ) { 
		StringBuilder sb = new StringBuilder();
		sb
			.append( "PREFIX rdfs: <" ).append( RDFS.getURI() ).append(">" )
			.append( "\nCONSTRUCT { ?x <" ).append( labelPropertyURI ).append( "> ?l }\nWHERE\n{" );
		s.beginGraph(sb);
	//	
		sb.append( "  { VALUES ?x { " );
	//
		for (RDFNode n: s.m.listObjects().toList()) 
			if (n.isURIResource())
				sb.append( "\n  " ).append("<").append( n.asNode().getURI() ).append(">");
	//
		sb.append( "\n} }\n" );
		sb.append( "?x <" ).append( labelPropertyURI ).append( "> ?l. " );
	//
		s.endGraph(sb);
		sb.append( "}\n" );
		String queryString = sb.toString();
	//
		if (log.isDebugEnabled()) log.debug("LABEL QUERY:\n" + queryString + "\n");
	//	
		Query constructQuery = QueryFactory.create( queryString );
		for (Source x: s.sources) s.m.add( x.executeConstruct( constructQuery ) );
	}	

	public String fetchDescriptionsFor
		( Controls c
		, List<Resource> roots
		, Model m
		, APISpec spec
		, VarSupply vars
		, String graphName
		) {        
		List<Source> sources = spec.getDescribeSources();
		return this.isTemplateView()
        	? this.viewByTemplate( roots, m, spec, sources, graphName )
        	: this.fetchDescriptions( c, new View.State( roots, m, sources, vars, graphName ) );
	}
	
	public String viewByTemplate(List<Resource> roots, Model m, APISpec spec, List<Source> sources, String graphName) {
		String viewTemplate = getTemplate();
		int estimatedSize = viewTemplate.length() * 2 + 30 + estimateRootsSize( roots );
		StringBuilder query = new StringBuilder( estimatedSize );
		SparqlSupport.appendPrefixes( query, spec.getPrefixMap() );
		query
			.append( "CONSTRUCT {\n" )
				.append( viewTemplate )
				.append( "} where {\n" )
				.append(graphName == null ? "" : "GRAPH <" + graphName + "> { ")
				.append( "{ " ).append( viewTemplate ).append( " }\n" )
				.append(graphName == null ? "" : " }")
				.append( SparqlSupport.itemsAsFilter( roots ) )
				.append( "}\n" )
				;
		String resultQueryString = query.toString();
		Query q = QueryFactory.create( resultQueryString );
		for (Source x: sources) m.add( x.executeConstruct( q ) );		
		return resultQueryString;
	}

	private int estimateRootsSize(List<Resource> roots) {
		int result = 0;
		for (Resource r: roots) result += r.getURI().length() + 14;
		return result + 10;
	}

	/**
	    minExpiryTime(pet, dur) returns the minimum of the expiry times for
	    predicates in this view. A predicate of propertySTAR, representing *,
	    forces the result to use the smallest expiry time of any predicate.
	    The result is the smaller of the givenDuration and the minimum expiry
	    time.
	*/
	public long minExpiryMillis(PropertyExpiryTimes pet, long givenDuration) {
		long viewDuration = propertyBasedMillis(pet);
		
//		System.err.println( ">> view: " + toString() );
//		System.err.println( "]]   View.minExpiryTime: viewDuration = " + viewDuration );		
		
		if (viewDuration == Long.MAX_VALUE) return givenDuration;
		if (givenDuration < 0) return viewDuration;
		return Math.min(viewDuration, givenDuration);
	}

	private long propertyBasedMillis(PropertyExpiryTimes pet) {
		
		long result = Long.MAX_VALUE;
		
		if (type.equals(Type.T_DESCRIBE) || type.equals(Type.T_ALL))
			return pet.minTimeMillis();
		
		for (PropertyChain pc: chains) {
			for (Property p: pc.getProperties()) {
				if (p.equals( ShortnameService.Util.propertySTAR )) {
					return pet.minTimeMillis();
				} else {
					long millis = pet.timeInMillisFor(p);
					if (millis < result) result = millis;
				}
			}
		}
		
		return result;
	}
	
}

