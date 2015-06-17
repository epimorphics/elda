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
import com.hp.hpl.jena.util.FileManager;

public class TestSharedCacheEntries {
	
	final String specString = build
		( ":spec a api:API"
		, "  ; api:sparqlEndpoint <local:src/test/resources/datasets/use-graph-testing.ttl,...use-graph-testing-A.ttl,...use-graph-testing-B.ttl>"
		, "  ; api:endpoint :endpoint"
		, "  ."
		, ""
		, ":endpoint a api:ListEndpoint"
		, "  ; api:uriTemplate '/endpoint'"
		, "  ."
		);
	
	final Model specModel = ModelIOUtils.modelFromTurtle(specString);
	
	Resource specRoot = specModel.createResource( specModel.expandPrefix( ":spec" ) );
	Resource endpoint = specModel.createResource( specModel.expandPrefix( ":endpoint" ) );

	@Test public void testShared() throws URISyntaxException {
		
		FileManager fm = FileManager.get();
		ModelLoader ml = LoadsNothing.instance;
		APISpec api = new APISpec( fm, specRoot, ml );
		
		URI ru = new URI("http://example.org/elda/endpoint");
		
		APIEndpointSpec spec = new APIEndpointSpec(api, api, endpoint);
		
		APIEndpointImpl ep = new APIEndpointImpl(spec);
		
		NoteBoard nb = new NoteBoard();
		
		List<String> formatNames = CollectionUtils.list("");
		
		String formatName = "json";
		
		Controls c = new Controls();
		
    	Bindings b = ep.getSpec().getBindings().copy();
		
		Request req = new APIEndpoint.Request( c, ru, b )
        	.withFormats( formatNames, formatName )
        	.withMode( Mode.PreferLocalnames )
        	;
		
		ResponseResult res = ep.call(req, nb);
		
		assertFalse(res.isFromCache);
		
	}
	
	private String build(String ...strings) {
		StringBuilder result = new StringBuilder();
		for (String s: strings) result.append(s).append("\n");
		return result.toString();
	}
}
