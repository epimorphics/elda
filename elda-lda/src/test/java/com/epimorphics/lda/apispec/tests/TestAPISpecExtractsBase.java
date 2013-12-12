/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.apispec.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.specs.APISpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class TestAPISpecExtractsBase 
	{
	Model spec = ModelIOUtils.modelFromTurtle
		( 
		":s a api:API; api:sparqlEndpoint <http://example.com/none>; api:base 'to/be/expunged'."
		);
	
	Resource s = spec.getResource( spec.expandPrefix( ":s" ) );
	Resource e = spec.getResource( spec.expandPrefix( ":e" ) );

	@Test public void testExtractsBase()
		{
		APISpec a = SpecUtil.specFrom( s );		
		assertEquals( "to/be/expunged", a.getBase() );
		}

	}
