/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.shortnames.tests;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.exceptions.EldaException;
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
	
	// not meant to be exhaustive
	static final String[][] params = new String[][] {
		new String[] {"shortname",    "full-uri:GOOD"}
		, new String[] {"shortName",  "full-uri:GOOD"}
		, new String[] {"short_name", "full-uri:GOOD"}
		, new String[] {"sh0rtname",  "full-uri:GOOD"}
		, new String[] {"ShortName",  "full-uri:GOOD"}
		, new String[] {"A_Z",        "full-uri:GOOD"}
		, new String[] {"short name", "full-uri:BAD"}
		, new String[] {"1shortname", "full-uri:BAD"}
		, new String[] {"short-name", "full-uri:BAD"}
		, new String[] {"$hortname",  "full-uri:BAD"}
		, new String[] {"short,name", "full-uri:BAD"}
		, new String[] {"",           "full-uri:BAD"}
		, new String[] {"short|name", "full-uri:BAD"}
	};
	
	@Test public void testDetectsBadShortnames() {
		if (true) return;
		for (String [] p: params) {
			Context c = new Context();
			String shortName = p[0], fullURI = p[1];
			try { 
				c.recordPreferredName(shortName, fullURI);
				if (fullURI.endsWith("BAD"))
					fail("should have spotted bad shortname '" + shortName + "'");
			} catch (EldaException e) {
				if (fullURI.endsWith("GOOD")) 
					fail("should have accepted good shortname '" + shortName + "'");
			}
		}
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
