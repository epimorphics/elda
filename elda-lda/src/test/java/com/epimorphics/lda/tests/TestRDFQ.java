package com.epimorphics.lda.tests;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static com.hp.hpl.jena.test.JenaTestBase.assertDiffer;

import com.epimorphics.lda.rdfq.*;
import com.epimorphics.lda.support.PrefixLogger;
import com.epimorphics.util.CollectionUtils;
import com.hp.hpl.jena.shared.PrefixMapping;

/**
    Tests for elements of the RDFQ representation for query triples.
*/
public class TestRDFQ {

	@Test public void testQuotesInSpelling() {
		PrefixLogger pl = new PrefixLogger( PrefixMapping.Extended );
		Any a = RDFQ.literal( "\"inside\"" );
		assertEquals( "\"\\\"inside\\\"\"", a.asSparqlTerm( pl ) );
	}
	
	Term a = RDFQ.uri( "eh:/alpha" );
	Term a2 = RDFQ.uri( "eh:/alpha" );
	Term notA = RDFQ.uri( "eh:/phala" );
	
	Term b = RDFQ.literal( "beta" );
	Term b2 = RDFQ.literal( "beta" );
	Term notB = RDFQ.literal( "beat" );
	
	Variable c = RDFQ.var( "?gamma" );
	Variable c2 = RDFQ.var( "?gamma" );
	Variable notC = RDFQ.var( "?ammag" );
	
	Value i = RDFQ.literal(17);
	Value i2 = RDFQ.literal(17);
	Value notI = RDFQ.literal(42);
	
	AnyList l = RDFQ.list( a, b, c );
	AnyList l2 = RDFQ.list( a, b, c );
	AnyList notL = RDFQ.list( c, b, c );
	
	@Test public void testX() {
		
	}
	
	@Test public void testEqualityURI() {
		assertEquals( a, a2 );
		assertDiffer( a, notA );
		assertEquals( "eh:/alpha", a.spelling() );
	}
	
	@Test public void testEqualityLiteral() {
		assertEquals( b, b2 );
		assertDiffer( b, notB );
		assertEquals( "beta", b.spelling() );
	}
	
	@Test public void testEqualityInteger() {
		assertEquals( i, i2 );
		assertDiffer( i, notI );
		assertEquals( "17", i.spelling() );
	}
		
	@Test public void testEqualityVariable() {
		assertEquals( c, c2 );
		assertDiffer( c, notC );
		assertEquals( "?gamma", c.name() );
	}
	
	@Test public void testInequalities() {
		assertDiffer( a, b );
		assertDiffer( a, c );
		assertDiffer( a, l );
		assertDiffer( a, i );
		assertDiffer( b, c );
		assertDiffer( b, i );
		assertDiffer( b, l );
		assertDiffer( c, i );
		assertDiffer( c, l );
	}
	
	@Test public void testEqualityList() {
		assertEquals( l, l2 );
		assertDiffer( l, notL );
	}
	
	@Test public void testListRetainsElements() {
		assertEquals( CollectionUtils.list(a, b, c), l.getElements() );
	}
	
	@Test public void testListSize() {
		assertEquals( 0, RDFQ.list().size() );
		assertEquals( 1, RDFQ.list(a).size() );
		assertEquals( 2, RDFQ.list(a, b).size() );
		assertEquals( 3, RDFQ.list(a, b, c).size() );
	}
	
	PrefixLogger pl = new PrefixLogger( PrefixMapping.Standard );
	
	@Test public void testSparqRendering() {
		assertEquals( "<eh:/alpha>", a.asSparqlTerm( pl ) );
		assertEquals( "\"beta\"", b.asSparqlTerm( pl ) );
		assertEquals( "?gamma", c.asSparqlTerm( pl ) );
		assertEquals( "17", i.asSparqlTerm( pl ) );
		assertEquals( "( <eh:/alpha> \"beta\" ?gamma)", l.asSparqlTerm( pl ) );
	}
	
}
