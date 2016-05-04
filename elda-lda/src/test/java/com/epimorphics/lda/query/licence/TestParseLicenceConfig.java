package com.epimorphics.lda.query.licence;

import static org.junit.Assert.*;

import java.util.HashSet;

import org.junit.Test;

import com.epimorphics.lda.core.ModelLoader;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.vocabularies.API;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;

public class TestParseLicenceConfig {

	final Model model = ModelFactory.createDefaultModel();
	
	final Resource root = model.createResource("eh:/the-spec");
	
	final Resource sparql = model.createResource("eh:/sparqlEndpoint");
	
	final Resource theEndpoint = model.createResource("eh:/the-endpoint");
	
	static final ModelLoader loader = new ModelLoader() {

		@Override public Model loadModel(String uri) {
			return null;
		}
		
	};
	
	@Test public void testParseEmptyLicenceConfig() {
		setBaseConfig();
//		model.write(System.err, "TTL");
		APISpec a = new APISpec(FileManager.get(), root, loader);
		assertEquals(new HashSet<RDFNode>(), a.getLicenceNodes());
		assertEquals(new HashSet<RDFNode>(), a.getEndpoints().get(0).getLicenceNodes());
	}

	private void setBaseConfig() {
		model.add(root, RDF.type, API.API);
		model.add(root, API.sparqlEndpoint, sparql);
		model.add(root, API.endpoint, theEndpoint);
		
		model.add(theEndpoint, RDF.type, API.ListEndpoint);
		model.add(theEndpoint, API.uriTemplate, "/an/endpoint");
	}


}
