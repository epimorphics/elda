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
import com.epimorphics.lda.specs.PropertyExpiryTimes;
import com.epimorphics.lda.vocabularies.ELDA_API;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;

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

	@Test public void testExtractsPropertyTimes() {
		APISpec a = SpecUtil.specFrom(s);
		Resource P = spec.getResource(spec.expandPrefix(":P"));
		Resource Q = spec.getResource(spec.expandPrefix(":Q"));
		PropertyExpiryTimes pet = PropertyExpiryTimes.buildForTests(P, 10L, Q, 20L);
		assertEquals(pet, a.getPropertyExpiryTimes());
	}
	
	@Test public void testExpiryTimesScaling() {
		testExpiryTimesScaling( "<eh:/P>", "100", 100 );
		testExpiryTimesScaling( "<eh:/Q>", "100s", 100 );
		testExpiryTimesScaling( "<eh:/R>", "100m", 100 * 60 );
		testExpiryTimesScaling( "<eh:/S>", "100h", 100 * 60 * 60 );
		testExpiryTimesScaling( "<eh:/T>", "100d", 100 * 60 * 60 * 24 );
		testExpiryTimesScaling( "<eh:/U>", "100w", 100 * 60 * 60 * 24 * 7 );
	}

	private void testExpiryTimesScaling(String property, String value, long expectedSeconds) {
		Model m = ModelFactory.createDefaultModel();
		Resource S = m.createResource(property);
		m.add(S, ELDA_API.cacheExpiryTime, value );
		m.add(S, RDF.type, RDF.Property);
		PropertyExpiryTimes pet = PropertyExpiryTimes.assemble(m);
		assertEquals(expectedSeconds * 1000, pet.timeInMillisFor(S));
	}
}
