package com.epimorphics.lda.apispec.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.specs.PropertyExpiryTimes;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class TestAPISpecExtractsPropertyExpiryTimes {

	Model spec = ModelIOUtils.modelFromTurtle
		( 
		":s a api:API; api:sparqlEndpoint <http://example.com/none>."
		+ "\n:P a rdf:Property; elda:cacheExpiryTime 10 ."
		+ "\n:Q a rdf:Property; elda:cacheExpiryTime 20 ."
		+ "\n:R a rdf:Property."
		);
	
	Resource s = spec.getResource( spec.expandPrefix( ":s" ) );
	Resource e = (Resource) spec.getResource( spec.expandPrefix( ":e" ) );

	@Test public void testExtractsPropertyTimes()
		{
		APISpec a = SpecUtil.specFrom( s );		
		
		Resource P = spec.getResource( spec.expandPrefix( ":P" ) );
		Resource Q = spec.getResource( spec.expandPrefix( ":Q" ) );
		
		PropertyExpiryTimes pet = PropertyExpiryTimes.testAssembly( P, 10L, Q, 20L );
		
		assertEquals( pet, a.getPropertyExpiryTimes() );
		}
	
	@Test public void testIt() {
		Resource P = spec.createProperty( "eh:/P" );
		Resource Q = spec.createProperty( "eh:/Q" );
		Resource R = spec.createProperty( "eh:/R" );
	//
		PropertyExpiryTimes pet = PropertyExpiryTimes.testAssembly( P, 10L, Q, 20L );
	//
		Model omP = ModelIOUtils.modelFromTurtle(":a <eh:/P> :b");
		Model omQ = ModelIOUtils.modelFromTurtle(":a <eh:/Q> :b");
		Model omR = ModelIOUtils.modelFromTurtle(":a <eh:/R> :b");
		Model omPQR = ModelIOUtils.modelFromTurtle
			(":a <eh:/P> :b. :c <eh:/Q> :d. :e <eh:/R> :f.");
		
		// TODO tsts
	}
}
