/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.apispec.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.support.ModelPrefixEditor;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

public class TestAPISpecExtractsModelPrefixEditor {

	@Test public void testExtractsEditor() {
		Model spec = ModelIOUtils.modelFromTurtle
			( 
			":s a api:API"
			+ "\n; api:sparqlEndpoint <http://example.com/none>"
			+ "\n; elda:rewriteResultURIs [elda:ifStarts 'fromA'; elda:replaceStartBy 'toA']"
			+ "\n; elda:rewriteResultURIs [elda:ifStarts 'fromB'; elda:replaceStartBy 'toB']"
			+ "\n."
			);
		
		Resource s = spec.getResource( spec.expandPrefix( ":s" ) );
		// Resource e = spec.getResource( spec.expandPrefix( ":e" ) );

		APISpec a = SpecUtil.specFrom( s );
		
		ModelPrefixEditor expected = new ModelPrefixEditor()
			.set( "fromA", "toA" )
			.set( "fromB", "toB" )
			;
		
		assertEquals( expected, a.getModelPrefixEditor() );
		
	}
	
}
