/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.endpointspec.tests;

import static org.junit.Assert.fail;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.apispec.tests.SpecUtil;
import com.epimorphics.lda.core.APIEndpointImpl;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.specs.APISpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class TestEndpointHandlesWhere 
	{
	Model spec = ModelIOUtils.modelFromTurtle
		( 
		":s a api:API; api:endpoint :e; api:sparqlEndpoint <http://example.com/none>."
		+ "\n:e a api:ListEndpoint; api:uriTemplate '/absent/friends'"
		+ "\n; api:selector[api:where 'PONDENOME']." 
		);

	Resource s = spec.getResource( spec.expandPrefix( ":s" ) );
	Resource e = spec.getResource( spec.expandPrefix( ":e" ) );
		
	@Test public void testEndpointImplUsedWhere()
		{
		APISpec a = SpecUtil.specFrom( s );
		APIEndpointSpec eps = new APIEndpointSpec( a, a, e );
		APIEndpointImpl i = new APIEndpointImpl( eps );
		String q = i.getSelectQuery();
		if (!q.replaceAll( "[\n ]+", " " ).matches( "SELECT \\?item WHERE \\{ PONDENOME\\} OFFSET 0 LIMIT 10" ))
			{
			fail( "constructed query '" + q + "'\ndoes not contain api:where clause" );
			}
		}
	}
