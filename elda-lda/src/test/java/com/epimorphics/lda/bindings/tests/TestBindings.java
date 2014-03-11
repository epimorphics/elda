/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.bindings.tests;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.apispec.tests.SpecUtil;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.*;
import com.epimorphics.lda.routing.Match;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.support.*;
import com.epimorphics.lda.tests_support.MakeData;
import com.epimorphics.util.Triad;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class TestBindings {
	
	static final Controls controls = new Controls( true, new Times() );
	
	/**
	    Test that declared variables can have values that depend on
	    bindings made during URI matching.
	*/
	@Test public void testDeclaredVariablesCanDependOnURIMatches() throws URISyntaxException {
		String specString =
			"@prefix t: <http://example.com/t#>."
			+ "t:chris a api:API ;"
			+ "\n api:sparqlEndpoint <here:example> ;"
			+ "\n api:endpoint t:properties-bwq." 
			+ "\n"
			+ "\n t:properties-bwq a api:ListEndpoint ;"
			+ "\n api:uriTemplate '/def/bathing-water-quality/{term}/property';"
			+ "\n api:variable ["
			+ "\n     api:name 'class' ;"
			+ "\n     api:value 'http://environment.data.gov.uk/def/bathing-water-quality/{term}' ;"
			+ "\n     api:type rdfs:Resource" 
			+ "\n ];"
			+ "\n api:selector [api:where '[] a ?class ; ?item [] .']."
			;
		Model m = ModelIOUtils.modelFromTurtle( specString );
		Resource root = m.createResource( m.expandPrefix( "t:chris" ) );
		APISpec spec = SpecUtil.specFrom( root );
		APIEndpointSpec eps = spec.getEndpoints().get(0);
		APIEndpointImpl ep = new APIEndpointImpl( eps );
		Match match = new Match( "_", ep, MakeData.hashMap( "term=autumn" ) );
		URI req = new URI( "/driver/cartwheel" );
		MultiMap<String, String> params = MakeData.parseQueryString( "" );
		
		APIEndpoint.Request r = new APIEndpoint.Request( controls, req, new Bindings() ).withFormat("xml");
		
		ResponseResult results = APIEndpointUtil.call( r, new NoteBoard(), match, "s", params );
//		System.err.println( ">> class: " + results.c.getStringValue( "class" ) );
		String sq = results.resultSet.getSelectQuery();
//		System.err.println( ">> " + sq );
		String expected = "[] a <http://environment.data.gov.uk/def/bathing-water-quality/autumn>";
		if (!sq.contains( expected ))
			Assert.fail( "query\n" + sq + "\n should contain\n" + expected + "\nbut does not." );
	}
}
