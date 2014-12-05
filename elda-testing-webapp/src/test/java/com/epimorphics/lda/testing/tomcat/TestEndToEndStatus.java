package com.epimorphics.lda.testing.tomcat;

import static org.junit.Assert.*;

import org.junit.Test;

import com.epimorphics.lda.testing.utils.TomcatTestBase;
import com.sun.jersey.api.client.ClientResponse;

public class TestEndToEndStatus extends TomcatTestBase {

	@Override public String getWebappRoot() {
		return "src/test/webapp";
	}
	
	@Test public void succeed() {
		assertTrue(1 == 1);
	}
	
	@Test public void testing() {
		String u = BASE_URL + "testing/games";
		System.err.println(">> U = " + u );
		ClientResponse response = getResponse(u, "text/turtle");
		System.err.println(">> status: " + response.getStatus());
		System.err.println(">> text: " + response.getEntity(String.class));
		assertEquals(200, response.getStatus());
	}


}
