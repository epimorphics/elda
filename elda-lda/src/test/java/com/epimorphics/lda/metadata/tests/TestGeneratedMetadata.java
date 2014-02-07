/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.metadata.tests;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.cache.tests.TestCaches;
import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.core.APIResultSet.MergedModels;
import com.epimorphics.lda.core.EndpointMetadata;
import com.epimorphics.lda.core.SetsMetadata;
import com.epimorphics.lda.core.View;
import com.epimorphics.lda.query.WantsMetadata;
import com.epimorphics.lda.renderers.Factories.FormatNameAndType;
import com.epimorphics.lda.shortnames.*;
import com.epimorphics.lda.shortnames.CompleteContext.Mode;
import com.epimorphics.lda.sources.Source;
import com.epimorphics.lda.specs.EndpointDetails;
import com.epimorphics.lda.support.Controls;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.lda.vocabularies.OpenSearch;
import com.epimorphics.util.CollectionUtils;
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
	
	@Test public void testAbsentTotalCount() throws URISyntaxException {
		Integer totalResults = null;
		Resource thisMetaPage = createMetadata(totalResults);
		assertFalse( thisMetaPage.hasProperty(OpenSearch.totalResults));
	}
	
	@Test public void testPresentTotalCount() throws URISyntaxException {
		Integer totalResults = new Integer(17);
		Resource thisMetaPage = createMetadata(totalResults);
		Literal tr = thisMetaPage.getModel().createTypedLiteral(totalResults);
		assertTrue( thisMetaPage.hasProperty(OpenSearch.totalResults, tr));
	}

	private Resource createMetadata(Integer totalResults) throws URISyntaxException {
		Model objectModel = ModelFactory.createDefaultModel();
		MergedModels mergedModels = new MergedModels(objectModel);
	//
		Model meta = mergedModels.getMetaModel();
	//
		Resource thisMetaPage = meta.createResource("eh:/thisMetaPage" );
		Resource SEP = meta.createResource("eh:/sparqlEndpoint" );
		thisMetaPage.addProperty(API.sparqlEndpoint, SEP);
		
		Bindings bindings = new Bindings();
		URI ru = new URI(thisMetaPage.getURI());
		Resource uriForDefinition = objectModel.createResource(thisMetaPage.getURI());
		boolean suppressIPTO = true;
		int page = 1, perPage = 10;
		boolean hasMorePages = true;
		Context context = new Context();
		CompleteContext cc = new CompleteContext(Mode.PreferLocalnames, context, objectModel);
	//
		SetsMetadata setsMeta = new SetsMetadata() {

			@Override public void setMetadata(String type, Model meta) {
				
			}};
		WantsMetadata wantsMeta = new WantsMetadata() {

			@Override public boolean wantsMetadata(String name) {
				return true;
			}};
	//	
		Map<String, View> views = new HashMap<String, View>();
		Set<FormatNameAndType> formats = new HashSet<FormatNameAndType>();
	//
		EndpointDetails details = new EndpointDetails() {
			
			@Override public boolean isListEndpoint() {
				return true;
			}
			
			@Override public boolean hasParameterBasedContentNegotiation() {
				return false;
			}
		};
	//
		EndpointMetadata.addAllMetadata
			( mergedModels
			, ru
			, uriForDefinition
			, bindings
			, cc
			, suppressIPTO
			, thisMetaPage
			, page
			, perPage
			, totalResults
			, hasMorePages
			, CollectionUtils.list( objectModel.createResource("eh:/item/_1") )
			, setsMeta
			, wantsMeta
			, "SELECT"
			, "VIEW"
			, new TestCaches.FakeSource("Nemos")
			, views
			, formats
			, details
			);
		return thisMetaPage;
	}

}
