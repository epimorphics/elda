package com.epimorphics.lda.renderers.tests;

import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;

public class TestXMLRenderer {

	@Test public void testSketch() {
		Model m = ModelTestBase.modelWithStatements( "a P b" );
		
//		String got = docToString( d );
		
	}
	
}
