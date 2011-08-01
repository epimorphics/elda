/*
See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
for the licence for this software.

(c) Copyright 2011 Epimorphics Limited
$Id$
*/

package com.epimorphics.lda.systemtest;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;

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

	@Test
	public void foobar() throws ClientProtocolException, IOException {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet("http://localhost:" + port + "/elda/api/education/schools?_view=basic&_page=0");
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
}
