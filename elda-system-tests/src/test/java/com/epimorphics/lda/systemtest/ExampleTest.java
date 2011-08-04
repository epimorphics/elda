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
import java.util.Collection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.acceptance.tests.Ask;
import com.epimorphics.lda.acceptance.tests.Framework;
import com.epimorphics.lda.acceptance.tests.WhatToDo;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * An example test to illustrate system testing framework.
 * 
 * @author bwm
 * 
 */

@RunWith(Parameterized.class) public class ExampleTest {
	
	private static String port = Config.getProperties().getProperty("com.epimorphics.lda.testserver.port");

    static Logger log = LoggerFactory.getLogger(ExampleTest.class);
    
	private final WhatToDo w;
	
	public ExampleTest( WhatToDo w )
		{ this.w = w; }
	
	@Parameters public static Collection<Object[]> data()
		{ return Framework.data( "/simple-range-tests" ); }
	
	// Messing around with exceptions because JUnit's Parameterized
	// runner doesn't display a decent test name. So we bash out the
	// test title if it fails.
	@Test public void RUN() throws ClientProtocolException, IOException 
		{
		try
			{ 
			RunTestAllowingFailures(); 
			}
		catch (RuntimeException e)
			{
			System.err.println( ">> test " + w.title + " FAILED." );
			throw e;
			}
		catch (Error e)
			{
			System.err.println( ">> test " + w.title + " FAILED." );
			throw e;
			}
		}
	
	public void RunTestAllowingFailures() throws ClientProtocolException, IOException
		{
		log.debug( "running test " + w.title );
		HttpClient httpclient = new DefaultHttpClient();
		String uri = "http://localhost:" + port + "/elda/api" + w.path + ".ttl?" + w.queryParams;
		HttpGet httpget = new HttpGet( uri );
		HttpResponse response = httpclient.execute(httpget);
		assertEquals( 200, response.getStatusLine().getStatusCode() );
		HttpEntity entity = response.getEntity();
		String content = stringFrom( entity.getContent() );
		Model rsm = ModelIOUtils.modelFromTurtle( content );
		for (Ask a: w.shouldAppear)
			{
			QueryExecution qe = QueryExecutionFactory.create( a.ask, rsm );
			if (qe.execAsk() != a.isPositive)
				{
				fail
					( "test " + w.title + ": the probe query\n"
					+ Framework.shortStringFor( a ) + "\n"
					+ "failed for the result set\n"
					+ Framework.shortStringFor( rsm )
					)
					;			
				}
			}
		}
	
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
