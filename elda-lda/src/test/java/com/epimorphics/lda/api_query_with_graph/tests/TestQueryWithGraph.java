package com.epimorphics.lda.api_query_with_graph.tests;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.*;
import com.epimorphics.lda.query.APIQuery;
import com.epimorphics.lda.query.APIQuery.QueryBasis;
import com.epimorphics.lda.query.tests.StubQueryBasis;
import com.epimorphics.lda.routing.Match;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.support.*;
import com.epimorphics.lda.tests.SNS;
import com.epimorphics.lda.tests_support.LoadsNothing;
import com.epimorphics.lda.tests_support.MakeData;
import com.epimorphics.lda.textsearch.TextSearchConfig;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.shared.PrefixMapping;
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
	
	@Test public void testExpansionOfGraphTemplate() {
		testExpansionOfGraphTemplate("straw{A}Man", null, v("A=VALUE"), "strawVALUEMan");
		testExpansionOfGraphTemplate("{A}B{C}", null, v("A=alpha C=camera"), "alphaBcamera");
	}
	
	@Test public void testExpansionOfGraphTemplateFrom_graph() {
		testExpansionOfGraphTemplate("straw{A}Man", "graphName", v("A=VALUE"), "graphName");
		testExpansionOfGraphTemplate("{A}B{C}", "A{B}C", v("A=alpha B=bonny C=camera"), "AbonnyC");
	}

	private void testExpansionOfGraphTemplate(final String template, String graphName, Bindings b, String expansion) {
		PrefixMapping p = PrefixMapping.Factory.create();
		ShortnameService sns = new SNS("");
		QueryBasis qb = new StubQueryBasis(sns) {
			
			public String getGraphTemplate() {
				return template;
			}
		};
		
		APIQuery q = new APIQuery(qb);
		if (graphName != null) q.setGraphName(graphName);
		String query = q.assembleSelectQuery(b, p);
		assertContains("GRAPH <" + expansion + "> {", query);
	}

	private void assertContains(String expected, String content) {
		if (content.indexOf(expected) < 0)
			Assert.fail("Expected to find " + expected + " in " + content);
	}

	private Bindings v(String bindings) {
		return MakeData.variables(bindings);
	}
	
	private String build(String ...strings) {
		StringBuilder result = new StringBuilder();
		for (String s: strings) result.append(s).append("\n");
		return result.toString();
	}	
}
