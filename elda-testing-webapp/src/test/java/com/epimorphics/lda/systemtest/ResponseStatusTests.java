/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
*/
package com.epimorphics.lda.systemtest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;

import com.epimorphics.lda.exceptions.EldaException;

public class ResponseStatusTests {

	
	@Test public void testSimpleFilter() throws ClientProtocolException, IOException {
		ResponseStatusTests.testHttpRequest( "alpha?min-eastish=10", 200, Util.ignore );
	}
	
	@Test public void testUnknownPropertyGeneratesBadRequest() throws ClientProtocolException, IOException {
		ResponseStatusTests.testHttpRequest( "alpha?nosuch=10", EldaException.BAD_REQUEST, Util.ignore );
	}
	
	@Test public void testCallbackWithNonJSONGeneratesBadRequest() throws ClientProtocolException, IOException {
		ResponseStatusTests.testHttpRequest( "alpha.html?callback=wrong", EldaException.BAD_REQUEST, Util.ignore );
	}
	
	@Test public void testCallbackWithJSONReturnsStatusOK() throws ClientProtocolException, IOException {
		ResponseStatusTests.testHttpRequest( "alpha.json?callback=right", 200, Util.ignore );
	}
	
	@Test public void testItemAccessIsOK() throws ClientProtocolException, IOException {
		ResponseStatusTests.testHttpRequest( "beta", 200, Util.ignore );
	}
	
	@Test public void testPageOnItemEndpointGeneratesBadRequest() throws ClientProtocolException, IOException {
		ResponseStatusTests.testHttpRequest( "beta?_page=1", EldaException.BAD_REQUEST, Util.ignore );
	}
	
	@Test public void testPageSizeOnItemEndpointGeneratesBadRequest() throws ClientProtocolException, IOException {
		ResponseStatusTests.testHttpRequest( "beta?_pageSize=1", EldaException.BAD_REQUEST, Util.ignore );
	}
	
	public static void testHttpRequest( String x, int status, Util.CheckContent cc ) 
		throws ClientProtocolException, IOException {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet("http://localhost:" + Config.port + "/elda/api/" + x );
		HttpResponse response = httpclient.execute(httpget);
	//
		assertEquals( "Check response status:", status, response.getStatusLine().getStatusCode() );
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			String content = Util.stringFrom( entity.getContent() );
			assertTrue( cc.failMessage(), cc.check( content ) );
		}
	}

}
