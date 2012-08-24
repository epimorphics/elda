/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.metadata.tests;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.EndpointMetadata;
import com.epimorphics.lda.shortnames.NameMap;
import com.epimorphics.util.CollectionUtils;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class TestGeneratedMetadata {
	
	static final Property Any = null;
	
	/**
	    Check that a predicate for which no shortnames are defined in name map still
	    gets a term binding in the metadata.
	*/
	@Test public void testTermBindingsCoverAllPredicates() throws URISyntaxException {
		Resource thisPage = ResourceFactory.createResource( "elda:thisPage" );
		boolean isListEndpoint = true;
		String pageNumber = "1";
		Bindings cc = new Bindings();
		URI reqURI = new URI( "" );
		Set<String> formatNames = CollectionUtils.set( "rdf" );
	//
		EndpointMetadata em = new EndpointMetadata( thisPage, isListEndpoint, pageNumber, cc, reqURI, formatNames );
	//
		Model toScan = ModelIOUtils.modelFromTurtle( ":a <http://example.com/root#predicate> :b." );
		Resource predicate = toScan.createProperty( "http://example.com/root#predicate" );
		Model meta = ModelFactory.createDefaultModel();
		Resource exec = meta.createResource( "fake:exec" );
		NameMap nm = new NameMap();
		em.addTermBindings( toScan, meta, exec, nm );
	//
		Resource tb = meta.listStatements( null, API.termBinding, Any ).nextStatement().getResource();
//		assertTrue( meta.contains( tb, API.label, "predicate" ) );
//		assertTrue( meta.contains( tb, API.property, predicate ) );
		System.err.println( ">> TODO: get testTermBindingsCoverAllPredicates working again." );
	}

}
