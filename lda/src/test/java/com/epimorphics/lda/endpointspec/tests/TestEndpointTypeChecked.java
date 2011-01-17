/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.endpointspec.tests;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.core.APISpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class TestEndpointTypeChecked
	{
	Model spec = ModelIOUtils.modelFromTurtle
		( 
		":s a api:API; api:endpoint :e; api:sparqlEndpoint <http://example.com/none>."
		+ "\n:e api:uriTemplate '/absent/friends'." 
		);

	Resource s = spec.getResource( spec.expandPrefix( ":s" ) );
	Resource e = spec.getResource( spec.expandPrefix( ":e" ) );
	
	@Test @Ignore public void spoo()
		{
		try 
			{ 
			new APISpec( s, null ); 
			fail( "should detect missing endpoint type" ); 
			}
		catch (Exception e) 
			{}
		}
	}
