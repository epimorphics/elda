package com.epimorphics.lda.api_query_with_graph.tests;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.*;
import com.epimorphics.lda.core.APIEndpoint.Request;
import com.epimorphics.lda.renderers.Renderer;
import com.epimorphics.lda.routing.Match;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.support.*;
import com.epimorphics.lda.tests_support.LoadsNothing;
import com.epimorphics.util.MediaType;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;

public class TestQueryWithGraph {

	final String specString = build
		( ":spec a api:API"
		, "  ; api:sparqlEndpoint <local:file-to-find>"
		, "  ; api:endpoint :endpoint"
		, "  ."
		, ""
		, ":endpoint a api:ListEndpoint"
		, "  ; api:uriTemplate '/endpoint'"
		, "  ."
		);
	
	final Model spec = ModelIOUtils.modelFromTurtle(specString);
	
	Resource specRoot = spec.createResource( spec.expandPrefix( ":spec" ) );
	Resource endpoint = spec.createResource( spec.expandPrefix( ":endpoint" ) );

	@Test @Ignore public void testWithGraph() throws URISyntaxException {
				
		FileManager fm = FileManager.get();
		ModelLoader ml = LoadsNothing.instance;
		APISpec api = new APISpec( fm, specRoot, ml );
		
		APIEndpointSpec spec = new APIEndpointSpec(api, api, endpoint);
		
		Map<String, String> settings = new HashMap<String, String>();
		URI requestURI = new URI("http://localhost:8080/path");
		Bindings context = new Bindings();
		Controls c = new Controls();
		APIEndpoint.Request r = new APIEndpoint.Request(c, requestURI, context);
		NoteBoard nb = new NoteBoard();
		APIEndpoint ep = new APIEndpointImpl(spec);
		Match m = new Match("path", ep, settings);
		String contextPath = "path";
		MultiMap<String, String> qp = new MultiMap<String, String>();
		
		ResponseResult rr = APIEndpointUtil.call(r, nb, m, contextPath, qp );
		
		Model rm = rr.resultSet.getMergedModel();
		
		// check that the model is the right one.
	}

	private String build(String ...strings) {
		StringBuilder result = new StringBuilder();
		for (String s: strings) result.append(s).append("\n");
		return result.toString();
	}
	

	
	
}
