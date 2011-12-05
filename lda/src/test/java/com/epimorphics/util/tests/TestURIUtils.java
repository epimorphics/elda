/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
    
    File:        APIEndpointImpl.java
    Created by:  Dave Reynolds
    Created on:  31 Jan 2010
*/

package com.epimorphics.util.tests;

import static org.junit.Assert.*;

import java.net.URI;
import java.util.Set;

import org.junit.Test;

import com.epimorphics.util.CollectionUtils;
import com.epimorphics.util.URIUtils;

public class TestURIUtils {
	
	@Test public void testReplacesKnownFormat() {
		URI req = URIUtils.newURI( "http://example.com/anchor/thing.rdf" );
		Set<String> knownFormats = CollectionUtils.set( "rdf", "ttl" );
		URI got = URIUtils.changeFormatSuffix( req, knownFormats, "ttl" );
		assertEquals( URIUtils.newURI("http://example.com/anchor/thing.ttl"), got );
	}
	
	@Test public void testPreservesUnknownFormat() {
		URI req = URIUtils.newURI( "http://example.com/anchor/thing.rdf" );
		Set<String> knownFormats = CollectionUtils.set( "n3", "ttl" );
		URI got = URIUtils.changeFormatSuffix( req, knownFormats, "ttl" );
		assertEquals( URIUtils.newURI("http://example.com/anchor/thing.rdf.ttl"), got );
	}
	
	@Test public void testChangeFormatSuffixPreservesEncoding() {
		URI req = URIUtils.newURI( "http://example.com/anc%20hor/thing.rdf" );
		Set<String> knownFormats = CollectionUtils.set( "n3", "ttl" );
		URI got = URIUtils.changeFormatSuffix( req, knownFormats, "ttl" );
		assertEquals( URIUtils.newURI("http://example.com/anc%20hor/thing.rdf.ttl"), got );
	}

}
