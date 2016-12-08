/*
	See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
	for the licence for this software.
	
	(c) Copyright 2011 Epimorphics Limited
	$Id$
*/
package com.epimorphics.lda.systemtest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.protocol.HttpContext;

public class Util {

	public interface CheckContent {
		boolean check( String s );
		String failMessage();
	}

	public static String stringFrom( InputStream s ) throws IOException {
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

	static Util.CheckContent ignore = new Util.CheckContent() {
	
		@Override public boolean check(String s) {
			return true;
		}
	
		@Override public String failMessage() {
			return "cannot fail";
		}
		
	};

	public static void testHttpRequest( String x, int status, CheckContent cc ) 
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
	
		HttpGet httpget = new HttpGet("http://localhost:" + 8070 + "/testing/" + x );
		HttpResponse response = httpclient.execute(httpget);
	//
		assertEquals( "Check response status:", status, response.getStatusLine().getStatusCode() );
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			String content = stringFrom( entity.getContent() );
			assertTrue( cc.failMessage(), cc.check( content ) );
		}
	}

}
