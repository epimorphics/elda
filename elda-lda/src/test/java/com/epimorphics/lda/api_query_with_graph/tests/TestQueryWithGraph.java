package com.epimorphics.lda.api_query_with_graph.tests;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.*;
import com.epimorphics.lda.routing.Match;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.support.*;
import com.epimorphics.lda.tests_support.LoadsNothing;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.util.FileManager;

public class TestQueryWithGraph {

	final String specString = build
		( ":spec a api:API"
		, "  ; api:sparqlEndpoint <local:src/test/resources/datasets/use-graph-testing.ttl,...use-graph-testing-A.ttl,...use-graph-testing-B.ttl>"
		, "  ; api:endpoint :endpoint"
		, "  ."
		, ""
		, "<local:src/test/resources/datasets/use-graph-testing.ttl,.../use-graph-testing-A.ttl,.../use-graph-testing-B.ttl> elda:supportsNestedSelect true"
		, "."
		, ""
		, ":endpoint a api:ListEndpoint"
		, "  ; api:uriTemplate '/endpoint'"
		, "  ."
		);
	
	final Model specModel = ModelIOUtils.modelFromTurtle(specString);
	
	Resource specRoot = specModel.createResource( specModel.expandPrefix( ":spec" ) );
	Resource endpoint = specModel.createResource( specModel.expandPrefix( ":endpoint" ) );

	@Test public void testWithoutGraph() throws URISyntaxException {
		String graphName = null;
		Model expected = ModelIOUtils.modelFromTurtle( ":Z :P :Z" );
		testWithGraph(graphName, expected);
	}
	
	@Test public void testWithGraph_A() throws URISyntaxException {
		String graphName = "file:///src/test/resources/datasets/use-graph-testing-A.ttl";
		Model expected = ModelIOUtils.modelFromTurtle( ":A :P :A" );
		testWithGraph(graphName, expected);
	}
	
	@Test public void testWithGraph_B() throws URISyntaxException {
		String graphName = "file:///src/test/resources/datasets/use-graph-testing-B.ttl";
		Model expected = ModelIOUtils.modelFromTurtle( ":B :P :B" );
		testWithGraph(graphName, expected);
	}

	private void testWithGraph(String graphName, Model expected) throws URISyntaxException {
		FileManager fm = FileManager.get();
		ModelLoader ml = LoadsNothing.instance;
		APISpec api = new APISpec( fm, specRoot, ml );
		
		APIEndpointSpec spec = new APIEndpointSpec(api, api, endpoint);
		
		Map<String, String> settings = new HashMap<String, String>();
		URI requestURI = new URI("http://localhost:8080/path#" + graphName);
		Bindings context = new Bindings();
		Controls c = new Controls();
		APIEndpoint.Request r = new APIEndpoint.Request(c, requestURI, context);
		NoteBoard nb = new NoteBoard();
		APIEndpoint ep = new APIEndpointImpl(spec);
		Match m = new Match("path", ep, settings);
		String contextPath = "path";
		
		MultiMap<String, String> qp = new MultiMap<String, String>();
		if (graphName != null) qp.add("_graph", graphName);
		
		ResponseResult rr = APIEndpointUtil.call(r, nb, m, contextPath, qp );
		Model rm = rr.resultSet.getModels().getObjectModel();
		ModelTestBase.assertIsoModels(expected, rm);
	}

	private String build(String ...strings) {
		StringBuilder result = new StringBuilder();
		for (String s: strings) result.append(s).append("\n");
		return result.toString();
	}	
}
