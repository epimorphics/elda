/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.apispec.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.hamcrest.*;
import org.junit.Test;

import com.epimorphics.lda.specs.ExtractPrefixMapping;
import com.epimorphics.lda.vocabularies.API;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TestAPISpecExtractPrefixMapping {
	
	@Test public void testWithNoMappings() {
		Model m = ModelFactory.createDefaultModel();
		Resource r = ModelTestBase.resource( m, "null" );
		PrefixMapping pm = ExtractPrefixMapping.from( r );
		assertEquals( 0, pm.getNsPrefixMap().size() );
	}
	
	@Test public void testWithOnlyModelPrefixMappings() {
		testWithOnlyModelMappings(makeMapping( "" ));
	}
	
	@Test public void testWithOnlyOneModelPrefixMapping() {
		testWithOnlyModelMappings(makeMapping( "a=eh:/A#" ));
	}
	
	@Test public void testWithOnlyTwoModelPrefixMappings() {
		testWithOnlyModelMappings(makeMapping( "a=eh:/A#; b=eh:/B#" ));
	}
	
	@Test public void testWithOneMappingInModel() {
		String modelString = "mine api:prefixMapping _x; _x api:prefix 'spoo'; _x api:namespace 'eh:/whatever/spoo#'";
		testWithPrefixesFromModelAPI(modelString, "spoo=eh:/whatever/spoo#");
	}
	
	@Test public void testWithTwoMappingsInModel() {
		String modelString = 
			"mine api:prefixMapping _x"
			+ "; _x api:prefix 'spoo'"
			+ "; _x api:namespace 'eh:/whatever/spoo#'"
			+ "; mine api:prefixMapping _y"
			+ "; _y api:prefix 'red'"
			+ "; _y api:namespace 'eh:/whatever/reddish#'"
			;
		testWithPrefixesFromModelAPI(modelString, "spoo=eh:/whatever/spoo#; red=eh:/whatever/reddish#");
	}

	private void testWithPrefixesFromModelAPI(String modelString, String mappingString) {
		PrefixMapping expected = makeMapping( mappingString );
		Model m = ModelFactory.createDefaultModel();
		m.setNsPrefix( "api", API.getURI() );
		ModelTestBase.modelAdd(m, modelString);
		m = ModelFactory.createDefaultModel().add(m);
		Resource r = ModelTestBase.resource( m, "mine" );
		PrefixMapping pm = ExtractPrefixMapping.from( r );
		assertThat( pm, IsSame(expected) );
	}

	private void testWithOnlyModelMappings(PrefixMapping expected) {
		Model m = ModelFactory.createDefaultModel();
		m.setNsPrefixes(expected);
		Resource r = ModelTestBase.resource( m, "null" );
		PrefixMapping pm = ExtractPrefixMapping.from( r );
		assertThat( pm, IsSame(expected) );
	}

	private Matcher<PrefixMapping> IsSame( final PrefixMapping expected ) {
		return new BaseMatcher<PrefixMapping>() {
			
			@Override public boolean matches( Object got ) {
				return got instanceof PrefixMapping && expected.samePrefixMappingAs( (PrefixMapping) got );
			}

			@Override public void describeTo( Description d ) {
				d.appendText( "same as " + expected );
			}
			
		};
	}

	private PrefixMapping makeMapping(String spec) {
		PrefixMapping result = PrefixMapping.Factory.create();
		if (spec.length() > 0)
			for (String elem: spec.split( " *; *" ))
				setMapping( result, elem );
		return result;
	}

	private void setMapping(PrefixMapping result, String elem) {
		String [] parts = elem.split( " *= *" );
		result.setNsPrefix( parts[0], parts[1] );
	}

}
