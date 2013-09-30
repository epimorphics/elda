package com.epimorphics.lda.query.tests;

import java.util.*;

import static junit.framework.Assert.*;

import org.junit.Test;

import com.epimorphics.lda.rdfq.*;
import com.epimorphics.lda.rdfq.RDFQ.Triple;
import com.epimorphics.lda.support.QuerySupport;
import com.hp.hpl.jena.vocabulary.RDF;

import static com.epimorphics.util.CollectionUtils.*;

public class TestQueryReordering {

	static final Any A = RDFQ.uri( "eh:/A" );
	static final Any B = RDFQ.uri( "eh:/B" );
	
	static final Any N = RDFQ.literal( 17 );
	static final Any M = RDFQ.literal( "M" );
	
	static final Any I = RDFQ.var( "?item" );
	static final Any X = RDFQ.var( "?A" );
	static final Any Y = RDFQ.var( "?B" );
	
	static final Any t = QuerySupport.text_query;
	static final Any T = RDFQ.uri( RDF.type.getURI() );
	static final Any P = RDFQ.uri( "eh:/P" );
	static final Any Q = RDFQ.uri( "eh:/Q" );
	
	static final RDFQ.Triple IPA = RDFQ.triple(I, P, A);
	static final RDFQ.Triple IPB = RDFQ.triple(I, P, B);
	
	static final RDFQ.Triple IPM = RDFQ.triple(I, P, M);
	static final RDFQ.Triple IPN = RDFQ.triple(I, P, N);
	
	static final RDFQ.Triple IPX = RDFQ.triple(I, P, X);
	static final RDFQ.Triple IPY = RDFQ.triple(I, P, Y);
	
	static final RDFQ.Triple IQA = RDFQ.triple(I, Q, A);
	static final RDFQ.Triple IQB = RDFQ.triple(I, Q, B);
	
	static final RDFQ.Triple IQM = RDFQ.triple(I, Q, M);
	static final RDFQ.Triple IQN = RDFQ.triple(I, Q, N);
	
	static final RDFQ.Triple IQX = RDFQ.triple(I, Q, X);
	static final RDFQ.Triple IQY = RDFQ.triple(I, Q, Y);
	
	static final RDFQ.Triple ItM = RDFQ.triple(I, t, M);
	static final RDFQ.Triple ItN = RDFQ.triple(I, t, N);
	
	static final RDFQ.Triple ITA = RDFQ.triple(I, T, A);
	static final RDFQ.Triple ITB = RDFQ.triple(I, T, B);
	
	// test that triples don't get dropped by the reordering.
	@Test public void testRetains() {
		testRetains( IPA );
		testRetains( IPB );
		testRetains( IPN );
		testRetains( IPM );
		testRetains( IPX );
		testRetains( IPY );
	//
		testRetains( IQA );
		testRetains( IQB );
		testRetains( IQN );
		testRetains( IQM );
		testRetains( IQX );
		testRetains( IQY );
	//
		testRetains( IPA, IPB );
		testRetains( IQA, IQB );
		testRetains( IPA, IPB, IQA, IQB );
		testRetains( IPX, IPB, IQX, IQY, IPN, IQM );
	//
		testRetains( ItM );
		testRetains( IPM );
		testRetains( ItM, ItN );
	}
	
	@Test public void testLeavesOrdinaryTriplesInGivenOrder() {
		assertEquals( list(IPA, IPB), reorder(IPA, IPB) );
		assertEquals( list(IPB, IPA), reorder(IPB, IPA) );
	}
	
	@Test public void testReordersLiteralsEarlier() {
		assertEquals( list(IPN, IPB), reorder(IPB, IPN) );
		assertEquals( list(IPM, IPB), reorder(IPB, IPM) );
	}
	
	@Test public void testPreservesRelativeLiteralOrder() {
		assertEquals( list(IPN, IPM), reorder(IPN, IPM) );
		assertEquals( list(IPM, IPN), reorder(IPM, IPN) );
	}
	
	@Test public void testOrdersTypesLaterPreservingRelativeOrder() {
		assertEquals( list(IPA, ITB), reorder(ITB, IPA) );
		assertEquals( list(IPB, ITA), reorder(ITA, IPB) );
		assertEquals( list(ITA, ITB), reorder(ITA, ITB) );
	}
	
	@Test public void testGeneralReordering() {
		assertEquals( list(ItM, IPN, ITA),reorder(ITA, ItM, IPN) );
	}
	
	@Test public void respectsTextQueryOrderingAgainstPlain() {
		assertEquals( list(ItM, IPA), reorder(true, ItM, IPA) );
		assertEquals( list(IPA, ItM), reorder(false, ItM, IPA) );
	}
	
	@Test public void respectsTextQueryOrderingAgainstTypes() {
		assertEquals( list(ItM, ITA), reorder(true, ItM, ITA) );
		assertEquals( list(ItM, ITA), reorder(false, ITA, ItM) );
	}

	private void testRetains(Triple ...triples) {
		Set<Triple> expected = set(triples);
		List<Triple> reordered = Arrays.asList( triples );
		Set<Triple> derived = new HashSet<Triple>( QuerySupport.reorder( reordered, true ) );
		assertEquals( expected.size(), reordered.size() );
		assertEquals( expected, derived );
	}
	
	private List<Triple> reorder(Triple... triples) {
		return reorder( true, triples );
	}
	
	private List<Triple> reorder(boolean tqFirst, Triple... triples) {
		return QuerySupport.reorder( Arrays.asList( triples ), tqFirst );
	}
	
}
