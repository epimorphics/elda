/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.routing.tests;

import static org.junit.Assert.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.core.APIEndpointImpl;
import com.epimorphics.lda.routing.DefaultRouter;
import com.epimorphics.lda.routing.Router;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.tests_support.LoadsNothing;
import com.epimorphics.util.URIUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
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
	@Test @Ignore public void testFindsURITemplateForItem() {
		URI req = URIUtils.newURI( "" );
		Resource root = specModel.createResource( specModel.expandPrefix( ":root" ) );
		APISpec spec = new APISpec( FileManager.get(), root, LoadsNothing.instance );
		Router r = new DefaultRouter();
		loadRouter( r, spec );
		assertEquals( apiBase + "item/1066", r.findItemURIPath( req, "/look/for/1066" ) );
		assertEquals( apiBase + "other/2001", r.findItemURIPath( req, "/not/this/2001" ) );
	}

	private static void loadRouter( Router r, APISpec spec ) {
		for (APIEndpointSpec eps: spec.getEndpoints()) {
			APIEndpoint ep = new APIEndpointImpl( eps );
			r.register( ep.getURITemplate(), ep );
		}	
	}
	
	String [][] testCases = new String[][] {
			{ "1", "base:uri/{A}", "thing/{A}", "/A", "base:uri/", "thing/A" },
	};
	
	@Test public void testing() {
		StringBuilder errors = new StringBuilder();
		for (String [] tc: testCases) {
			try {
				testInverseLookup( tc[1], tc[2], tc[3], tc[4], tc[5] );
			} catch (AssertionError e) {
				errors.append( "\n" ).append( tc[0] ).append( ": " ).append( e.getMessage() );
			}
		}
		if (errors.length() > 0) {
			fail( errors.toString() );
		}
	}

	private void testInverseLookup
		( String itemTemplate, String uriTemplate, String path, String baseURI, String expected ) {
		Setup s = Setup.prepare( baseURI, itemTemplate, uriTemplate );
		String answer = s.invert( path, baseURI );
		assertEquals( expected, answer );		
	}
	
	static class Setup {
		
		final Router r = new DefaultRouter();
		final APISpec spec;
		
		Setup( APISpec spec ) {
			this.spec = spec;			
		}
		
		static final String configTemplate =
			":root a api:API"
			+ "\n  ; api:sparqlEndpoint <unused:endpoint>"
			+ "\n  ; api:base '{{BASE}}'"
			+ "\n  ; api:endpoint :A"
			+ "\n."
			+ "\n:A a api:ItemEndpoint"
			+ "\n  ; api:uriTemplate '{{UT}}'"
			+ "\n  ; api:itemTemplate '{{IT}}'"
			+ "\n."
			;
		
		static Setup prepare( String apiBase, String itemTemplate, String uriTemplate ) {
			String config = expand( configTemplate, apiBase, itemTemplate, uriTemplate );
			Model m = ModelIOUtils.modelFromTurtle( config );
						
			Resource root = m.createResource( m.expandPrefix( ":root" ) );
			APISpec spec = new APISpec( FileManager.get(), root, LoadsNothing.instance );
			
			Setup result = new Setup( spec );
			loadRouter( result.r, spec ); 
			return result;
		}
		
		private static String expand(String template, String apiBase, String itemTemplate, String uriTemplate) {
			return template
				.replace( "{{IT}}", itemTemplate )
				.replace( "{{UT}}", uriTemplate )
				.replace( "{{BASE}}", apiBase )
				;
		}

		String invert( String path, String baseURI ) {
			return r.findItemURIPath( URIUtils.newURI( baseURI ), path );
		}
		
	}
	
	

}
