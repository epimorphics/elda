/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.routing.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.tests_support.LoadsNothing;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

public class TestItemRedirects {
	
	Model specModel = ModelIOUtils.modelFromTurtle
		( ":root a api:API"
		+ "\n  ; api:sparqlEndpoint <unused:endpoint>"
		+ "\n  ; api:endpoint :A, :B"
		+ "\n."
		+ "\n:A a api:ListEndpoint"
		+ "\n  ; api:uriTemplate '/list'"
		+ "\n."
		+ "\n:B a api:ItemEndpoint"
		+ "\n  ; api:uriTemplate '/item/{item}'"
		+ "\n  ; api:itemTemplate 'look/for/{item}'"
		+ "\n."
		);
	
	@Test public void testRecognisesRedirect() {
		Resource root = specModel.createResource( specModel.expandPrefix( ":root" ) );
		APISpec spec = new APISpec( FileManager.get(), root, LoadsNothing.instance );
		String redirectTo = findItemURI( spec, "/look/for/1066" );
		assertEquals( "/item/1066", redirectTo );
	}

	private String findItemURI( APISpec spec, String path ) {
		return null;
	}

}
