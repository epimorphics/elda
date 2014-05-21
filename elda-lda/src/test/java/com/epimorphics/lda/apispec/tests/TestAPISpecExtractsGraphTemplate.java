package com.epimorphics.lda.apispec.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.specs.APISpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class TestAPISpecExtractsGraphTemplate {

	Model specWithoutAPIGraphTemplate = ModelIOUtils.modelFromTurtle
		(":root a api:API"
		+ "\n; api:sparqlEndpoint <eh:/sparqlEnpoint>"
		+ "\n; api:endpoint :endpointA, :endpointB"
		+ "\n."
		+ "\n:endpointA a api:ListEndpoint"
		+ "\n; api:uriTemplate 'uri/template/A'"
		+ "\n; elda:graphTemplate 'hello/world'"
		+ "\n."
		+ "\n:endpointB a api:ListEndpoint"
		+ "\n; api:uriTemplate 'uri/template/B'"
		+ "\n."
		+ "\n"
		);
	
	Model specWithAPIGraphTemplate = ModelIOUtils.modelFromTurtle
		(":root a api:API"
		+ "\n; api:sparqlEndpoint <eh:/sparqlEnpoint>"
		+ "\n; elda:graphTemplate 'api/graph/template'"
		+ "\n; api:endpoint :endpointA, :endpointB"
		+ "\n."
		+ "\n:endpointA a api:ListEndpoint"
		+ "\n; api:uriTemplate 'uri/template/A'"
		+ "\n; elda:graphTemplate 'hello/world'"
		+ "\n."
		+ "\n:endpointB a api:ListEndpoint"
		+ "\n; api:uriTemplate 'uri/template/B'"
		+ "\n."
		+ "\n"
		);
	
	@Test public void testWithoutAPIGraphTemplate() {
		Resource root = specWithoutAPIGraphTemplate.getResource( specWithoutAPIGraphTemplate.expandPrefix(":root"));
		APISpec a = SpecUtil.specFrom(root);
	//
		APIEndpointSpec A = getEndpoint(a, "endpointA");
		assertEquals("hello/world", A.getGraphTemplate());
	//
		APIEndpointSpec B = getEndpoint(a, "endpointB");
		assertEquals(null, B.getGraphTemplate());
	}
	
	@Test public void testWithAPIGraphTemplate() {
		Resource root = specWithAPIGraphTemplate.getResource( specWithAPIGraphTemplate.expandPrefix(":root"));
		APISpec a = SpecUtil.specFrom(root);
	//
		APIEndpointSpec A = getEndpoint(a, "endpointA");
		assertEquals("hello/world", A.getGraphTemplate());
	//
		APIEndpointSpec B = getEndpoint(a, "endpointB");
		assertEquals("api/graph/template", B.getGraphTemplate());
	}

	private APIEndpointSpec getEndpoint(APISpec a, String localName) {
		for (APIEndpointSpec e: a.getEndpoints())
			if (e.getResource().getLocalName().equals(localName))
				return e;
		return null;
	}
}
