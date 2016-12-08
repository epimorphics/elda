package com.epimorphics.lda.cache.tests;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.cache.Cache;
import com.epimorphics.lda.core.APIEndpoint.Request;
import com.epimorphics.lda.core.*;
import com.epimorphics.lda.shortnames.CompleteContext.Mode;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.support.Controls;
import com.epimorphics.lda.support.NoteBoard;
import com.epimorphics.lda.tests_support.LoadsNothing;
import com.epimorphics.util.CollectionUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.util.FileManager;

public class TestSharedCacheEntries {
	
	final String specString = build
		( ":spec a api:API"
		, "  ; api:sparqlEndpoint <local:src/test/resources/datasets/use-graph-testing.ttl,...use-graph-testing-A.ttl,...use-graph-testing-B.ttl>"
		, "  ; api:endpoint :endpoint"
		, "  ; api:variable [api:name '_stripCacheKey'; api:value 'true']"
		, "  ; api:variable [api:name '_cacheOnlyObjectData'; api:value 'true']"
		, "  ."
		, ""
		, ":endpoint a api:ListEndpoint"
		, "  ; api:uriTemplate '/endpoint'"
		, "  ."
		);
	
	final Model specModel = ModelIOUtils.modelFromTurtle(specString);
	
	Resource specRoot = specModel.createResource( specModel.expandPrefix( ":spec" ) );
	Resource endpoint = specModel.createResource( specModel.expandPrefix( ":endpoint" ) );

	/**
	    Test that requesting the same URI returns a cached result and that
	    requesting a semantically different URI doesn't.
	    
	    This isn't a very strong test.
	 
	*/
	@Test public void testShared() throws URISyntaxException {
		APIEndpointImpl ep = createEndpoint();
		
		Request req_1 = createRequest(ep, "json", new URI("http://example.org/elda/endpoint"));
		ResponseResult res_1 = ep.call(req_1, new NoteBoard());
		
		assertFalse(res_1.isFromCache);
		
		Request req_2 = createRequest(ep, "json", new URI("http://example.org/elda/endpoint"));
		ResponseResult res_2 = ep.call(req_2, new NoteBoard());
		
		assertTrue(res_2.isFromCache);
		
		Model model_1 = res_1.resultSet.getModels().getObjectModel();
		Model model_2 = res_2.resultSet.getModels().getObjectModel();
		ModelTestBase.assertIsoModels(model_1, model_2);
		
		Request req_3 = createRequest(ep, "ttl", new URI("http://example.org/elda/endpoint.ttl"));
		ResponseResult res_3 = ep.call(req_3, new NoteBoard());
		assertTrue(res_3.isFromCache);
		
		Model model_3 = res_3.resultSet.getModels().getObjectModel();
		ModelTestBase.assertIsoModels(model_1, model_3);
		
		Request req_4 = createRequest(ep, "xml", new URI("http://example.org/elda/endpoint?_format=xml"));
		ResponseResult res_4 = ep.call(req_4, new NoteBoard());
		assertTrue(res_4.isFromCache);
		
		Model model_4 = res_4.resultSet.getModels().getObjectModel();
		ModelTestBase.assertIsoModels(model_1, model_4);
		
		Request req_5= createRequest(ep, "json", new URI("http://example.org/elda/endpoint?_page=2"));
		ResponseResult res_5 = ep.call(req_5, new NoteBoard());
		assertFalse(res_5.isFromCache);
	}

	private Request createRequest(APIEndpointImpl ep, String format, URI ru) throws URISyntaxException {
		List<String> formatNames = CollectionUtils.list("ttl", "xml", "json", "html");
		Controls c = new Controls();
    	Bindings b = ep.getSpec().getBindings().copy();
		return new APIEndpoint.Request( c, ru, b )
        	.withFormats( formatNames, format )
        	.withMode( Mode.PreferLocalnames )
        	;
	}

	private APIEndpointImpl createEndpoint() {
		FileManager fm = FileManager.get();
		ModelLoader ml = LoadsNothing.instance;
		APISpec api = new APISpec( fm, specRoot, ml );
		APIEndpointSpec spec = new APIEndpointSpec(api, api, endpoint);
		APIEndpointImpl ep = new APIEndpointImpl(spec);
		return ep;
	}
	
	private String build(String ...strings) {
		StringBuilder result = new StringBuilder();
		for (String s: strings) result.append(s).append("\n");
		return result.toString();
	}
}
