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
import java.util.*;

import org.junit.Test;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.cache.tests.TestCaches;
import com.epimorphics.lda.core.*;
import com.epimorphics.lda.core.APIResultSet.MergedModels;
import com.epimorphics.lda.query.WantsMetadata;
import com.epimorphics.lda.renderers.Factories.FormatNameAndType;
import com.epimorphics.lda.shortnames.*;
import com.epimorphics.lda.shortnames.CompleteContext.Mode;
import com.epimorphics.lda.specs.*;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.lda.vocabularies.OpenSearch;
import com.epimorphics.util.CollectionUtils;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class TestGeneratedMetadata {
	
	static final Property Any = null;
	
	/**
	    Check that a predicate for which no shortnames are defined in name map still
	    gets a term binding in the metadata.
	*/
	@Test public void testTermBindingsCoverAllPredicates() throws URISyntaxException {
		Resource thisPage = ResourceFactory.createResource( "elda:thisPage" );
		String pageNumber = "1";
		Bindings noBindings = new Bindings();
	//
		EndpointDetails spec = new EndpointDetails() {

			@Override public boolean isListEndpoint() {
				return true;
			}

			@Override public boolean hasParameterBasedContentNegotiation() {
				return false;
			}
		};
		EndpointMetadata em = new EndpointMetadata( spec, thisPage, pageNumber, noBindings );
	//
		PrefixMapping pm = PrefixMapping.Factory.create().setNsPrefix( "this", "http://example.com/root#" );
		Model toScan = ModelIOUtils.modelFromTurtle( ":a <http://example.com/root#predicate> :b." );
		toScan.setNsPrefixes( pm );
		Resource predicate = toScan.createProperty( "http://example.com/root#predicate" );
		Model meta = ModelFactory.createDefaultModel();
		Resource exec = meta.createResource( "fake:exec" );
		ShortnameService sns = new StandardShortnameService();
		
		CompleteContext cc  = 
			new CompleteContext(CompleteContext.Mode.PreferPrefixes, sns.asContext(), pm )
			;
		
		em.addTermBindings( toScan, meta, exec, cc );
		
		@SuppressWarnings("unused") Map<String, String> termBindings = cc.Do();
		Resource tb = meta.listStatements( null, API.termBinding, Any ).nextStatement().getResource();
		assertTrue( meta.contains( tb, API.label, "this_predicate" ) );
		assertTrue( meta.contains( tb, API.property, predicate ) );
	}
	
	@Test public void testAbsentTotalCount() throws URISyntaxException {
		Integer totalResults = null;
		Resource thisMetaPage = createMetadata(true, totalResults);
		assertFalse( thisMetaPage.hasProperty(OpenSearch.totalResults));
	}
	
	@Test public void testPresentTotalCount() throws URISyntaxException {
		Integer totalResults = new Integer(17);
		Resource thisMetaPage = createMetadata(true, totalResults);
		Literal tr = thisMetaPage.getModel().createTypedLiteral(totalResults);
		assertTrue( thisMetaPage.hasProperty(OpenSearch.totalResults, tr));
	}
	
	@Test public void testListEndpointHasCorrectType() throws URISyntaxException {
		Integer totalResults = null;
		Resource thisMetaPage = createMetadata(true, totalResults);
		assertTrue(thisMetaPage.hasProperty(RDF.type, API.ListEndpoint));
	}
	
	@Test public void testItemEndpointHasCorrectType() throws URISyntaxException {
		Integer totalResults = null;
		Resource thisMetaPage = createMetadata(false, totalResults);
		assertTrue(thisMetaPage.hasProperty(RDF.type, API.ItemEndpoint));
	}
	
	static final Property[] expectedTermboundProperties = new Property[] 
		{ RDFS.label
		, RDF.value
		, RDF.type
		};

	@Test public void testTermbindsIncludesMetaproperties() throws URISyntaxException {
		Integer totalResults = null;
		Resource thisMetaPage = createMetadata(false, totalResults);
		
		for (Property p: expectedTermboundProperties) {
			Model model = thisMetaPage.getModel();
			if (!model.contains(null, API.property, p)) {
				fail("term bindings should include " + model.shortForm(p.getURI()));
			}
		}
	}

	static final ModelLoader loader = new ModelLoader() {

		@Override public Model loadModel(String uri) {
			return null;
		}
		
	};
	
	final Model config = ModelFactory.createDefaultModel();

	final Resource root = config.createResource("eh:/the-spec");

	final Resource sparql = config.createResource("eh:/sparqlEndpoint");
	
	final Resource theEndpoint = config.createResource("eh:/the-endpoint");
	
	private Resource createMetadata(final boolean isListEndpoint, Integer totalResults) throws URISyntaxException {
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
				return isListEndpoint;
			}
			
			@Override public boolean hasParameterBasedContentNegotiation() {
				return false;
			}
		};
	//
		config.add(root, RDF.type, API.API);
		config.add(root, API.sparqlEndpoint, sparql);
		config.add(root, API.endpoint, theEndpoint);
		
		config.add(theEndpoint, RDF.type, API.ListEndpoint);
		config.add(theEndpoint, API.uriTemplate, "/an/endpoint");
		
		APISpec aspec = new APISpec(FileManager.get(), root, loader);
		APIEndpointSpec espec = aspec.getEndpoints().get(0);
	//
		EndpointMetadata.addAllMetadata
			( espec
			, mergedModels
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
			, new HashSet<Resource>() // no licences
			);
		return thisMetaPage;
	}

}
