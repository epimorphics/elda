/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.apispec.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.specs.APISpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

public class TestAPISpecExtractsExpiryTime {
	
	@Test public void testExtractsDefaultExpiry() {
		testExtractsExpiry( -1000, "" );
		testExtractsExpiry( 1 * 1000, "elda:cacheExpiryTime 1" );
		testExtractsExpiry( 10 * 1000, "elda:cacheExpiryTime 10" );
		testExtractsExpiry( 100 * 1000, "elda:cacheExpiryTime 100" );
		testExtractsExpiry( 7 * 1000, "elda:cacheExpiryTime '7s'" );
		testExtractsExpiry( 3 * 60 * 1000, "elda:cacheExpiryTime '3m'" );
		testExtractsExpiry( 9 * 60 * 60 * 1000, "elda:cacheExpiryTime '9h'" );
		testExtractsExpiry( 2 * 7 * 24 * 60 * 60 * 1000, "elda:cacheExpiryTime '2w'" );	}

	public void testExtractsExpiry(long expected, String timeSpec) {
		Model spec = ModelIOUtils.modelFromTurtle
			( ":s a api:API"
			+ "; " + timeSpec
			+ "; api:sparqlEndpoint <http://example.com/none>"
			+ "."
			);
		Resource s = spec.getResource( spec.expandPrefix( ":s" ) );
		APISpec a = SpecUtil.specFrom( s );		
		assertEquals( expected, a.getCacheExpiryMilliseconds());
	}
}
