package com.epimorphics.lda.views.tests;

import java.util.List;
import org.junit.Test;

import com.epimorphics.lda.core.VarSupply;
import com.epimorphics.lda.core.View;
import com.epimorphics.lda.sources.Source;

import static com.epimorphics.jsonrdf.utils.ModelIOUtils.modelFromTurtle;
import static com.epimorphics.util.CollectionUtils.*;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;

public class TestLabelQuery {
	
	static final String graphName = null;
	static final VarSupply vars = null;
	static final List<Source> sources = null;

	static final Model P1P2P3Model = modelFromTurtle
		( ":x :p1 'xp1'."
		+ "\n :x :p2 'xp2'."
		+ "\n :y :p1 'yp1'."
		+ "\n :z :p1 'zp1'."
		+ "\n :z :p2 'zp2'."
		+ "\n :z :p3 'zp3'."
		+ "\n"		
		);
	
	static final Model labels = modelFromTurtle
		(":x rdfs:label 'l1'. :y rdfs:label 'l2'. :z rdfs:label 'l3'.");
	
	static final Model dataModel = (Model) ModelFactory.createDefaultModel()
		.add(P1P2P3Model)
		.add(labels)
		.setNsPrefixes(P1P2P3Model)
		;
	
	String p1 = dataModel.expandPrefix(":p1");
	String p2 = dataModel.expandPrefix(":p2");
	String p3 = dataModel.expandPrefix(":p3");
	
	Resource x = dataModel.createResource(dataModel.expandPrefix(":x"));
	Resource y = dataModel.createResource(dataModel.expandPrefix(":y"));
	Resource z = dataModel.createResource(dataModel.expandPrefix(":y"));
	
	@Test public void fetchLabels() {
		
		testBuildFetchLabelQuery(
			list(p1)
			, modelFromTurtle(":x :p1 'xp1'. :y :p1 'yp1'. :z :p1 'zp1'.")
		);
		
		testBuildFetchLabelQuery(
			list(p2)
			, modelFromTurtle(":x :p2 'xp2'. :z :p2 'zp2'")
		);
		
		testBuildFetchLabelQuery(
			list(p3)
			, modelFromTurtle(":z :p3 'zp3'.")
		);
		
		testBuildFetchLabelQuery(
			list(p1, p2)
			, modelFromTurtle(":x :p1 'xp1'; :p2 'xp2'. :y :p1 'yp1'. :z :p1 'zp1'; :p2 'zp2'.")
		);
		
		testBuildFetchLabelQuery(
			list(p1, p3)
			, modelFromTurtle(":x :p1 'xp1'. :y :p1 'yp1'. :z :p1 'zp1'; :p3 'zp3'.")
		);
		
		testBuildFetchLabelQuery(
			list(p2, p3)
			, modelFromTurtle(":x :p2 'xp2'. :z :p2 'zp2'; :p3 'zp3'.")
		);
		
		testBuildFetchLabelQuery(
			list(p1, p2, p3)
			, P1P2P3Model
		);
	}

	private void testBuildFetchLabelQuery(List<String> properties, Model expected) {
		Model m = modelFromTurtle(":it :p :x, :y, :z");
		View.State s = new View.State(null, m, null, null, null);
		String queryString = View.buildFetchLabelsQuery(s, properties);
		Query q = QueryFactory.create(queryString);
		QueryExecution qx = QueryExecutionFactory.create(q, dataModel);
		Model resultModel = qx.execConstruct();
		ModelTestBase.assertIsoModels(expected, resultModel);
	}
	
}
