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

import static org.junit.Assert.*;

import org.junit.Test;

import com.epimorphics.util.MediaType;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;

public class TestMediaType 
	{
	@Test public void ensureMediaTypeRespectsEquals() 
		{
		MediaType mt = new MediaType("a", "b" );
		assertEquals( mt, new MediaType( "a", "b" ) );
		ModelTestBase.assertDiffer( mt, new MediaType( "b", "a" ) );
		}
	
	@Test public void ensureMediaTypeDoesAccept()
		{
		assertTrue( new MediaType( "a", "b" ).accepts( new MediaType( "a", "b" ) ) );
		assertTrue( new MediaType( "*", "b" ).accepts( new MediaType( "a", "b" ) ) );
		assertTrue( new MediaType( "a", "*" ).accepts( new MediaType( "a", "b" ) ) );
		assertTrue( new MediaType( "*", "*" ).accepts( new MediaType( "a", "b" ) ) );
		}
	
	@Test public void ensureMediaTypeDoesReject()
		{
		assertFalse( new MediaType( "a", "b" ).accepts( new MediaType( "a", "c" ) ) );
		assertFalse( new MediaType( "a", "b" ).accepts( new MediaType( "c", "b" ) ) );
		assertFalse( new MediaType( "a", "b" ).accepts( new MediaType( "c", "d" ) ) );
		}
	
	@Test public void ensureMediaTypeRejectsBithWildcard()
		{
		assertFalse( new MediaType( "*", "b" ).accepts( new MediaType( "a", "c" ) ) );
		assertFalse( new MediaType( "a", "*" ).accepts( new MediaType( "c", "b" ) ) );
		}
	}	
