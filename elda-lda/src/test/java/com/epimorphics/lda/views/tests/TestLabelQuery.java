package com.epimorphics.lda.views.tests;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.core.VarSupply;
import com.epimorphics.lda.core.View;
import com.epimorphics.lda.sources.Source;
import com.epimorphics.util.CollectionUtils;
import com.hp.hpl.jena.rdf.model.Model;

public class TestLabelQuery {
	
	static final String graphName = null;
	static final VarSupply vars = null;
	static final List<Source> sources = null;

	@Test public void testSingleLabelProperty() {
		Model m = ModelIOUtils.modelFromTurtle(":a :p :b");
		View.State s = new View.State(null, m, sources, vars, graphName);
		List<String> properties = CollectionUtils.list("fake:label");
		
		String result = View.buildFetchLabelsQuery(s, properties);
		
		if (!result.matches("(?s).*CONSTRUCT.*WHERE.*VALUES.*")) fail("overall shape wrong");
		
		String [] parts = result.split("WHERE");
		String cons = parts[0], select = parts[1];
		
		assertContains("?x <fake:label> ?l1.", cons);
		assertContains("?x <fake:label> ?l1.", select);
	}
	
	@Test public void testMultipleLabelProperties() {
		Model m = ModelIOUtils.modelFromTurtle(":a :p :b");
		View.State s = new View.State(null, m, sources, vars, graphName);
		List<String> properties = CollectionUtils.list("fake:label1", "fake:label2", "fake:label3");
		
		String result = View.buildFetchLabelsQuery(s, properties);
		
		if (!result.matches("(?s).*CONSTRUCT.*WHERE.*VALUES.*")) fail("overall shape wrong");
		
		String [] parts = result.split("WHERE");
		String cons = parts[0], select = parts[1];
		
		assertContains("?x <fake:label1> ?l1.", cons);
		assertContains("?x <fake:label1> ?l1.", select);
		assertContains("?x <fake:label2> ?l2.", cons);
		assertContains("?x <fake:label2> ?l2.", select);
		assertContains("?x <fake:label3> ?l3.", cons);
		assertContains("?x <fake:label3> ?l3.", select);
	}

	private void assertContains(String expected, String target) {
		if (!target.contains(expected)) {
			fail("expected '" + expected + "' within '" + target + "'");
		}
	}
	
}
