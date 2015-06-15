package com.epimorphics.lda.testing.tomcat;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MediaType;

import org.junit.Test;

import com.epimorphics.lda.testing.utils.TestUtil;
import com.epimorphics.lda.testing.utils.TomcatTestBase;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.lda.vocabularies.SPARQL;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.RDF;
import com.sun.jersey.api.client.ClientResponse;

public class TestViewIsUsingValues extends TomcatTestBase{

	@Override public String getWebappRoot() {
		return "src/main/webapp";
	}

	// TODO tidy up a lot, this is the first pass.
	static final MediaType typeTurtle = new MediaType("text", "turtle");
	
	/**
		Test that the view query has a VALUES clause that mentions exactly
		the same items as are the selected items in the model. This assumes
		that the view query, as it appears in the metadata, is an accurate
		representation of the view (remember that Elda may need up to 3
		queries to compute a view).
	<p>
		This doesn't demonstrate that the answer (model) is correct, only that
		the view uses the same set of URIs as ere selected.
	*/
	@Test public void viewsUseValues() {
		ClientResponse response = getResponse(BASE_URL + "testing/games?_metadata=all&_view=basic", "text/turtle");
		assertEquals(200, response.getStatus());
		assertTrue(response.getType().isCompatible(typeTurtle));
		String entity = response.getEntity(String.class);
		Model result = TestUtil.modelFromTurtle(entity);
	//
		List<RDFNode> selectedItems = result.listObjectsOfProperty(API.items).next().as(RDFList.class).asJavaList();		
	//
		Resource anExecution = result.listObjectsOfProperty(API.wasResultOf).nextNode().asResource();
		String viewQuery = anExecution
			.getProperty(API.viewingResult).getResource()
			.getProperty(SPARQL.query).getResource()
			.getProperty(RDF.value).getString()
			;
	//
		PrefixMapping pm = extractPrefixes(viewQuery);
	//	
		Matcher m = valuesExtractionPattern.matcher(viewQuery);
		assertTrue("View query should contain a VALUES clause.", m.find());
	//		
		String[] items = m.group(1).split("\n");
		List<Resource> valuesItems = new ArrayList<Resource>();
		for (String item: items) {
			valuesItems.add(result.createResource(pm.expandPrefix(item.trim())));
		}
	//
		assertEquals(valuesItems.size(), selectedItems.size());	
		assertEquals(new HashSet<RDFNode>(valuesItems), new HashSet<RDFNode>(selectedItems));
	}

	// pattern to extract the content of a VALUES clause from a query.
	static final Pattern valuesExtractionPattern = Pattern.compile
		("\\{ VALUES \\?item \\{ \n([^}]*)\\} \\}", Pattern.DOTALL);

	// patter to recognise prefix declaractions from a query.
	static final Pattern extractPrefixPattern = Pattern.compile
		("^PREFIX *([^:]*): *<(.*)>");
	
	private PrefixMapping extractPrefixes(String viewQuery) {
		PrefixMapping result = PrefixMapping.Factory.create();
		for (String line: viewQuery.split("\n")) {
			Matcher m = extractPrefixPattern.matcher(line);
			if(m.find()) result.setNsPrefix(m.group(1), m.group(2));
		}
		return result;
	}

}
