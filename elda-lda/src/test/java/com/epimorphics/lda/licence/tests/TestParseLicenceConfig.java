package com.epimorphics.lda.licence.tests;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.epimorphics.lda.core.ModelLoader;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.lda.vocabularies.ELDA_API;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;

public class TestParseLicenceConfig {
	
	final Set<RDFNode> empty = new HashSet<RDFNode>();

	final Model model = ModelFactory.createDefaultModel();
	
	final Resource root = model.createResource("eh:/the-spec");
	
	final Resource sparql = model.createResource("eh:/sparqlEndpoint");
	
	final Resource theEndpoint = model.createResource("eh:/the-endpoint");
	
	final Resource aLicenceResource = model.createResource("eh:/licence-1");
	
	static final ModelLoader loader = new ModelLoader() {

		@Override public Model loadModel(String uri) {
			return null;
		}
		
	};
	
	@Test public void testParseEmptyLicenceConfig() {
		setBaseConfig();
		APISpec a = new APISpec(FileManager.get(), root, loader);
		assertEquals(new HashSet<RDFNode>(), a.getLicenceNodes());
		assertEquals(new HashSet<RDFNode>(), a.getEndpoints().get(0).getLicenceNodes());
	}
	
	@Test public void testParseAPILicenceConfig() {
		setBaseConfig();
		model.add(root, ELDA_API.license, "this.is.a.path");
		model.add(root, ELDA_API.license, aLicenceResource);
		
		Set<RDFNode> expected = new HashSet<RDFNode>();
		expected.add(aLicenceResource);
		expected.add(literal("this.is.a.path"));
		
		APISpec a = new APISpec(FileManager.get(), root, loader);
		assertEquals(expected, a.getLicenceNodes());
		assertEquals(expected, a.getEndpoints().get(0).getLicenceNodes());
	}
	
	@Test public void testParseEndpointLicenceConfig() {
		setBaseConfig();
		model.add(theEndpoint, ELDA_API.license, "this.is.a.path");
		model.add(theEndpoint, ELDA_API.license, aLicenceResource);
		
		Set<RDFNode> expected = new HashSet<RDFNode>();
		expected.add(aLicenceResource);
		expected.add(literal("this.is.a.path"));
		
		APISpec a = new APISpec(FileManager.get(), root, loader);
		assertEquals(empty, a.getLicenceNodes());
		assertEquals(expected, a.getEndpoints().get(0).getLicenceNodes());
	}
	
	@Test public void testParseAPIandEndpointLicenceConfig() {
		setBaseConfig();
		model.add(root, ELDA_API.license, "this.is.a.path");
		model.add(theEndpoint, ELDA_API.license, aLicenceResource);
		
		Set<RDFNode> justPath = new HashSet<RDFNode>();
		Set<RDFNode> expected = new HashSet<RDFNode>();
		justPath.add(literal("this.is.a.path"));
		expected.add(aLicenceResource);
		expected.add(literal("this.is.a.path"));
		
		APISpec a = new APISpec(FileManager.get(), root, loader);
		assertEquals(justPath, a.getLicenceNodes());
		assertEquals(expected, a.getEndpoints().get(0).getLicenceNodes());
	}

	private Literal literal(String string) {
		return model.createLiteral(string);
	}

	private void setBaseConfig() {
		model.add(root, RDF.type, API.API);
		model.add(root, API.sparqlEndpoint, sparql);
		model.add(root, API.endpoint, theEndpoint);
		
		model.add(theEndpoint, RDF.type, API.ListEndpoint);
		model.add(theEndpoint, API.uriTemplate, "/an/endpoint");
	}


}
