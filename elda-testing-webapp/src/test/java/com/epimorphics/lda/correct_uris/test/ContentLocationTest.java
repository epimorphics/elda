package com.epimorphics.lda.correct_uris.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.epimorphics.lda.testing.utils.TestUtil;
import com.epimorphics.lda.testing.utils.TomcatTestBase;
import com.epimorphics.lda.vocabularies.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;
import com.sun.jersey.api.client.ClientResponse;

/**
    Test that the content-location header tracks the chosen rendering format.
    Also test that the content-location has at least some of the meta-data 
    properties.
*/
public class ContentLocationTest extends TomcatTestBase {

	@Override public String getWebappRoot() {
		return "src/main/webapp";
	}
	
	@Test public void testContentLocationIncludesNegotiatedRenderer() {
		testContentLocationIncludesRenderer("games", "games.ttl", "?_metadata=all&_view=basic");
	}
	
	@Test public void testContentLocationIncludesExplicitTTL() {
		testContentLocationIncludesRenderer("games.ttl", "games.ttl", "?_metadata=all&_view=basic");
	}
	
	@Test public void testContentLocationIncludesExplicitTTL_Format() {
		testContentLocationIncludesRenderer("games", "games.ttl", "?_format=ttl&_metadata=all&_view=basic");
	}
	
	@Test public void testContentLocationIncludesExplicitJSON() {
		testContentLocationIncludesRenderer("games.json", "games.json", "?_metadata=all&_view=basic");
	}
	
	@Test public void testContentLocationIncludesExplicitJSON_Format() {
		testContentLocationIncludesRenderer("games", "games.json", "?_format=json&_metadata=all&_view=basic");
	}
	
	@Test public void testContentLocationIncludesExplicitXML() {
		testContentLocationIncludesRenderer("games.xml", "games.xml", "?_metadata=all&_view=basic");
	}
	
	@Test public void testContentLocationIncludesExplicitXML_Format() {
		testContentLocationIncludesRenderer("games", "games.xml", "?_format=xml&_metadata=all&_view=basic");
	}
	
	@Test public void testContentLocationIncludesExplicitHTML() {
		testContentLocationIncludesRenderer("games.html", "games.html", "?_metadata=all&_view=basic");
	}
	
	@Test public void testContentLocationIncludesExplicitHTML_Format() {
		testContentLocationIncludesRenderer("games", "games.html", "?_format=html&_metadata=all&_view=basic");
	}
	
	protected void testContentLocationIncludesRenderer(String provided, String expected, String query) {
		ClientResponse response = getResponse(BASE_URL + "testing/" + provided + query, "text/turtle");
		assertEquals(200, response.getStatus());
		
		String fullLocation = response.getHeaders().get("Content-Location").get(0);
				
		String shortLocation = fullLocation
			.replaceAll("http://[^/]*/", "")
			.replaceAll("\\?.*", "")
			;
		
		assertEquals(expected, shortLocation);
		
		if (shortLocation.endsWith(".ttl")) {
			String entity = response.getEntity(String.class);
			Model result = TestUtil.modelFromTurtle(entity);
			Resource root = result.createResource(fullLocation);
			
			assertTrue(root.hasProperty(OpenSearch.itemsPerPage));
			assertTrue(root.hasProperty(DCTerms.hasFormat));
			assertTrue(root.hasProperty(DCTerms.hasPart));
			assertTrue(root.hasProperty(API.items));
			assertTrue(root.hasProperty(API.page));
			assertTrue(root.hasProperty(XHV.next));
		}
		
	}
}
