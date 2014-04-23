/*
See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
for the licence for this software.

(c) Copyright 2011 Epimorphics Limited
$Id$
*/

package com.epimorphics.lda.systemtest;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.acceptance.tests.Ask;
import com.epimorphics.lda.acceptance.tests.TestFramework;
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

@Ignore @RunWith(Parameterized.class) public class RemoteSimpleRangeTests {
	
	static Logger log = LoggerFactory.getLogger(RemoteSimpleRangeTests.class);
    
	private final WhatToDo w;
	
	public RemoteSimpleRangeTests( WhatToDo w )
		{ this.w = w; }
	
	@Parameters public static Collection<Object[]> data()
		{ return TestFramework.data( "/simple-range-tests" ); }
	
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
		String uri = "http://localhost:" + Config.port + "/elda/api" + w.path + ".ttl?" + w.queryParams;
		HttpGet httpget = new HttpGet( uri );
		HttpResponse response = httpclient.execute(httpget);
		assertEquals( 200, response.getStatusLine().getStatusCode() );
		HttpEntity entity = response.getEntity();
		String content = Util.stringFrom( entity.getContent() );
		Model rsm = ModelIOUtils.modelFromTurtle( content );
		for (Ask a: w.shouldAppear)
			{
			QueryExecution qe = QueryExecutionFactory.create( a.ask, rsm );
			if (qe.execAsk() != a.isPositive)
				{
				fail
					( "test " + w.title + ": the probe query\n"
					+ TestFramework.shortStringFor( a ) + "\n"
					+ "failed for the result set\n"
					+ TestFramework.shortStringFor( rsm )
					)
					;			
				}
			}
		}
	
//	@Test public void exemplar() throws ClientProtocolException, IOException {
//		HttpClient httpclient = new DefaultHttpClient();
//		HttpGet httpget = new HttpGet("http://localhost:" + Config.port + "/elda/api/alpha?min-eastish=10");
//		HttpResponse response = httpclient.execute(httpget);
//		assertEquals(200, response.getStatusLine().getStatusCode());
//		HttpEntity entity = response.getEntity();
//		if (entity != null) {
//			InputStream instream = entity.getContent();
//			byte[] tmp = new byte[2048];
//			while ((instream.read(tmp)) != -1) {
//			}
//		}
//	}
	
}
