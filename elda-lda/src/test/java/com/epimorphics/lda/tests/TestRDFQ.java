package com.epimorphics.lda.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.epimorphics.lda.rdfq.Any;
import com.epimorphics.lda.rdfq.RDFQ;
import com.epimorphics.lda.support.PrefixLogger;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TestRDFQ {

	@Test public void testQuotesInSpelling() {
		PrefixLogger pl = new PrefixLogger( PrefixMapping.Extended );
		Any a = RDFQ.literal( "\"inside\"" );
		assertEquals( "\"\\\"inside\\\"\"", a.asSparqlTerm( pl ) );
	}
	
}
