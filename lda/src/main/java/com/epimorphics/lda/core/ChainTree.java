package com.epimorphics.lda.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.epimorphics.lda.core.View.State;
import com.epimorphics.lda.rdfq.Any;
import com.epimorphics.lda.rdfq.RDFQ;
import com.epimorphics.lda.rdfq.Variable;
import com.epimorphics.lda.support.PrefixLogger;
import com.epimorphics.lda.support.PropertyChain;
import com.hp.hpl.jena.rdf.model.Property;

/*
	See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
	for the licence for this software.
	
	(c) Copyright 2011 Epimorphics Limited
	$Id$
*/

public class ChainTree {
	
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

	public void renderWhere( StringBuilder sb, PrefixLogger pl ) {
		renderWhere( sb, pl, "", 0 );
	}
	
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
			RDFQ.Triple triple = RDFQ.triple( r, RDFQ.uri( entry.getKey().getURI() ), nv );
			ChainTrees followers = make( nv, st, entry.getValue() );
			result.add( new ChainTree( triple, followers ) );
		}
		return result;
	}

	private static PropertyChain tail(PropertyChain chain) {
		List<Property> properties = chain.getProperties();
		return new PropertyChain( properties.subList( 1, properties.size() ) );
	}
}