package com.epimorphics.lda.views.tests;

import static org.junit.Assert.fail;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.apispec.tests.SpecUtil;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.*;
import com.epimorphics.lda.routing.MatchSearcher;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.support.*;
import com.epimorphics.lda.tests_support.MakeData;
import com.epimorphics.util.Triad;
import com.epimorphics.util.URIUtils;
import com.hp.hpl.jena.rdf.model.*;

public class TestAPITemplate {
	
	@Test public void testDefaultTemplateInSpec() {
		testTemplate
			( ""
			, "; api:defaultViewer [api:template '?item :predicate ?v']" 
			, ""
			);
	}
	
	@Test public void testDefaultTemplateInEndpoint() {
		testTemplate
			( ""
			, ""
			, "; api:defaultViewer [api:template '?item :predicate ?v']" 
			);
	}
	
	@Test public void testNamedTemplateInSpec() {
		testTemplate
			( "_view=NAMED"
			, "; api:viewer [api:name 'NAMED'; api:template '?item :predicate ?v']" 
			, ""
			);
	}
	
	@Test public void testNamedTemplateInEndpoint() {
		testTemplate
			( "_view=NAMED"
			, ""
			, "; api:viewer [api:name 'NAMED'; api:template '?item :predicate ?v']" 
			);
	}
	
	/**
		Ensure that running the configured endpoint correctly exposes in
		the view the :predicate item from the model but not the :catiprede.
		
		<p><code>params</code> may be applied to the query part of the URL
		to select a specific view. <code>inSpec</code> is a Turtle fragment
		inserted into the API declaration. <code>inEndpoint</code> is a
		Turtle fragment inserted into the endpoint declaration.
	*/
	public void testTemplate( String params, String inSpec, String inEndpoint ) {
		
		String eh_prefix = "@prefix : <eh:/>.";
		
		Model specModel = modelFrom
			(
			eh_prefix
			, "@prefix api:     <http://purl.org/linked-data/api/vocab#> ."
			, "@prefix dc:      <http://purl.org/dc/elements/1.1/> ."
			, "@prefix geo:     <http://www.w3.org/2003/01/geo/wgs84_pos#> ."
			, "@prefix owl:     <http://www.w3.org/2002/07/owl#> ."
			, "@prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ."
			, "@prefix xsd:     <http://www.w3.org/2001/XMLSchema#> ."
			, "@prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> ."
			, "@prefix foaf:    <http://xmlns.com/foaf/0.1/> ."
			, "@prefix school:  <http://education.data.gov.uk/def/school/> ."
			, "@prefix skos:    <http://www.w3.org/2004/02/skos/core#> ."
		//
			, ":root a api:API"
			, "; api:sparqlEndpoint <here:dataPart>"
			, inSpec
			, "; api:endpoint :ep"
			, "."
		//
			, ":ep a api:ListEndpoint"
			, "; api:uriTemplate '/this'"
			, inEndpoint
			, "; api:selector [ api:filter 'type=Item' ]" 
			, "."
		//
			, "<here:dataPart> :elements :A, :B"
			, "."
		//
			, ":A a :Item; :predicate :X."
			, ":B a :Item; :catiprede :Y."
		//
			, ":Item a rdfs:Class; rdfs:label 'Item'"
			, "."
			);

		Resource root = specModel.createResource( specModel.expandPrefix( ":root" ) );
		
		APISpec spec = SpecUtil.specFrom( root );
				
		APIEndpoint ep = new APIEndpointImpl( spec.getEndpoints().get(0) );
		Bindings epBindings = ep.getSpec().getBindings();
		MultiMap<String, String> map = MakeData.parseQueryString( params );
		URI ru = URIUtils.newURI( "/this" );
		Bindings cc = Bindings.createContext( bindTemplate( epBindings, "/this", "/path", map ), map );
		ResponseResult resultsAndFormat = ep.call( new APIEndpoint.Request( controls, ru, cc ), new NoteBoard() );
		Model rsm = resultsAndFormat.resultSet.getMergedModel();
		
		Model obtained = ModelFactory.createDefaultModel();
		obtained.add( rsm );
		
		assertHas( obtained, eh_prefix, ":A :predicate :X." );
		assertHasnt( obtained, eh_prefix, ":B :catiprede :Y." );		
	}	
	
	private void assertHas( Model obtained, String ... lines ) {
		Model wanted = modelFrom( lines );
		for (Statement s: wanted.listStatements().toList())
			if (!obtained.contains( s ))
				fail("missing required statement: " + s );
	}	
	
	private void assertHasnt( Model obtained, String ... lines ) {
		Model wanted = modelFrom( lines );
		for (Statement s: wanted.listStatements().toList())
			if (obtained.contains( s ))
				fail("has prohibited statement: " + s );
	}

	private Bindings bindTemplate( Bindings epBindings, String template, String path, MultiMap<String, String> qp ) {
		MatchSearcher<String> ms = new MatchSearcher<String>();
		ms.register( template, "IGNORED" );
		Map<String, String> bindings = new HashMap<String, String>();
		ms.lookup( bindings, path, qp );
		return epBindings.updateAll( bindings ); 
	}
	
	static final Controls controls = new Controls( true, new Times() );

	private Model modelFrom(String ... lines) {
		StringBuilder ttl = new StringBuilder();
		for (String line: lines) ttl.append( line ).append( '\n' );
		return ModelIOUtils.modelFromTurtle( ttl.toString() );
	}
}
