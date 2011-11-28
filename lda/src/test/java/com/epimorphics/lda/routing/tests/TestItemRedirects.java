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
import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.core.APIEndpointImpl;
import com.epimorphics.lda.routing.DefaultRouter;
import com.epimorphics.lda.routing.Router;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.tests_support.LoadsNothing;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

public class TestItemRedirects {
	
	private static final String apiBase = "http://example.com/";
	
	Model specModel = ModelIOUtils.modelFromTurtle
		(( ":root a api:API"
		+ "\n  ; api:base '$BASE$'"
		+ "\n  ; api:sparqlEndpoint <unused:endpoint>"
		+ "\n  ; api:endpoint :A, :B"
		+ "\n."
		+ "\n:A a api:ItemEndpoint"
		+ "\n  ; api:uriTemplate '/other/{item}'"
		+ "\n  ; api:itemTemplate '$BASE$not/this/{item}'"
		+ "\n."
		+ "\n:B a api:ItemEndpoint"
		+ "\n  ; api:uriTemplate '/item/{item}'"
		+ "\n  ; api:itemTemplate '$BASE$look/for/{item}'"
		+ "\n."
		).replace( "$BASE$", apiBase ));
	
	/**
	    Test that a (Default) Router will find the correct URI template associated
	    with an ItemTemplate that matches a given path.
	*/
	@Test public void testFindsURITemplateForItem() {
		Resource root = specModel.createResource( specModel.expandPrefix( ":root" ) );
		APISpec spec = new APISpec( FileManager.get(), root, LoadsNothing.instance );
		Router r = new DefaultRouter();
		loadRouter( r, spec );
		assertEquals( apiBase + "item/1066", r.findItemURIPath( "/look/for/1066" ) );
		assertEquals( apiBase + "other/2001", r.findItemURIPath( "/not/this/2001" ) );
	}

	private void loadRouter( Router r, APISpec spec ) {
		for (APIEndpointSpec eps: spec.getEndpoints()) {
			APIEndpoint ep = new APIEndpointImpl( eps );
			r.register( ep.getURITemplate(), ep );
		}	
	}

}
