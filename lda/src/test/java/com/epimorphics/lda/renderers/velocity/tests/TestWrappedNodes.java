package com.epimorphics.lda.renderers.velocity.tests;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.epimorphics.lda.renderers.velocity.IdMap;
import com.epimorphics.lda.renderers.velocity.ShortNames;
import com.epimorphics.lda.renderers.velocity.WrappedNode;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.RDFS;

public class TestWrappedNodes {

	static final String NS = "http://junit/epimorphics.com/ns#";
	
	ShortNames sn = new ShortNames( PrefixMapping.Factory.create() );
	
	@Test public void ensureItemPreservesResourceURI() {
		IdMap ids = new IdMap();		
		Model m = ModelFactory.createDefaultModel();
		Resource r = m.createResource( NS + "leafName" );
		WrappedNode.Bundle b = new WrappedNode.Bundle( sn, ids );
		WrappedNode i = new WrappedNode( b, r );
		assertEquals( r.getURI(), i.getURI().raw() );
	}
	
//	@Test public void ensureFindsSingleLabel() {
//		Resource r = m.createResource( NS + "labelled" );
//		WrappedNode wr = new WrappedNode( r );
////		assertEquals( wr.getLabel() );
//		r.addProperty( RDFS.label, "sticky" );
//		assertEquals( "sticky", wr.getLabel() );
//	}
	
	@Test public void ensureLabels() {
		// expect X if provided Y and ask for language Z
		ensureLabels( "label", "label", "" );
		ensureLabels( "root", "", "" );
		ensureLabels( "labelB,labelA", "labelA,labelB", "" );
		ensureLabels( "label", "label@en,chat@fr", "en" );
		ensureLabels( "root", "label@en,chat@fr", "de" );
		ensureLabels( "wanted", "wanted@en", "" );
	}

	private void ensureLabels( String expected, String provided, String language ) {
		IdMap ids = new IdMap();
		Model m = ModelFactory.createDefaultModel();
		Resource r = m.createResource( NS + "root" );
		if (provided.length() > 0)
			for (String one: provided.split(" *, *" )) {
				String [] parts = one.split( "@", 2 );
				Literal l = parts.length == 1 
					? m.createLiteral( parts[0] )
					: m.createLiteral( parts[0], parts[1] )
					;
				r.addProperty( RDFS.label,  l );
			}
	//
		Set<String> expect = new HashSet<String>();
		for (String s: expected.split( " *, *" )) expect.add( s );
	//
		WrappedNode.Bundle b = new WrappedNode.Bundle( sn,  ids );
		String result = new WrappedNode( b, r ).getLabel( language ).raw();
		assertTrue( "'" + result + "' expected to be one of " + expect, expect.contains( result ) );
	}
	
}
