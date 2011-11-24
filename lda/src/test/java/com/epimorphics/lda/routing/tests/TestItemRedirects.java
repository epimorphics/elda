/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.routing.tests;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.bindings.Lookup;
import com.epimorphics.lda.routing.MatchSearcher;
import com.epimorphics.lda.specs.APIEndpointSpec;
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
		+ "\n:A a api:ItemEndpoint"
		+ "\n  ; api:uriTemplate '/other/{item}'"
		+ "\n  ; api:itemTemplate '/not/this/{item}'"
		+ "\n."
		+ "\n:B a api:ItemEndpoint"
		+ "\n  ; api:uriTemplate '/item/{item}'"
		+ "\n  ; api:itemTemplate '/look/for/{item}'"
		+ "\n."
		);
	
	@Test public void testRecognisesRedirect() {
		Resource root = specModel.createResource( specModel.expandPrefix( ":root" ) );
		APISpec spec = new APISpec( FileManager.get(), root, LoadsNothing.instance );
		assertEquals( "/item/1066", findItemURI( spec, "/look/for/1066" ) );
		assertEquals( "/other/2001", findItemURI( spec, "/not/this/2001" ) );
	}

	private String findItemURI( APISpec spec, String path ) {
		MatchSearcher<String> ms = new MatchSearcher<String>();
		for (APIEndpointSpec eps: spec.getEndpoints()) {
			String it = eps.getItemTemplate();
			if (it != null) ms.register( it, eps.getURITemplate() );
		}
		Map<String, String> bindings = new HashMap<String, String>();
		String ut = ms.lookup( bindings, path );
		if (ut != null) {
			return Bindings.expandVariables( asLookup( bindings ), ut );
		}
		return null;
	}

	private Lookup asLookup( final Map<String, String> bindings ) {
		return new Lookup() {
			@Override public String getValueString(String name) {
				return bindings.get( name );
			}
		};
	}

}
