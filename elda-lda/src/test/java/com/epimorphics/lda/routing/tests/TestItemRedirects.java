/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.routing.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.URI;

import org.junit.Ignore;
import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.core.APIEndpointImpl;
import com.epimorphics.lda.routing.DefaultRouter;
import com.epimorphics.lda.routing.Router;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.support.EldaFileManager;
import com.epimorphics.lda.tests_support.LoadsNothing;
import com.epimorphics.util.URIUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

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
		APISpec spec = new APISpec( EldaFileManager.get(), root, LoadsNothing.instance );
		Router r = new DefaultRouter();
		loadRouter( r, spec );
		assertEquals( apiBase + "item/1066", r.findItemURIPath( "_", req, "/look/for/1066" ) );
		assertEquals( apiBase + "other/2001", r.findItemURIPath( "_", req, "/not/this/2001" ) );
	}

	private static void loadRouter( Router r, APISpec spec ) {
		for (APIEndpointSpec eps: spec.getEndpoints()) {
			APIEndpoint ep = new APIEndpointImpl( eps );
			r.register( null, ep.getURITemplate(), ep );
		}	
	}
	
	String [][] testCases = new String[][] {
		// tag request URI           itemTemplate           path    uriTemplate  api:base              expected
			
		{ "1",  "http://ex.com/A",   "http://ex.com/{A}",   "/A",   "thing/{A}", "http://ex.com/",     "http://ex.com/thing/A" },
		{ "2",  "http://ex.com/A/B", "http://ex.com/{A}",   "/A/B", "thing/{A}", "http://ex.com/",     null },
		{ "3",  "http://ex.com/B/A", "http://ex.com/{A}",   "/B/A", "thing/{A}", "http://ex.com/",     null },
		{ "4",  "http://ex.com/A",   "http://ex.com/{A}",   "/A",   "thing/{A}", "http://ex.com/miff", "http://ex.com/miff/thing/A" },
		{ "5",  "http://ex.com/A",   "http://ey.com/{A}",   "/A",   "thing/{A}", "http://ex.com/",     "http://ex.com/thing/A" },
		{ "6",  "http://ex.com/X/A", "http://ex.com/X/{A}", "/X/A", "thing/{A}", "http://ex.com/",     "http://ex.com/thing/A" },
		
		{ "7",  "http://ex.com/A",   "http://ex.com/{A}",   "/A",  "thing/{A}", "/",                  "http://ex.com/thing/A" },
		{ "8",  "http://ey.com/A",   "http://ex.com/{A}",   "/A",  "thing/{A}", "/",                  "http://ey.com/thing/A" },
		
		{ "9",  "http://ex.com/A/B", "http://ex.com/{A}",   "/A/B", "thing/{A}", "http://ex.com/",     null },
		{ "10", "http://ex.com/B/A", "http://ex.com/{A}",   "/B/A", "thing/{A}", "http://ex.com/",     null },
		
		{ "11", "http://ex.com/X/A", "http://ex.com/X/{A}", "/X/A", "thing/{A}", "http://fy.dun/P/Q", "http://fy.dun/P/Q/thing/A" },
		
		{ "12", "http://ex.com/A",   "http://ex.com/{A}",   "/A",   "thing/{A}", "http://ex.com/miff", "http://ex.com/miff/thing/A" },
		{ "13", "http://ex.com/A",   "http://ey.com/{A}",   "/A",   "thing/{A}", "http://ex.com/",     "http://ex.com/thing/A" },
		{ "14", "http://ex.com/X/A", "http://ex.com/X/{A}", "/X/A", "thing/{A}", "http://ex.com/",     "http://ex.com/thing/A" },
		
	};
	
	@Test public void testing() {
		StringBuilder errors = new StringBuilder();
		for (String [] tc: testCases) {
			try {
				testInverseLookup( tc[1], tc[2], tc[4], tc[3], tc[5], tc[6] );
			} catch (AssertionError e) {
				errors.append( "\n" ).append( "case " ).append( tc[0] ).append( ": " ).append( e.getMessage() );
			}
		}
		if (errors.length() > 0) {
			fail( errors.toString() );
		}
	}

	private void testInverseLookup
		( String requestURI, String itemTemplate, String uriTemplate, String path, String baseURI, String expected ) {
		Setup s = Setup.prepare( baseURI, itemTemplate, uriTemplate );
		String answer = s.invert( requestURI, path );
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
			APISpec spec = new APISpec( EldaFileManager.get(), root, LoadsNothing.instance );
			
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

		String invert( String requestURI, String path ) {
			return r.findItemURIPath( "_", URIUtils.newURI( requestURI ), path );
		}
		
	}
	
	

}
