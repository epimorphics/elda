package com.epimorphics.lda.renderers.velocity.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.*;

import org.junit.Test;

import com.epimorphics.lda.renderers.velocity.*;
import com.epimorphics.util.CollectionUtils;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.RDF;
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
	
	@Test public void ensure_finds_properties() {
		WrappedNode.Bundle b = new WrappedNode.Bundle( sn,  new IdMap() );
		Model m = ModelFactory.createDefaultModel();
		Resource r = m.createResource( NS + "S" );
		r.addProperty( RDFS.label, "hello" ).addLiteral( RDF.first, 10 );
		WrappedNode w = new WrappedNode( b, r );
	//
		Set<WrappedNode> expected = new HashSet<WrappedNode>();
		expected.add( new WrappedNode( b, RDF.first.inModel( m ) ) );
		expected.add( new WrappedNode( b, RDFS.label.inModel( m ) ) );
		assertEquals( expected, new HashSet<WrappedNode>( w.getProperties() ) );
	}
	
	@Test public void ensure_finds_inverse_properties() {
		WrappedNode.Bundle b = new WrappedNode.Bundle( sn,  new IdMap() );
		Model m = ModelFactory.createDefaultModel();
		Resource Sa = m.createResource( NS + "Sa" );
		Resource Sb = m.createResource( NS + "Sb" );
		Resource O = m.createResource( NS + "O" );
	//
		Sa.addProperty( RDF.first, O );
		Sb.addProperty( RDFS.domain, O );
		WrappedNode w = new WrappedNode( b, O );
	//
		Set<WrappedNode> expected = new HashSet<WrappedNode>();
		expected.add( new WrappedNode( b, RDF.first.inModel( m ) ) );
		expected.add( new WrappedNode( b, RDFS.domain.inModel( m ) ) );
		assertEquals( expected, new HashSet<WrappedNode>( w.getInverseProperties() ) );
	}
	
	@Test public void ensure_follows_inverse_properties() {
		WrappedNode.Bundle b = new WrappedNode.Bundle( sn,  new IdMap() );
		Model m = ModelFactory.createDefaultModel();
	//
		Resource Sa = m.createResource( NS + "Sa" );
		Resource Sb = m.createResource( NS + "Sb" );
		Resource O = m.createResource( NS + "O" );
	//
		Sa.addProperty( RDF.first, O );
		Sb.addProperty( RDF.value, O );
	//
		WrappedNode w = new WrappedNode( b, O );
	//
		List<WrappedNode> got = w.getInverseValues( new WrappedNode( b, RDF.value.inModel( m ) ) );
		assertEquals( CollectionUtils.list( new WrappedNode( b, Sb ) ), got );
	
	}
	
}
