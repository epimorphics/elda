/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.tests;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.shortnames.ShortnameService.Util;
import com.epimorphics.lda.tests_support.ExpandOnly;
import com.epimorphics.util.CollectionUtils;
import com.hp.hpl.jena.rdf.model.Property;

import static com.hp.hpl.jena.rdf.model.test.ModelTestBase.property;

/**
    Some (limited) tests for shortname services
 
 	@author chris
*/
public class TestShortnameServices
	{
	@Test public void testExpandProperties()
		{
		Property P = property( "eh:/P" ), Q = property( "spoo:/Q" );
		ShortnameService sns = new ExpandOnly( "P=eh:/P;Q=spoo:/Q" );
		List<Property> p = Util.expandProperties( "P.Q", sns );
		assertEquals( CollectionUtils.list( P, Q ), p );
		}
	}
