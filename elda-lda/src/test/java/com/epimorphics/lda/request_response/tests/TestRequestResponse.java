/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.request_response.tests;

import static org.junit.Assert.fail;

import java.net.URI;
import java.util.ArrayList;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.acceptance.tests.*;
import com.epimorphics.lda.apispec.tests.SpecUtil;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.cache.Cache;
import com.epimorphics.lda.core.*;
import com.epimorphics.lda.log.ELog;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.support.*;
import com.epimorphics.lda.tests_support.MakeData;
import com.epimorphics.util.URIUtils;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.LocationMapper;

// INCOMPLETE (but does more than nothing)
// Code cloned from TestFramework which has turned out to be
// too clumsy to use gracefully. Anticipate generalising this
// and taking the hit, then rebuilding the test-framework examples.
// Doing this now should pay back the next time we need an in-to-out test.
//
public class TestRequestResponse {

    static Logger log = LoggerFactory.getLogger(TestFramework.class);
    
	static final Controls controls = new Controls( true, new Times() );
	
	String turtle_A =
		":root a api:API; api:sparqlEndpoint <here:data>; api:endpoint :ep."
		+ "\n:ep a api:ListEndpoint; api:uriTemplate 'perimeter'."
		+ "\n:data a rdfs:Resource."
		;
	
	String turtle_B =
		":root a api:API; api:sparqlEndpoint <here:data>; api:endpoint :ep"
		+ "  ; api:defaultViewer api:basicViewer."
		+ "\n:ep a api:ListEndpoint; api:uriTemplate 'perimeter'."
		+ "\n:data a rdfs:Resource."
		;
	
	String turtle_C =
		":root a api:API; api:sparqlEndpoint <here:data>; api:endpoint :ep"
		  + "; api:defaultViewer [a api:Viewer; api:name 'chris']."
		+ "\n:ep a api:ListEndpoint; api:uriTemplate 'perimeter'."
		+ "\n:data a rdfs:Resource."
		;
	
	@Test public void testA() {
		testA(turtle_A, "", "description");
		testA(turtle_A, "&_view=all", "all");
		testA(turtle_A, "&_view=basic", "basic");
	}
	
	@Test public void testB() {
		testA(turtle_B, "", "basic");
		testA(turtle_B, "&_view=all", "all");
		testA(turtle_B, "&_view=basic", "basic");
	}
	
	@Test public void testC() {
		testA(turtle_C, "", "chris");
		testA(turtle_C, "&_view=all", "all");
		testA(turtle_C, "&_view=basic", "basic");
	}
	
	/**
	    Test that the config <code>turtle</code>, when run with query
	    parameters including those in <code>queryParts</code>, will generate
	    metadata that has a variable binding of _selectedView to the
	    selected view even if that view is not explicitly named. Relies on
	    revisions to the view code that ensure that the view is named and
	    that we have the version of the name without .copy appended.
	*/
	public void testA(String turtle, String queryParts, String viewName) {
		
		Model specModel = ModelIOUtils.modelFromTurtle(turtle);
		String prefixes = TestFramework.sparqlPrefixesFrom(specModel);
		
		String queryParams = "_metadata=all" + queryParts;
		String path = "";				// foo/bar/baz
		String uriTemplate = "";		// thing/to/match
	//
		ArrayList<Ask> asks = new ArrayList<Ask>();
		Ask ask = new Ask(true, QueryFactory.create
			( prefixes 
			+ "\nask {?v api:value '%A'; api:label '_selectedView'}"
				.replace("%A", viewName)
			));
		asks.add(ask);
	//
		WhatToDo w = new WhatToDo();
	//
		w.template = uriTemplate;
		w.title = "title";
		w.specModel = specModel;
		w.path = path;
		w.queryParams = queryParams;
		w.shouldAppear = asks;
		w.pathToData = "path to data";
	//
		testSingle(w);
	}
	
	public void testSingle(WhatToDo w) {
		Cache.Registry.clearAll();
		log.debug(ELog.message( "[test] running '%s'", w.title) );
//		System.err.println( ">> " + w.pathToData );
	//
	// this little dance of resetting the location mapper bypasses a
	// problem that hits a null pointer exception. FileManager issue?
	//
		EldaFileManager.get().setLocationMapper( new LocationMapper() );
		EldaFileManager.get().getLocationMapper().addAltEntry( "CURRENT-TEST", w.pathToData );
	//
		Model specModel = w.specModel;
		Resource root = specModel.createResource( specModel.expandPrefix( ":root" ) );
		APISpec s = SpecUtil.specFrom( root );
		APIEndpoint ep = new APIEndpointImpl( s.getEndpoints().get(0) ); 
		Bindings epBindings = ep.getSpec().getBindings();
		MultiMap<String, String> map = MakeData.parseQueryString( w.queryParams );
		URI ru = URIUtils.newURI(w.path);
		Bindings cc = Bindings.createContext( TestFramework.bindTemplate( epBindings, w.template, w.path, map ), map );
		ResponseResult resultsAndFormat = ep.call( new APIEndpoint.Request( controls, ru, cc ), new NoteBoard() );
		Model rsm = resultsAndFormat.resultSet.getMergedModel();
//		System.err.println( ">> " + rs.getResultList() );				
//		System.err.println( "||>> " + resultsAndFormat.a.getSelectQuery() );

		for (Ask a: w.shouldAppear)
			{
//			System.err.println( ">>  asking ... " + (a.isPositive ? "ASSERT" : "DENY") );
			QueryExecution qe = QueryExecutionFactory.create( a.ask, rsm );
			if (qe.execAsk() != a.isPositive)
				{
//				System.err.println( ">> WHOOPS------------------------------------__" );
//				System.err.println( ">> path: " + w.path );
//				System.err.println( ">> qp: " + w.queryParams );
//				System.err.println( ">> template: " + w.template );
//				System.err.println( ">> ------------------------------------------__" );
//				System.err.println( resultsAndFormat.a.getSelectQuery() );
//				System.err.println( ">> ------------------------------------------__" );
//				System.err.println( ">> cc = " + cc );
//				System.err.println( ">> ------------------------------------------__" );
				// System.err.println( ">>\n>> Failing result model for " + w.title + ":" );
				// rsm.write( System.err, "TTL" );
				fail
					( "test " + w.title + ": the probe query\n"
					+ TestFramework.shortStringFor( a ) + "\n"
					+ "failed for the result set\n"
					+ TestFramework.shortStringFor( rsm )
					)
					;			
				}
			}
		
		
	}

}
