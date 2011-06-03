/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.bindings.tests;

import org.junit.Ignore;
import org.junit.Test;

import com.epimorphics.lda.bindings.VarValues;
import com.epimorphics.lda.bindings.VariableExtractor;
import com.epimorphics.lda.tests_support.MakeData;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class TestBindings {
	
	@Test @Ignore public void testMe() {
		Model m = MakeData.specModel( "root api:variable _v; _v api:name 'var'; _v api:value '{other}'" );
		Resource root = m.createResource( "eh:/root" );
		VarValues v = VariableExtractor.findAndBindVariables( root );
		// System.err.println( ">> " + v );
	}

}
