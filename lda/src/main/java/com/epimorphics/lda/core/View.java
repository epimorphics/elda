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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.rdfq.Variable;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.sources.Source;
import com.epimorphics.lda.support.PropertyChain;
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
    public static final View ALL = new View( false, Type.T_ALL );
    
    /**
        View that does rdf:type and rdfs:label.
    */
    public static final View BASIC = new View( false, Type.T_BASIC );
    
    /**
        View that does DESCRIBE.
    */
    public static final View DESCRIBE = new View(false, Type.T_DESCRIBE );

	protected boolean doesFiltering = true;
	
	static enum Type { T_DESCRIBE, T_ALL, T_CHAINS, T_BASIC };
	
	protected Type type = Type.T_DESCRIBE;
    
    public View() {
    	this(true);
    }
    
    public View( Type type ) {
    	this( true, type );
    }
    
    public View( boolean doesFiltering ) {
    	this( doesFiltering, Type.T_DESCRIBE );
    }
    
    public View( boolean doesFiltering, Type type ) {
    	this.type = type;
    	this.doesFiltering = doesFiltering;
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
    	View r = new View( doesFiltering, type ).addFrom( this );
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

	public void fetchDescriptions( Model m, List<Resource> roots, List<Source> sources, VarSupply vars ) {
//		log.info( "fetchDescriptionsFor: sources = " + sources + " using " + this );
		switch (type) {
			case T_DESCRIBE: 
				fetchBareDescriptions( m, roots, sources );
				break;
				
			case T_ALL:			
				fetchBareDescriptions( m, roots, sources );
			    addAllObjectLabels( m, sources );
			    break;

			case T_CHAINS:	{
				long zero = System.currentTimeMillis();
				fetchByGivenPropertyChains( m, roots, sources, vars, chains );
				long time = System.currentTimeMillis() - zero;
				log.debug( "T_CHAINS took " + (time/1000.0) + "s" );
				break;
			}
				
			case T_BASIC: {
				long zero = System.currentTimeMillis();
				fetchByGivenPropertyChains( m, roots, sources, vars, BasicChains );
				long time = System.currentTimeMillis() - zero;
				log.debug( "T_BASIC took " + (time/1000.0) + "s" );
				break;
			}
				
			default:
				EldaException.Broken( "unknown view type " + type );				
		}
	}

	/**
	    Property chains: [RDF.type] and [RDFS.label].
	*/
	static final List<PropertyChain> BasicChains = 
		Arrays.asList( new PropertyChain( RDF.type ), new PropertyChain( RDFS.label ) );

	private void fetchByGivenPropertyChains(Model m, List<Resource> roots, List<Source> sources, VarSupply vars, List<PropertyChain> chains) {
		StringBuilder construct = new StringBuilder();
		construct.append( "CONSTRUCT {" );
		List<Variable> varsInOrder = new ArrayList<Variable>();
		for (Resource r: new HashSet<Resource>( roots)) {
			for (PropertyChain c: chains) {
				construct.append( "\n  " );
				buildConstructClause( construct, r, c, vars, varsInOrder );
			}
		}
	//
		construct.append( "\n} WHERE {" );
		String union = "";
		for (Resource r: new HashSet<Resource>( roots)) {
			for (PropertyChain c: chains) {
				construct.append( "\n  " ).append( union );
				buildWhereClause( construct, r, c, vars, varsInOrder );
				union = "UNION ";
			}
		}
		construct.append( "\n}" );
	//
		Query constructQuery = QueryFactory.create( construct.toString() );
		for (Source x: sources) m.add( x.executeConstruct( constructQuery ) );
	}
	
	private void buildConstructClause( StringBuilder construct, Resource r, PropertyChain c, VarSupply vs, List<Variable> varsInOrder ) {
		String S = "<" + r.getURI() + ">";
		for (Property p: c.getProperties()) {
			Variable v = vs.newVar();
			varsInOrder.add( v );
			String V = v.asSparqlTerm();
			construct.append( S );
			construct.append( " " ).append( "<" ).append( p.getURI() ).append( ">" );
			construct.append( " " ).append( V );
			S = " . " + V;
		}
		construct.append( " ." );
	}
	
	private void buildWhereClause( StringBuilder construct, Resource r, PropertyChain c, VarSupply vs, List<Variable> varsInOrder ) {
		String S = "<" + r.getURI() + ">";
		construct.append( "{" );
		for (Property p: c.getProperties()) {
			String V = next( varsInOrder) .asSparqlTerm();
			construct.append( S );
			construct.append( " " ).append( "<" ).append( p.getURI() ).append( ">" );
			construct.append( " " ).append( V );
			S = " OPTIONAL { " + V;
		}
		for (int i = 0; i < c.getProperties().size(); i += 1) construct.append( "}" );
	}

	private Variable next(List<Variable> varsInOrder) {
		return varsInOrder.remove(0);
	}

	private void fetchBareDescriptions( Model m, List<Resource> roots, List<Source> sources ) {
		long zero = System.currentTimeMillis();
		String describe = "DESCRIBE";
		for (Resource r: new HashSet<Resource>( roots )) { // TODO
			describe += " <" + r.getURI() + ">";
		}
		Query describeQuery = QueryFactory.create( describe );
		for (Source x: sources) m.add( x.executeDescribe( describeQuery ) );
		long time = System.currentTimeMillis() - zero;
		log.debug( "fetchBareDescriptions took " + (time/1000.0) + "s" );
	}		
	
	private void addAllObjectLabels( Model m, List<Source> sources ) {
		long zero = System.currentTimeMillis();
		String construct = "PREFIX rdfs: <" + RDFS.getURI() + ">\nCONSTRUCT { ?x rdfs:label ?l }\nWHERE\n{";
		String union = "";
		for (RDFNode n: m.listObjects().toList()) {
			if (n.isURIResource()) {
				construct += union + "{?x rdfs:label ?l. FILTER(?x = <" + n.asNode().getURI() + ">)" + "}";
				union = "\nUNION ";
			}
		}
		construct += "}\n";
		Query constructQuery = QueryFactory.create( construct );
		for (Source x: sources) m.add( x.executeConstruct( constructQuery ) );
		long time = System.currentTimeMillis() - zero;
		log.debug( "addAllObjectLabels took " + (time/1000.0) + "s" );
	}
}

