package com.epimorphics.lda.renderers.tests;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Set;

import org.junit.Test;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.renderers.JSONRenderer;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
    Test that the JSON renderer doesn't corrupt the Context with
    newly required short names. (The JSON writer is still free to
    do so, but the JSON renderer takes a copy of the context.)
*/
public class TestJsonRenderer {
	
	@Test public void testIt() {
		Bindings b = new Bindings();
		Model model = ModelIOUtils.modelFromTurtle( "<fake:root> <fake:property> 17 ." );
		Resource root = model.createResource( "fake:root" );
		Context given = new Context();
		String rendered = new JSONRenderer( null ).renderFromModelAndContext( b, model, root, given );
		Set<String> allShortNames = given.allNames();
		assertEquals( "rendering should not update the context", Collections.EMPTY_SET, allShortNames );
	}

}
