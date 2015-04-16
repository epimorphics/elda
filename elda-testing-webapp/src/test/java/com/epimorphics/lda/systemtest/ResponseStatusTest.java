/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
*/
package com.epimorphics.lda.systemtest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.protocol.HttpContext;
import org.junit.Ignore;
import org.junit.Test;

import com.epimorphics.lda.exceptions.EldaException;

@Ignore public class ResponseStatusTest {

	@Test public void testSimpleFilter() throws ClientProtocolException, IOException {
		ResponseStatusTest.testHttpRequest( "alpha?min-eastish=10", 200, Util.ignore );
	}
	
	@Test public void testUnknownPropertyGeneratesBadRequest() throws ClientProtocolException, IOException {
		ResponseStatusTest.testHttpRequest( "alpha?nosuch=10", EldaException.BAD_REQUEST, Util.ignore );
	}
	
	@Test public void testCallbackWithNonJSONGeneratesBadRequest() throws ClientProtocolException, IOException {
		ResponseStatusTest.testHttpRequest( "alpha.html?callback=wrong", EldaException.BAD_REQUEST, Util.ignore );
	}
	
	@Test public void testCallbackWithJSONReturnsStatusOK() throws ClientProtocolException, IOException {
		ResponseStatusTest.testHttpRequest( "alpha.json?callback=right", 200, Util.ignore );
	}
	
	@Test public void testMatchingItemTempklateGeneratesRedirection() throws ClientProtocolException, IOException {
		ResponseStatusTest.testHttpRequest( "handover", 303, Util.ignore );
	}
	
	@Test public void testItemAccessIsOK() throws ClientProtocolException, IOException {
		ResponseStatusTest.testHttpRequest( "beta", 200, Util.ignore );
	}
	
	@Test public void testPageOnItemEndpointGeneratesBadRequest() throws ClientProtocolException, IOException {
		ResponseStatusTest.testHttpRequest( "beta?_page=1", EldaException.BAD_REQUEST, Util.ignore );
	}
	
	@Test public void testPageSizeOnItemEndpointGeneratesBadRequest() throws ClientProtocolException, IOException {
		ResponseStatusTest.testHttpRequest( "beta?_pageSize=1", EldaException.BAD_REQUEST, Util.ignore );
	}
	
	public static void testHttpRequest( String x, int status, Util.CheckContent cc ) 
		throws ClientProtocolException, IOException {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		httpclient.setRedirectStrategy( new DefaultRedirectStrategy() {
			 public boolean isRedirected(
			            final HttpRequest request,
			            final HttpResponse response,
			            final HttpContext context) throws ProtocolException {

			    int statusCode = response.getStatusLine().getStatusCode();	 
				return statusCode == HttpStatus.SC_SEE_OTHER ? false : super.isRedirected(request, response, context);
			 }
			
		} );

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
