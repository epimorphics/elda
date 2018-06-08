package com.epimorphics.lda.config.tests;

import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

import com.epimorphics.lda.configs.ConfigLoader;
import com.epimorphics.lda.core.ModelLoader;
import com.epimorphics.lda.tests_support.FileManagerModelLoader;
import com.epimorphics.lda.vocabularies.ELDA_API;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.XSD;

public class TestConfigLoader {

	static final Model testModel = ModelFactory.createDefaultModel();
	
	static final ModelLoader ml = new FileManagerModelLoader();
	
	static final Resource example = testModel.createResource(ELDA_API.getURI() + "example");
		
	static final Model ignore = testModel.add(example, RDF.type, XSD.xstring);
	
	@Test @Ignore public void testConfigLoader() {
		Model m = ConfigLoader.loadModelExpanding(ml, "includefiles/toplevel.ttl");		
		assertTrue(m.isIsomorphicWith(testModel));
	}
}
