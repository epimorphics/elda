/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.support.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.hp.hpl.jena.rdf.model.Model;

public class TestContextRecognisesPropertiesByRange {

	Model m = ModelIOUtils.modelFromTurtle
		( "@prefix : <eh:/>."
		+ "\n:a a owl:DatatypeProperty." 
		+ "\n:b a owl:ObjectProperty." 
		+ "\n:c a rdf:Property."
		+ "\n:d rdfs:range xsd:integer."
		+ "\n:e api:name 'e'."
		+ "\n:f rdfs:label 'f'."
		+ "\n:g a rdfs:Class."
		);
		
	@Test public void testRecognises() {
		Context c = new Context( m );
		assertNotNull( "cannot find 'a'", c.getPropertyByURI( "eh:/a" ) );
		assertNotNull( "cannot find 'b'", c.getPropertyByURI( "eh:/b" ) );
		assertNotNull( "cannot find 'c'", c.getPropertyByURI( "eh:/c" ) );
		assertNotNull( "cannot find 'd'", c.getPropertyByURI( "eh:/d" ) );
	//
		assertNull( "unexpected 'e'", c.getPropertyByURI( "eh:/e" ) );
		assertNull( "unexpected 'f'", c.getPropertyByURI( "eh:/f" ) );
		assertNull( "unexpected 'g'", c.getPropertyByURI( "eh:/g" ) );
	}
	
}
