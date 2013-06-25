package com.epimorphics.lda.shortnames.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.shortnames.BuiltIn;
import com.hp.hpl.jena.rdf.model.Model;

public class TestContext {

	@Test public void testSettingMultiplicityUnhindered() {
		Set<String> seen = new HashSet<String>();
		Model m = ModelIOUtils.modelFromTurtle
			( ":p a rdf:Property; api:label 'np'." 
			+ "\n:q a rdf:Property; api:label 'nq'; api:multiValued true."
			+ "\n:r a rdf:Property; api:label 'nr'; api:multiValued false."  
			);
	//
		Context c = new Context();
		c.loadVocabularyAnnotations( seen, m );
	//
		assertFalse( c.getPropertyByName( "np" ).isMultivalued() );
		assertTrue( c.getPropertyByName( "nq" ).isMultivalued() );
		assertFalse( c.getPropertyByName( "nr" ).isMultivalued() );
	}	
	
	@Test public void testSettingMultiplicity() {
		Set<String> seen = new HashSet<String>();
		Model m = ModelIOUtils.modelFromTurtle
			( "rdfs:comment a rdf:Property; api:multiValued false."
			);
	//
		Context c = new Context();
		c.loadVocabularyAnnotations( seen, m );
		
        for (Model vocab: BuiltIn.vocabularies) 
        	c.loadVocabularyAnnotations( seen, vocab );
    //
		// assertFalse( c.getPropertyByName( "comment" ).isMultivalued() );
	}
}
