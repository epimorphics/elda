package com.epimorphics.lda.endpointspec.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.apispec.tests.SpecUtil;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.specs.APISpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class TestExtractsEndpointExpiryTimes {

	Model makeModel(String forAPI, String forEndpoint) {
		return  ModelIOUtils.modelFromTurtle
			( ":s a api:API"
			+ "; " + forAPI
			+ "; api:endpoint :e"
			+ "; api:sparqlEndpoint <http://example.com/none>"
			+ ".\n"
			+ ":e a api:ItemEndpoint"
			+ "; " + forEndpoint
			+ "; api:uriTemplate '/absent/friends'"
			+ "; api:itemTemplate 'http://fake.domain.org/spoo/{what}'"
			+ ".\n" 
			);
	}
	
	@Test public void testDefaultEndpointExpiryTime() {
		Model spec = makeModel("", "");
		Resource s = spec.getResource( spec.expandPrefix( ":s" ) );		
		Resource e = spec.getResource( spec.expandPrefix( ":e" ) );
		APISpec a = SpecUtil.specFrom( s );
		APIEndpointSpec eps = new APIEndpointSpec( a, null, e );
		assertTrue( eps.getCacheExpiryMilliseconds() < 0);
	}
	
	@Test public void testInheritedEndpointExpiryTime() {
		Model spec = makeModel("elda:cacheExpiryTime 17", "");
		Resource s = spec.getResource( spec.expandPrefix( ":s" ) );		
		Resource e = spec.getResource( spec.expandPrefix( ":e" ) );
		APISpec a = SpecUtil.specFrom( s );
		APIEndpointSpec eps = new APIEndpointSpec( a, null, e );
		assertEquals( 17 * 1000, eps.getCacheExpiryMilliseconds() );
	}
	
	@Test public void testOverriddenEndpointExpiryTime() {
		Model spec = makeModel("elda:cacheExpiryTime 17", "elda:cacheExpiryTime 2000");
		Resource s = spec.getResource( spec.expandPrefix( ":s" ) );		
		Resource e = spec.getResource( spec.expandPrefix( ":e" ) );
		APISpec a = SpecUtil.specFrom( s );
		APIEndpointSpec eps = new APIEndpointSpec( a, null, e );
		assertEquals( 2000 * 1000, eps.getCacheExpiryMilliseconds() );
	}
	
}
