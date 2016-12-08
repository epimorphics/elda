/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
    
    File:        APIEndpointImpl.java
    Created by:  Dave Reynolds
    Created on:  31 Jan 2010
*/

package com.epimorphics.util.tests;

import static com.epimorphics.util.URIUtils.changeFormatSuffix;
import static com.epimorphics.util.URIUtils.newURI;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.URI;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.epimorphics.util.CollectionUtils;
import com.epimorphics.util.URIUtils;

public class TestURIUtils {
	
	@Test public void toHoldASpecialCharacter() {
		@SuppressWarnings("unused") URI u = newURI( "http://example.com/spa%20cé" );
	}
	
	@Test public void testReplacesKnownFormat() {
		URI req = newURI( "http://example.com/anchor/thing.rdf" );
		List<String> knownFormats = CollectionUtils.list( "rdf", "ttl" );
		URI got = changeFormatSuffix( req, knownFormats, "ttl" );
		assertEquals( newURI("http://example.com/anchor/thing.ttl"), got );
	}
	
	@Test public void testPreservesUnknownFormat() {
		URI req = newURI( "http://example.com/anchor/thing.rdf" );
		List<String> knownFormats = CollectionUtils.list( "n3", "ttl" );
		URI got = changeFormatSuffix( req, knownFormats, "ttl" );
		assertEquals( newURI("http://example.com/anchor/thing.rdf.ttl"), got );
	}
	
	@Test public void testChangeFormatSuffixPreservesEncoding() {
		URI req = newURI( "http://example.com/anc%20hor/thing.rdf" );
		List<String> knownFormats = CollectionUtils.list( "n3", "ttl" );
		URI got = changeFormatSuffix( req, knownFormats, "ttl" );
		assertEquals( newURI("http://example.com/anc%20hor/thing.rdf.ttl"), got );
	}
	
	// If any braces (etc) are in the path, they are escaped.
	@Test public void testURISlashlessFragment() {
		URI u = URIUtils.noLeadingSlash( "/a{b}c" );
		assertEquals( "a%7Bb%7Dc", u.getRawPath() );
		assertNull( u.getScheme() );
		assertNull( u.getHost() );
		assertNull( u.getFragment() );
		assertNull( u.getQuery() );
	}
	
	static class Case {
		String expect;
		String apiBase;
		String requestURI;
		String uiPath;
		
		Case(String e, String a, String r, String u) { expect = e; apiBase = a; requestURI = r; uiPath = u; }
	}
	
	static final Case[] cases = new Case[] {
		//        RESULT                                                             API:BASE                                 REQUEST URI                                                        PATH
		new Case( "http://education.gov.uk/redirected/education/id/school/100869", "http://education.gov.uk/", "http://localhost:8080/elda/api/redirected/education/id/school/100869", "redirected/education/id/school/100869" )
	};
	
	@Test public void testResolution() {
		for (Case c: cases) {
			URI result = URIUtils.resolveAgainstBase( newURI(c.requestURI), newURI(c.apiBase), c.uiPath );
			assertEquals( c.expect, result.toString() );
		}
	}
	
	static final String RU = "http://localhost:8080/elda/api/redirected/education/id/school/100869";

	static final String[][] params = new String[][] {
		// apiBase				uipath					result
		{ "/",					"/doc/bathing-water",	"http://localhost:8080/doc/bathing-water" },
		{ "/",					"doc/bathing-water", 	"http://localhost:8080/doc/bathing-water" },
		{ "/environment",		"/doc/bathing-water",	"http://localhost:8080/environment/doc/bathing-water" },
		{ "/environment",		"doc/bathing-water",	"http://localhost:8080/environment/doc/bathing-water" },
		{ "/environment/",		"/doc/bathing-water",	"http://localhost:8080/environment/doc/bathing-water" },
		{ "/environment/",		"doc/bathing-water",	"http://localhost:8080/environment/doc/bathing-water" },
		{ "http://localhost",	"/doc/bathing-water",	"http://localhost/doc/bathing-water" },
		{ "http://localhost",	"doc/bathing-water",	"http://localhost/doc/bathing-water" },
		{ "http://localhost/",	"/doc/bathing-water",	"http://localhost/doc/bathing-water" },
		{ "http://localhost/",	"doc/bathing-water",	"http://localhost/doc/bathing-water" }
	};
	
	@Test public void testBuildsExpectedURI() {
		for (String [] row: params) {
			String apiBase = row[0], uiPath = row[1];
			URI result = URIUtils.resolveAgainstBase(newURI(RU), newURI(apiBase), uiPath);
			assertEquals(newURI(row[2]), result);
		}
	}
	
}
