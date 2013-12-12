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
import java.util.Set;

import org.junit.Test;

import com.epimorphics.util.CollectionUtils;
import com.epimorphics.util.URIUtils;

public class TestURIUtils {
	
	@Test public void toHoldASpecialCharacter() {
		URI u = newURI( "http://example.com/spa%20cé" );
	}
	
	@Test public void testReplacesKnownFormat() {
		URI req = newURI( "http://example.com/anchor/thing.rdf" );
		Set<String> knownFormats = CollectionUtils.set( "rdf", "ttl" );
		URI got = changeFormatSuffix( req, knownFormats, "ttl" );
		assertEquals( newURI("http://example.com/anchor/thing.ttl"), got );
	}
	
	@Test public void testPreservesUnknownFormat() {
		URI req = newURI( "http://example.com/anchor/thing.rdf" );
		Set<String> knownFormats = CollectionUtils.set( "n3", "ttl" );
		URI got = changeFormatSuffix( req, knownFormats, "ttl" );
		assertEquals( newURI("http://example.com/anchor/thing.rdf.ttl"), got );
	}
	
	@Test public void testChangeFormatSuffixPreservesEncoding() {
		URI req = newURI( "http://example.com/anc%20hor/thing.rdf" );
		Set<String> knownFormats = CollectionUtils.set( "n3", "ttl" );
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
	
	@Test public void testIt() {
		String RU = "http://localhost:8080/elda/api/redirected/education/id/school/100869";
		for (String apiBase: "/ /environment /environment/ http://localhost http://localhost/".split( " +" )) 
			for (String mrp: "/doc/bathing-water doc/bathing-water".split(" +" )) {	
				URI result = URIUtils.resolveAgainstBase( newURI(RU), newURI(apiBase), mrp );
				// System.err.println( "!! " + apiBase + ", " + mrp + ", " + result.toString() );
		}
	}

	@Test public void testThat() {
		URI mid = newURI( "http://localhost:8080/environment/" );
		URI resolved = mid.resolve( "doc/bathing-water" );
		// System.err.println( ")) " + resolved.toString() );
	}
	
}
