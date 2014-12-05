package com.epimorphics.lda.testing.tomcat;

import static org.junit.Assert.*;

import java.io.StringReader;

import javax.ws.rs.core.MediaType;

import org.junit.Test;

import com.epimorphics.lda.testing.utils.TomcatTestBase;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.sun.jersey.api.client.ClientResponse;

public class TestViewIsUsingValues extends TomcatTestBase{

	@Override public String getWebappRoot() {
		return "src/test/webapp";
	}
	
	static final MediaType typeTurtle = new MediaType("text", "turtle");
	
	@Test public void testme() {
		ClientResponse response = getResponse(BASE_URL + "testing/games?_metadata=all&_view=basic", "text/turtle");
		assertEquals(200, response.getStatus());
		assertTrue(response.getType().isCompatible(typeTurtle));
		String entity = response.getEntity(String.class);
		System.err.println(">> entity:" + entity);
		Model result = modelFromTurtle(entity);
		System.err.println(">> =============================================== <<");
		result.write(System.err, "TTL");
		System.err.println(">> ----------------------------------------------- <<");
	}
	
	public static Model modelFromTurtle(String ttl) {
		Model model = ModelFactory.createDefaultModel();
		return model.read( new StringReader(ttl), null, "Turtle");
	}

}
