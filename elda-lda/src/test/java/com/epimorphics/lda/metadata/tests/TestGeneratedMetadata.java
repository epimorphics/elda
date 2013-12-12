/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.metadata.tests;

import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.core.EndpointMetadata;
import com.epimorphics.lda.shortnames.*;
import com.epimorphics.lda.specs.EndpointDetails;
import com.epimorphics.lda.support.Controls;
import com.epimorphics.lda.vocabularies.API;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TestGeneratedMetadata {
	
	static final Property Any = null;
	
	/**
	    Check that a predicate for which no shortnames are defined in name map still
	    gets a term binding in the metadata.
	*/
	@Test public void testTermBindingsCoverAllPredicates() throws URISyntaxException {
		Resource thisPage = ResourceFactory.createResource( "elda:thisPage" );
		String pageNumber = "1";
		Bindings cc = new Bindings();
		URI reqURI = new URI( "" );
	//
		EndpointDetails spec = new EndpointDetails() {

			@Override public boolean isListEndpoint() {
				return true;
			}

			@Override public boolean hasParameterBasedContentNegotiation() {
				return false;
			}
		};
		EndpointMetadata em = new EndpointMetadata( spec, thisPage, pageNumber, cc, reqURI );
	//
		PrefixMapping pm = PrefixMapping.Factory.create().setNsPrefix( "this", "http://example.com/root#" );
		Model toScan = ModelIOUtils.modelFromTurtle( ":a <http://example.com/root#predicate> :b." );
		toScan.setNsPrefixes( pm );
		Resource predicate = toScan.createProperty( "http://example.com/root#predicate" );
		Model meta = ModelFactory.createDefaultModel();
		Resource exec = meta.createResource( "fake:exec" );
		ShortnameService sns = new StandardShortnameService();
		APIEndpoint.Request r = new APIEndpoint.Request( new Controls(), reqURI, cc );
		
		CompleteContext c  = 
			new CompleteContext(CompleteContext.Mode.PreferPrefixes, sns.asContext(), pm )
			.include(toScan);
		
		em.addTermBindings( toScan, meta, exec, c );
		
		Map<String, String> termBindings = c.Do();
		Resource tb = meta.listStatements( null, API.termBinding, Any ).nextStatement().getResource();
		assertTrue( meta.contains( tb, API.label, "this_predicate" ) );
		assertTrue( meta.contains( tb, API.property, predicate ) );
	}

}
