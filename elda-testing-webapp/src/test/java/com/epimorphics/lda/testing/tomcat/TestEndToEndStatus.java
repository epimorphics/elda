package com.epimorphics.lda.testing.tomcat;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import com.epimorphics.lda.testing.utils.TomcatTestBase;
import com.sun.jersey.api.client.ClientResponse;

public class TestEndToEndStatus extends TomcatTestBase {

	@Override public String getWebappRoot() {
		return "src/test/webapp";
	}
	
	@Test public void testStatus200() {
		ClientResponse response = getResponse(BASE_URL + "testing/games", "text/turtle");
		assertEquals(200, response.getStatus());
	}
	
	@Test @Ignore public void testStatus400() {
		ClientResponse response = getResponse(BASE_URL + "testing/games?_unknown=17", "text/turtle");
		assertEquals(400, response.getStatus());
	}
	
	@Test @Ignore public void testStatus400BadCountValue() {
		ClientResponse response = getResponse(BASE_URL + "testing/games?_count=vorkosigan", "text/turtle");
		assertEquals(400, response.getStatus());
	}
	
	@Test public void testStatus404() {
		ClientResponse response = getResponse(BASE_URL + "testing/no-games", "text/turtle");
		assertEquals(404, response.getStatus());
	}

}
