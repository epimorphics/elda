/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.shortnames;

import com.epimorphics.lda.core.MultiMap;
import com.epimorphics.vocabs.API;
import com.epimorphics.vocabs.FIXUP;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
    Another class maintaining shortnames, this one avoids
    commitments made in Context.
    
    @author chris
*/
public class NameMap {

	private static final Resource ANY = null;
	
	private final MultiMap<String, String> map = new MultiMap<String, String>();
	
	private final PrefixMapping prefixes = PrefixMapping.Factory.create();

	public void load( PrefixMapping pm, Model m ) {
		prefixes.withDefaultMappings( pm );
		load( pm, m.listStatements( ANY, RDFS.label, ANY ) );
		load( pm, m.listStatements( ANY, FIXUP.label, ANY ) );
		load( pm, m.listStatements( ANY, API.name, ANY ) );
	}

	private void load( PrefixMapping pm, StmtIterator si ) {
		while (si.hasNext()) load( pm, si.next() );
	}

	private void load(PrefixMapping pm, Statement s) {
		Resource r = s.getSubject();
		String name = asString( s.getObject().asNode() );
		fullNameToShortName( r.getURI(), name );
	}

	private void fullNameToShortName(String uri, String name) {
		map.add( uri, name );
	}

	private String asString( Node n ) {
		return n.isLiteral() ? n.getLiteralLexicalForm() : n.getURI();
	}
}