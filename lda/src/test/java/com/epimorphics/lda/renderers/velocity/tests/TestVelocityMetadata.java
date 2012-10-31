package com.epimorphics.lda.renderers.velocity.tests;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.epimorphics.lda.renderers.velocity.Help;
import com.epimorphics.lda.renderers.velocity.ShortNames;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

public class TestVelocityMetadata {

	@Test public void testMe() {
		Model m = ModelFactory.createDefaultModel();
		ShortNames shortNames = new ShortNames(m);
		Map<String, Object> meta = Help.getMetadataFrom( shortNames, m );
	}
}
