package com.epimorphics.lda.apispec.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.specs.APISpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
    Test that API specs and endpoints correctly detect settings of
    elda:enableCounting.
*/
public class TestEnableCounting {
	
	// test configuration of an API spec
	@Test public void testSpecEnableCountingDefault() {
		Model m = ModelIOUtils.modelFromTurtle
			( ":my a api:API; api:sparqlEndpoint :spoo" );
		Resource root = m.createResource(m.expandPrefix(":my"));
		APISpec s = SpecUtil.specFrom( root );
		assertEquals( Boolean.FALSE, s.getEnableCounting() );
	}
	
	@Test public void testSpecEnableCountingTRUE() {
		Model m = ModelIOUtils.modelFromTurtle
			( ":my a api:API; elda:enableCounting true; api:sparqlEndpoint :spoo" );
		Resource root = m.createResource(m.expandPrefix(":my"));
		APISpec s = SpecUtil.specFrom( root );
		assertEquals( Boolean.TRUE, s.getEnableCounting() );
	}
	
	@Test public void testSpecEnableCountingYes() {
		Model m = ModelIOUtils.modelFromTurtle
			( ":my a api:API; elda:enableCounting 'yes'; api:sparqlEndpoint :spoo" );
		Resource root = m.createResource(m.expandPrefix(":my"));
		APISpec s = SpecUtil.specFrom( root );
		assertEquals( Boolean.TRUE, s.getEnableCounting() );
	}
	
	@Test public void testSpecEnableCountingFALSE() {
		Model m = ModelIOUtils.modelFromTurtle
			( ":my a api:API; elda:enableCounting false; api:sparqlEndpoint :spoo" );
		Resource root = m.createResource(m.expandPrefix(":my"));
		APISpec s = SpecUtil.specFrom( root );
		assertEquals( Boolean.FALSE, s.getEnableCounting() );
	}
	
	@Test public void testSpecEnableCountingNo() {
		Model m = ModelIOUtils.modelFromTurtle
			( ":my a api:API; elda:enableCounting 'no'; api:sparqlEndpoint :spoo" );
		Resource root = m.createResource(m.expandPrefix(":my"));
		APISpec s = SpecUtil.specFrom( root );
		assertEquals( Boolean.FALSE, s.getEnableCounting() );
	}
	
	@Test public void testSpecEnableCountingOptional() {
		Model m = ModelIOUtils.modelFromTurtle
			( ":my a api:API; elda:enableCounting 'optional'; api:sparqlEndpoint :spoo" );
		Resource root = m.createResource(m.expandPrefix(":my"));
		APISpec s = SpecUtil.specFrom( root );
		assertNull( s.getEnableCounting() );
	}
	
	// test that endpoints inherit counting from parent specs
	
	@Test public void testEndpointEnableCountingFromSpecDefault() {
		Model m = ModelIOUtils.modelFromTurtle
			( ":my a api:API; api:sparqlEndpoint :spoo; api:endpoint :ep. :ep a api:ListEndpoint; api:uriTemplate 'ep'" );
		Resource root = m.createResource(m.expandPrefix(":my"));
		APISpec s = SpecUtil.specFrom( root );
		APIEndpointSpec e = s.getEndpoints().get(0);
		assertEquals( Boolean.FALSE, e.getEnableCounting() );
	}
	
	@Test public void testEndpointEnableCountingFromSpecTrue() {
		Model m = ModelIOUtils.modelFromTurtle
			( ":my a api:API; elda:enableCounting true; api:sparqlEndpoint :spoo; api:endpoint :ep. :ep a api:ListEndpoint; api:uriTemplate 'ep'" );
		Resource root = m.createResource(m.expandPrefix(":my"));
		APISpec s = SpecUtil.specFrom( root );
		APIEndpointSpec e = s.getEndpoints().get(0);
		assertEquals( Boolean.TRUE, e.getEnableCounting() );
	}
	
	@Test public void testEndpointEnableCountingFromSpecFalse() {
		Model m = ModelIOUtils.modelFromTurtle
			( ":my a api:API; elda:enableCounting false; api:sparqlEndpoint :spoo; api:endpoint :ep. :ep a api:ListEndpoint; api:uriTemplate 'ep'" );
		Resource root = m.createResource(m.expandPrefix(":my"));
		APISpec s = SpecUtil.specFrom( root );
		APIEndpointSpec e = s.getEndpoints().get(0);
		assertEquals( Boolean.FALSE, e.getEnableCounting() );
	}
	
