package com.epimorphics.lda.apispec.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.core.APISpec;
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
		APISpec a = new APISpec( s, null );		
		assertEquals( "to/be/expunged", a.getBase() );
		}

	}
