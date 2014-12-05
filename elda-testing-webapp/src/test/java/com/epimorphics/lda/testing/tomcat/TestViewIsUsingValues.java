package com.epimorphics.lda.testing.tomcat;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MediaType;

import org.junit.Test;

import com.epimorphics.lda.testing.utils.TomcatTestBase;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.lda.vocabularies.SPARQL;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.RDF;
import com.sun.jersey.api.client.ClientResponse;

public class TestViewIsUsingValues extends TomcatTestBase{

	@Override public String getWebappRoot() {
		return "src/test/webapp";
	}

	// TODO tidy up a lot, this is the first pass.
	static final MediaType typeTurtle = new MediaType("text", "turtle");
	
	@Test public void testme() {
		ClientResponse response = getResponse(BASE_URL + "testing/games?_metadata=all&_view=basic", "text/turtle");
		assertEquals(200, response.getStatus());
		assertTrue(response.getType().isCompatible(typeTurtle));
		String entity = response.getEntity(String.class);
		Model result = modelFromTurtle(entity);
	//
		
		List<RDFNode> spog = result.listObjectsOfProperty(API.items).next().as(RDFList.class).asJavaList();		
		
		Resource anExecution = result.listObjectsOfProperty(API.wasResultOf).nextNode().asResource();
		String viewQuery = anExecution
			.getProperty(API.viewingResult).getResource()
			.getProperty(SPARQL.query).getResource()
			.getProperty(RDF.value).getString()
			;
			
		Pattern p = Pattern.compile("\\{ VALUES \\?item \\{ \n([^}]*)\\} \\}", Pattern.DOTALL);
		Matcher m = p.matcher(viewQuery);
		assertTrue("View query doesn't contain the VALUES clause.", m.find());
		
		PrefixMapping pm = PrefixMapping.Factory.create();
		for (String line: viewQuery.split("\n")) {
			Pattern q = Pattern.compile("^PREFIX *([^:]*): *<(.*)>");
			Matcher n = q.matcher(line);
			if(n.find()) pm.setNsPrefix(n.group(1), n.group(2));
		}
		
		String[] items = m.group(1).split("\n");
		List<Resource> valuesItems = new ArrayList<Resource>();
		for (String item: items) {
			valuesItems.add(result.createResource(pm.expandPrefix(item.trim())));
		}
		
		assertEquals(valuesItems.size(), spog.size());
		
//		for (int i = 0; i < spog.size(); i += 1) {
//			int W = 150;
//			RDFNode S = spog.get(i);
//			Resource V = valuesItems.get(i);
//			int V_length = V.getURI().length();
//			int S_length = S.asResource().getURI().length();
//			// System.err.println(">> V " + V_length + ", S " + S_length);
//			int G = W - S_length - V_length;
//			String dots = "................................................".substring(0, G);
//			System.err.println("| " + S + " " + dots + " " + V + " |");
//		}
		
		assertEquals(new HashSet<RDFNode>(valuesItems), new HashSet<RDFNode>(spog));
	}

	public static Model modelFromTurtle(String ttl) {
		Model model = ModelFactory.createDefaultModel();
		return model.read( new StringReader(ttl), null, "Turtle");
	}

}
