package com.epimorphics.lda.renderers.velocity.tests;

import java.util.Map;

import org.junit.Test;

import com.epimorphics.lda.renderers.velocity.Help;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class TestVelocityMetadata {

	@Test public void testMe() {
		Model m = ModelFactory.createDefaultModel();
		Map<String, Object> meta = Help.getMetadataFrom( m );
	}
}
