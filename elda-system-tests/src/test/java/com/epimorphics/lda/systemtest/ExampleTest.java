/*
See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
for the licence for this software.

(c) Copyright 2011 Epimorphics Limited
$Id$
*/

package com.epimorphics.lda.systemtest;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;

/**
 * An example test to illustrate system testing framework.
 * 
 * @author bwm
 * 
 */

public class ExampleTest {
	
	private static String port = Config.getProperties().getProperty("com.epimorphics.lda.testserver.port");

	@Test public void exemplar() throws ClientProtocolException, IOException {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet("http://localhost:" + port + "/elda/api/alpha?min-eastish=10");
		HttpResponse response = httpclient.execute(httpget);
		assertEquals(200, response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			InputStream instream = entity.getContent();
			byte[] tmp = new byte[2048];
			while ((instream.read(tmp)) != -1) {
			}
		}
	}
	
	@Test public void testSimpleFilter() throws ClientProtocolException, IOException {
		testHttpRequest( "alpha?min-eastish=10", 200, ignore );
	}
	
	@Test public void testUnknownPropertyGeneratesBadRequest() throws ClientProtocolException, IOException {
		testHttpRequest( "alpha?nosuch=10", 400, ignore );
	}
	
	interface CheckContent {
		boolean check( String s );
		String failMessage();
	}
	
	static CheckContent ignore = new CheckContent() {

		@Override public boolean check(String s) {
			return true;
		}

		@Override public String failMessage() {
			return "cannot fail";
		}
		
	};
	
	public void testHttpRequest( String x, int status, CheckContent cc ) 
		throws ClientProtocolException, IOException {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet("http://localhost:" + port + "/elda/api/" + x );
		HttpResponse response = httpclient.execute(httpget);
	//
		assertEquals( "Check response status:", status, response.getStatusLine().getStatusCode() );
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			String content = stringFrom( entity.getContent() );
			assertTrue( cc.failMessage(), cc.check( content ) );
		}
	}

	private String stringFrom( InputStream s ) throws IOException {
		final char[] buffer = new char[0x10000];
		StringBuilder out = new StringBuilder();
		Reader in = new InputStreamReader( s, "UTF-8" );
		int read;
		do {
		  read = in.read( buffer, 0, buffer.length );
		  if (read > 0) out.append( buffer, 0, read );
		} while (read >= 0);
		return out.toString();
	}
	
}
