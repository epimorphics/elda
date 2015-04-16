/*
	See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
	for the licence for this software.
	
	(c) Copyright 2011 Epimorphics Limited
	$Id$
*/
package com.epimorphics.lda.core;

import java.util.*;

import com.epimorphics.lda.core.View.State;
import com.epimorphics.lda.rdfq.*;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.support.PrefixLogger;
import com.epimorphics.lda.support.PropertyChain;
import com.hp.hpl.jena.rdf.model.Property;

/**
    A collection of property chains represented as a triple (?item predicate ?value)
    and all the different tails of the chain, the followers, which are ChainTrees
    (qv).
*/

public class ChainTree {
	
	protected final RDFQ.Triple triple;
	protected final ChainTrees followers;

	/**
	    Initialise this ChainTree with its triple and followers.
	*/
	ChainTree( RDFQ.Triple triple, ChainTrees followers ) {
		this.triple = triple;
		this.followers = followers;
	}
	
	/**
	    Render all (and only) the triples in this ChainTree as SPARQL
	    triples.
	*/
	public void renderTriples( StringBuilder sb, PrefixLogger pl ) {
		sb.append( triple.asSparqlTriple(pl) ).append( " .\n" );
		followers.renderTriples( sb, pl );
	}

	/**
	    Render this ChainTree into <code>sb</code> as SPARQL constructs suitable 
	    for use in the WHERE clause of a SPARQL query.
	*/
	public void renderWhere( StringBuilder sb, PrefixLogger pl ) {
		renderWhere( sb, pl, "", 0 );
	}
	
	/**
	    Render this ChainTree into sb. Try to retain readability by taking
	    account of the depth along the property chain. Prefix the 'union' string
	    onto each of the clauses, and pass UNION as that string to subordinate
	    chain walks.
	*/
	private void renderWhere( StringBuilder sb, PrefixLogger pl, String union, int depth ) {
		boolean isComplex = followers.size() > 0;
		for (int i = 0; i < depth; i += 1) sb.append( "  " );
		sb.append( union );
		if (isComplex) sb.append( "{" );
		sb.append( "{ " ).append( triple.asSparqlTriple(pl) ).append( " . } " );
		if (isComplex) {
			int index = 0;
			sb.append( " OPTIONAL {\n" );
			for (ChainTree cc: followers) {
				cc.renderWhere( sb, pl, (index ++ == 0 ? "" : "UNION "), depth + 1 );
			}
			for (int i = 0; i < depth; i += 1) sb.append( "  " );
			sb.append( "}" );
		}
		if (isComplex) sb.append( "}" );
		sb.append( "\n" );
	}

	/**
	    Convert a list of property chains to the chain tree representation.
	    Convert wildcard predicates (originally '*' in the property chain string,
	    and as a magic property in the PropertyChain objects) to a fresh variable.
	*/
	public static ChainTrees make( Any r, State st, List<PropertyChain> chains ) {
		Map<Property, List<PropertyChain>> them = new HashMap<Property, List<PropertyChain>>();
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
	//
		ChainTrees result = new ChainTrees();
		for (Map.Entry<Property, List<PropertyChain>> entry: them.entrySet()) {
			Variable nv = st.vars.newVar();
			RDFQ.Triple triple = RDFQ.triple( r, predicate( st, entry.getKey() ), nv );
			ChainTrees followers = make( nv, st, entry.getValue() );
			result.add( new ChainTree( triple, followers ) );
		}
		return result;
	}

	private static Any predicate( State st, Property p ) {
		return p.equals( ShortnameService.Util.propertySTAR ) ? st.vars.newVar() : RDFQ.uri( p.getURI() );
	}

	private static PropertyChain tail(PropertyChain chain) {
		List<Property> properties = chain.getProperties();
		return new PropertyChain( properties.subList( 1, properties.size() ) );
	}
}