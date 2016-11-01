package com.epimorphics.lda.restlets.tests;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.core.APIEndpointImpl;
import com.epimorphics.lda.routing.DefaultRouter;
import com.epimorphics.lda.routing.Router;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.support.EldaFileManager;
import com.epimorphics.lda.tests_support.LoadsNothing;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;

public class TestItemTemplateFallback {
	
	final Model model = ModelFactory.createDefaultModel();
	final Resource sparql = model.createResource("eh:/sparqlEndpoint");
	final Resource theEndpoint = model.createResource("eh:/the-endpoint");
	final Resource otherEndpoint = model.createResource("eh:/other-endpoint");
	final Resource root = model.createResource("eh:/the-spec");
	final FileManager fm = EldaFileManager.get();
	
	{ setBaseConfig(); }
	
	final Router r = new DefaultRouter();
	final APISpec parent = new APISpec(fm, root, LoadsNothing.instance);
	
	final APIEndpointSpec specA = new APIEndpointSpec(parent, parent, theEndpoint);
	final APIEndpoint apA = new APIEndpointImpl(specA);
	
	final APIEndpointSpec specB = new APIEndpointSpec(parent, parent, otherEndpoint);
	final APIEndpoint apB = new APIEndpointImpl(specB);

	{ 
		r.register("_", "/an/endpoint", apA);
		r.register("_", "/other/endpoint.pseudo", apB);
	}

	final Set<String> _formats = new HashSet<String>(Arrays.asList("html", "json", "ttl", "csv"));
	
	public void setBaseConfig() {
		model.add(root, RDF.type, API.API);
		model.add(root, API.sparqlEndpoint, sparql);
		model.add(root, API.endpoint, theEndpoint);
		model.add(root, API.endpoint, otherEndpoint);
		
		model.add(theEndpoint, RDF.type, API.ListEndpoint);
		model.add(theEndpoint, API.uriTemplate, "/an/endpoint");
		model.add(theEndpoint, API.itemTemplate, "/item/template");
		
		model.add(otherEndpoint, RDF.type, API.ListEndpoint);
		model.add(otherEndpoint, API.uriTemplate, "/other/endpoint.pseudo");
		model.add(otherEndpoint, API.itemTemplate, "/other/item/template");
	}
	
	@Test public void testOrdinaryEndpoint() throws URISyntaxException {
		assertUnfound(new URI("http://sundry/absent/template"), "/absent/template");
		assertFound("/an/endpoint", null, new URI("http://sundry/item/template"), "/item/template");
		assertFound("/an/endpoint.json", "json", new URI("http://sundry/item/template"), "/item/template");
		assertFound("/an/endpoint.csv", "csv", new URI("http://sundry/item/template"), "/item/template");
	}
	
	@Test public void testDotNonformatEndpoint() throws URISyntaxException {
		assertFound("/other/endpoint.pseudo", null, new URI("http://sundry/other/item/template"), "/other/item/template");
		assertFound("/other/endpoint.pseudo.json", "json", new URI("http://sundry/other/item/template"), "/other/item/template");
		assertFound("/other/endpoint.pseudo.csv", "csv", new URI("http://sundry/item/template"), "/other/item/template");
	}

	private void assertUnfound(URI requestURI, String itemPath) {
		String obtained = r.findItemURIPath("_", requestURI, itemPath);
		assertNull(obtained);
	}

	private void assertFound(String expected, String type, URI requestURI, String itemPath) throws URISyntaxException {
		String obtained = r.findItemURIPath("_", requestURI, itemPath);
		if (obtained == null) fail("expected " + expected + ", obtained <null>.");
		URI formatted = DefaultRouter.accountForFormat(type, _formats, obtained);
		assertEquals(new URI(expected), formatted);
	}
}
