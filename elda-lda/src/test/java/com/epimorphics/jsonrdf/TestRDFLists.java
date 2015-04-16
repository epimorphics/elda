/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2012 Epimorphics Limited
    $Id$
*/

package com.epimorphics.jsonrdf;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class TestRDFLists {
	
	final Model m = ModelFactory.createDefaultModel();
	
	@Test public void testIsList() {
		Resource notList = r( "nl" );
		assertFalse( RDFUtil.isList( notList ) );
	//
		assertTrue( RDFUtil.isList( RDF.nil ) );
	//
		Resource hasFirst = r( "hf" );
		hasFirst.addProperty( RDF.first, "first" );
		assertTrue( RDFUtil.isList( hasFirst ) );
	//
		Resource hasLabel = r( "hl" );
		hasLabel.addProperty( RDFS.label, "what's my name?" );
		assertFalse( RDFUtil.isList( hasLabel ) );
	}
	
	@Test public void testUsesMarkersForMissingFirst() {
		Resource listMissingSecond = r( "lmf" );
		Resource spine = m.createResource();
		listMissingSecond.addProperty( RDF.first, "first" ).addProperty( RDF.rest, spine );
		spine.addProperty( RDF.rest,  RDF.nil );
	//
		List<RDFNode> elements = RDFUtil.asJavaList( listMissingSecond );
		assertEquals( 2, elements.size() );
		assertEquals( m.createLiteral("first"), elements.get(0) );
		assertEquals( RDFUtil.Vocab.missingListElement, elements.get(1) );
	}
	
	@Test public void testUsesMarkersForMissingRest() {
		Resource listMissingTail = r( "lmt" );
		listMissingTail.addProperty( RDF.first, "first" );
	//
		List<RDFNode> elements = RDFUtil.asJavaList( listMissingTail );
	//
		assertEquals( 2, elements.size() );
		assertEquals( m.createLiteral("first"), elements.get(0) );
		assertEquals( RDFUtil.Vocab.missingListTail, elements.get(1) );
	}
	
	protected Resource r( String localName ) {
		return m.createResource( "eh:/" + localName );
	}

}
