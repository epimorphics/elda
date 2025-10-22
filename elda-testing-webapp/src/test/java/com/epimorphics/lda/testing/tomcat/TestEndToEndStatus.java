package com.epimorphics.lda.testing.tomcat;

import com.epimorphics.lda.testing.utils.TomcatTestBase;
import jakarta.ws.rs.core.MediaType;
import org.glassfish.jersey.client.ClientResponse;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestEndToEndStatus extends TomcatTestBase {

	@Override public String getWebappRoot() {
		return "src/main/webapp";
	}
	
	static final MediaType typeTurtle = new MediaType("text", "turtle");
	
	@Test public void testStatus200() {
		ClientResponse response = getResponse(BASE_URL + "testing/games", "text/turtle");
		assertEquals(200, response.getStatus());
		assertTrue(response.getMediaType().isCompatible(typeTurtle));
	}
	
	@Test public void testStatus400() {
		ClientResponse response = getResponse(BASE_URL + "testing/games?_unknown=17", "text/turtle");
		assertEquals(400, response.getStatus());
		assertTrue(response.getMediaType().isCompatible(typeTurtle));
	}
	
	@Test public void testStatus400BadCountValue() {
		ClientResponse response = getResponse(BASE_URL + "testing/games?_count=vorkosigan", "text/turtle");
		assertEquals(400, response.getStatus());
		assertTrue(response.getMediaType().isCompatible(typeTurtle));
	}
	
	@Test public void testStatus404() {
		ClientResponse response = getResponse(BASE_URL + "testing/no-games", "text/turtle");
		assertEquals(404, response.getStatus());
	}

}