	@Test public void testEndpointEnableCountingFromOptional() {
		Model m = ModelIOUtils.modelFromTurtle
			( ":my a api:API; elda:enableCounting 'optional'; api:sparqlEndpoint :spoo; api:endpoint :ep. :ep a api:ListEndpoint; api:uriTemplate 'ep'" );
		Resource root = m.createResource(m.expandPrefix(":my"));
		APISpec s = SpecUtil.specFrom( root );
		APIEndpointSpec e = s.getEndpoints().get(0);
		assertNull( e.getEnableCounting() );
	}
	
	// test that endpoints override parent specs
	
	@Test public void testEndpointOveridesEnableCountingWithTrue() {
		Model m = modelFrom
			( ":my a api:API"
			, "; elda:enableCounting 'optional'"
			, "; api:sparqlEndpoint :spoo"
			, "; api:endpoint :ep"
			, "."
			, ":ep a api:ListEndpoint"
			, "; api:uriTemplate 'ep'"
			, "; elda:enableCounting true"
			);
		Resource root = m.createResource(m.expandPrefix(":my"));
		APISpec s = SpecUtil.specFrom( root );
		APIEndpointSpec e = s.getEndpoints().get(0);
		assertEquals( Boolean.TRUE, e.getEnableCounting() );
	}
	
	@Test public void testEndpointOveridesFalseEnableCountingWithTrue() {
		Model m = modelFrom
			( ":my a api:API"
			, "; elda:enableCounting false"
			, "; api:sparqlEndpoint :spoo"
			, "; api:endpoint :ep"
			, "."
			, ":ep a api:ListEndpoint"
			, "; api:uriTemplate 'ep'"
			, "; elda:enableCounting true"
			);
		Resource root = m.createResource(m.expandPrefix(":my"));
		APISpec s = SpecUtil.specFrom( root );
		APIEndpointSpec e = s.getEndpoints().get(0);
		assertEquals( Boolean.TRUE, e.getEnableCounting() );
	}
	
	@Test public void testEndpointOveridesTrueEnableCountingWithFalse() {
		Model m = modelFrom
			( ":my a api:API"
			, "; elda:enableCounting true"
			, "; api:sparqlEndpoint :spoo"
			, "; api:endpoint :ep"
			, "."
			, ":ep a api:ListEndpoint"
			, "; api:uriTemplate 'ep'"
			, "; elda:enableCounting false"
			);
		Resource root = m.createResource(m.expandPrefix(":my"));
		APISpec s = SpecUtil.specFrom( root );
		APIEndpointSpec e = s.getEndpoints().get(0);
		assertEquals( Boolean.FALSE, e.getEnableCounting() );
	}
	
	@Test public void testEndpointOveridesTrueEnableCountingWithOptional() {
		Model m = modelFrom
			( ":my a api:API"
			, "; elda:enableCounting true"
			, "; api:sparqlEndpoint :spoo"
			, "; api:endpoint :ep"
			, "."
			, ":ep a api:ListEndpoint"
			, "; api:uriTemplate 'ep'"
			, "; elda:enableCounting 'optional'"
			);
		Resource root = m.createResource(m.expandPrefix(":my"));
		APISpec s = SpecUtil.specFrom( root );
		APIEndpointSpec e = s.getEndpoints().get(0);
		assertNull( e.getEnableCounting() );
	}
	
	@Test public void testEndpointOveridesEnableCountingWithFalse() {
		Model m = modelFrom
			( ":my a api:API"
			, "; elda:enableCounting 'optional'"
			, "; api:sparqlEndpoint :spoo"
			, "; api:endpoint :ep"
			, "."
			, ":ep a api:ListEndpoint"
			, "; api:uriTemplate 'ep'"
			, "; elda:enableCounting false"
			);
		Resource root = m.createResource(m.expandPrefix(":my"));
		APISpec s = SpecUtil.specFrom( root );
		APIEndpointSpec e = s.getEndpoints().get(0);
		assertEquals( Boolean.FALSE, e.getEnableCounting() );
	}
	
	@Test public void testEndpointOveridesEnableCountingWithOptional() {
		Model m = modelFrom
			( ":my a api:API"
			, "; elda:enableCounting 'optional'"
			, "; api:sparqlEndpoint :spoo"
			, "; api:endpoint :ep"
			, "."
			, ":ep a api:ListEndpoint"
			, "; api:uriTemplate 'ep'"
			, "; elda:enableCounting 'optional'"
			);
		Resource root = m.createResource(m.expandPrefix(":my"));
		APISpec s = SpecUtil.specFrom( root );
		APIEndpointSpec e = s.getEndpoints().get(0);
		assertNull( e.getEnableCounting() );
	}
	
	private Model modelFrom(String... elements) {
		StringBuilder sb = new StringBuilder();
		for (String e: elements) sb.append("\n").append( e );
		return ModelIOUtils.modelFromTurtle( sb.toString() );
	}
	
}
