/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2015 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.apispec.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.specs.APISpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
	If an API Spec defines a default formatter, then it should bind
	_defaultSuffix to the name of that formatter; otherwise it should
	be unbound.
	
	If an API Endpoint Spec defaults a default formatter, then it should
	bind _defaultSuffix to the name of that formatter; otherwise it should
	be bound to whatever its parent Spec binds it to.
*/
public class TestBindsDefaultFormatterSuffix {

	@Test public void requireNoDefaultFormatterMeansNoDefaultSuffix() {
		testSpecDefaultSuffix(null, "");
	}
	
	@Test public void requireOneDefaultFormatterMeansThatDefaultSuffix() {
		testSpecDefaultSuffix
			( "xyzzy"
			, ":my api:defaultFormatter [a elda:VelocityFormatter; api:mimeType 'text/plain'; api:name 'xyzzy']."
			);
	}
	
	@Test public void requireOneDefaultFormatterMeansThatDefaultSuffixWithEndpointFormatters() {
		testSpecDefaultSuffix
			( "xyzzy"
			, ":my api:defaultFormatter [a elda:VelocityFormatter; api:mimeType 'text/plain'; api:name 'xyzzy']."
			  + "\n:my api:endpoint :A."
			  + "\n:A a api:ListEndpoint; api:uriTemplate '/spoo'."
			  + "\n:A api:defaultFormatter [a elda:VelocityFormatter; api:mimeType 'text/plain'; api:name 'plugh']."
			);
	}
	
	@Test public void requireEndpointDefaultFormatterMeansThatDefaultSuffixWithEndpointFormatters() {
		testEndpointDefaultSuffix
			( "plugh"
			, ":my api:defaultFormatter [a elda:VelocityFormatter; api:mimeType 'text/plain'; api:name 'xyzzy']."
			  + "\n:my api:endpoint :A."
			  + "\n:A a api:ListEndpoint; api:uriTemplate '/spoo'."
			  + "\n:A api:defaultFormatter [a elda:VelocityFormatter; api:mimeType 'text/plain'; api:name 'plugh']."
			);
	}
	
	@Test public void requireEndpointDefaultFormatterInheritsSpecDefaultSuffix() {
		testEndpointDefaultSuffix
			( "xyzzy"
			, ":my api:defaultFormatter [a elda:VelocityFormatter; api:mimeType 'text/plain'; api:name 'xyzzy']."
			  + "\n:my api:endpoint :A."
			  + "\n:A a api:ListEndpoint; api:uriTemplate '/spoo'."
			);
	}
	
	@Test public void requireEachEndpointHasItsOwnDefaultSuffix() {
		EachEndpointHasItsOwnDefaultSuffix
			( "xyzzy"
			, ":my api:defaultFormatter [a elda:VelocityFormatter; api:mimeType 'text/plain'; api:name 'xyzzy']."
			  + "\n:my api:endpoint :plugh, :flarn."
			  + "\n:plugh a api:ListEndpoint; api:uriTemplate '/spoo'."	
			  + "\n:plugh api:defaultFormatter [a elda:VelocityFormatter; api:mimeType 'text/plain'; api:name 'plugh']."
			  + "\n:flarn a api:ListEndpoint; api:uriTemplate '/spoo'."	
			  + "\n:flarn api:defaultFormatter [a elda:VelocityFormatter; api:mimeType 'text/plain'; api:name 'flarn']."
			);
	}

	private void testSpecDefaultSuffix(String expected, String protoSpec ) {
		String spec = ":my a api:API; api:sparqlEndpoint :spoo." + "\n" + protoSpec;
		Model m = ModelIOUtils.modelFromTurtle( spec );
		Resource root = m.createResource( m.expandPrefix( ":my" ) );
		APISpec s = SpecUtil.specFrom( root );
		assertEquals(expected, s.bindings.getAsString("_defaultSuffix", null));
	}

	private void testEndpointDefaultSuffix(String expected, String protoSpec ) {
		String spec = ":my a api:API; api:sparqlEndpoint :spoo." + "\n" + protoSpec;
		Model m = ModelIOUtils.modelFromTurtle( spec );
		Resource root = m.createResource( m.expandPrefix( ":my" ) );
		APISpec s = SpecUtil.specFrom( root );
		APIEndpointSpec es = s.getEndpoints().get(0);
		assertEquals(expected, es.getBindings().getAsString("_defaultSuffix", null));
	}

	private void EachEndpointHasItsOwnDefaultSuffix(String expected, String protoSpec ) {
		String spec = ":my a api:API; api:sparqlEndpoint :spoo." + "\n" + protoSpec;
		Model m = ModelIOUtils.modelFromTurtle( spec );
		Resource root = m.createResource( m.expandPrefix( ":my" ) );
		APISpec s = SpecUtil.specFrom( root );
	//
		for (APIEndpointSpec es : s.getEndpoints()) {
			String localName = es.getResource().getLocalName();
			String defaultSuffix = es.getBindings().getAsString("_defaultSuffix", null);
			assertEquals("suffix should be the same as the local name", localName, defaultSuffix);
		}
	}
	
}
