/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
*/
package com.epimorphics.lda.systemtest;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.junit.Ignore;
import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.testing.utils.TomcatTestBase;
import com.epimorphics.lda.vocabularies.API;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;

public class ResponseStatusTest extends TomcatTestBase {

	@Override public String getWebappRoot() {
		return "src/main/webapp";
	}
	
	// test that a call to api-config succeeds
	@Test public void testApiConfig() throws ClientProtocolException, IOException {
		Util.testHttpRequest( "api-config", 200, Util.ignore );
	}
	
	// test that a call to api-config succeeds after a endpoint call
	@Test public void testApiConfig2() throws ClientProtocolException, IOException {		
		Util.testHttpRequest( "games", 200, Util.ignore );
		Util.testHttpRequest( "api-config", 200, Util.ignore );
	}
	
	@Test public void testSimpleFilter() throws ClientProtocolException, IOException {
		Util.testHttpRequest( "games", 200, Util.ignore );
	}
	
	@Test public void testUnknownPropertyGeneratesBadRequest() throws ClientProtocolException, IOException {
		Util.testHttpRequest( "games?nosuch=10", EldaException.BAD_REQUEST, Util.ignore );
	}
	
	@Test public void testCallbackWithNonJSONGeneratesBadRequest() throws ClientProtocolException, IOException {
		Util.testHttpRequest( "games.html?callback=wrong", EldaException.BAD_REQUEST, Util.ignore );
	}
	
	@Test public void testCallbackWithJSONReturnsStatusOK() throws ClientProtocolException, IOException {
		Util.testHttpRequest( "games.json?callback=right", 200, Util.ignore );
	}
	
	@Test @Ignore public void testShowItemSucceeds() throws ClientProtocolException, IOException {
		Util.testHttpRequest( "show/item/A", 200, Util.ignore );
	}
	
	@Test public void testMatchingItemTemplateGeneratesRedirection() throws ClientProtocolException, IOException {
		Util.testHttpRequest( "item/A", 303, Util.ignore );
	}
	
	@Test public void testThingAccessIsOK() throws ClientProtocolException, IOException {
		Util.testHttpRequest( "things", 200, Util.ignore );
	}
	
	@Test public void testLanguageCodes() throws ClientProtocolException, IOException {
		Util.testHttpRequest( "lang.ttl?_metadata=all&_lang=en,cy&min-label=crumble", 200, new Util.CheckContent() {
			
			String message = "unset";
			
			@Override public String failMessage() {
				return "expected item C only: " + message;
			}
			
			@Override public boolean check(String content) {				
				Model m = ModelIOUtils.modelFromTurtle(content);			
				Resource C = m.createResource(m.expandPrefix("hello:langC"));
				RDFNode selected = m.listObjectsOfProperty(API.items).toList().get(0);
				List<RDFNode> l = selected.as(RDFList.class).asJavaList();
				message = l.toString();
				return l.size() == 1 && l.get(0).asResource().equals(C);
				}
			} );
	}
	
	@Test public void testBnodesSuppressed() throws ClientProtocolException, IOException {
		
		boolean anonPresent = false;
		Model m = FileManager.get().loadModel("src/main/webapp/games-test-data.ttl");
		Resource Thing = m.createResource(m.expandPrefix("egc:Thing"));
		for (Statement s: m.listStatements(null, RDF.type, Thing).toList()) {
			if (s.getSubject().isAnon()) anonPresent = true;
		}
		assertTrue("there should be candidate bnodes in the tests data.", anonPresent);
		
		Util.testHttpRequest( "things.ttl", 200, new Util.CheckContent() {
			
			@Override public String failMessage() {
				return "bnodes should not be returned as results";
			}
			
			/*
				check interprets the content as a Turtle rendering of a query response.
				It finds the list of selected items from the model (which is the object
				of the single api:items triple in that model) and returns false if any
				of them is a bnode. Otherwise it returns true. Other resources in the
				model may be bnodes; that doesn't matter.
			*/
			@Override public boolean check(String content) {
				Model m = ModelIOUtils.modelFromTurtle(content);
				
				for (RDFNode selected: m.listObjectsOfProperty(API.items).toList()) {
					List<RDFNode> l = selected.as(RDFList.class).asJavaList();
					for (RDFNode x: l) if (x.isAnon()) return false;
				}
				
				return true;
			}
		} );
	}
	
	@Test public void testPageOnItemEndpointGeneratesBadRequest() throws ClientProtocolException, IOException {
		Util.testHttpRequest( "show/item/A?_page=1", EldaException.BAD_REQUEST, Util.ignore );
	}
	
	@Test public void testPageSizeOnItemEndpointGeneratesBadRequest() throws ClientProtocolException, IOException {
		Util.testHttpRequest( "show/item/B?_pageSize=1", EldaException.BAD_REQUEST, Util.ignore );
	}

}
