/*
	See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
	for the licence for this software.
	
	(c) Copyright 2011 Epimorphics Limited
	$Id$
*/
package com.epimorphics.lda.core;

import java.util.*;

import com.epimorphics.lda.core.View.State;
import com.epimorphics.lda.core.property.ViewProperty;
import com.epimorphics.lda.rdfq.*;
import com.epimorphics.lda.support.PrefixLogger;
import com.epimorphics.lda.support.PropertyChain;

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
		Map<ViewProperty, List<PropertyChain>> them = new HashMap<>();
		for (PropertyChain chain: chains) {
			List<ViewProperty> properties = chain.getProperties();
			if (properties.size() > 0) {
				ViewProperty key = properties.get(0);
				PropertyChain tail = chain.tail();
				List<PropertyChain> entries = them.get(key);
				if (entries == null) them.put( key, entries = new ArrayList<>() );
				entries.add(tail);
			}
		}

		ChainTrees result = new ChainTrees();
		for (Map.Entry<ViewProperty, List<PropertyChain>> entry: them.entrySet()) {
			Variable nv = st.vars.newVar();
			RDFQ.Triple triple = entry.getKey().asTriple(r, nv, st.vars);
			ChainTrees followers = make( nv, st, entry.getValue() );
			result.add( new ChainTree( triple, followers ) );
		}
		return result;
	}
}