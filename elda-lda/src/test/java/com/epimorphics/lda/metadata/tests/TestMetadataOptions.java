package com.epimorphics.lda.metadata.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.cache.tests.TestCaches;
import com.epimorphics.lda.core.APIResultSet.MergedModels;
import com.epimorphics.lda.metadata.MetaConfig;
import com.epimorphics.lda.core.EndpointMetadata;
import com.epimorphics.lda.core.SetsMetadata;
import com.epimorphics.lda.core.View;
import com.epimorphics.lda.query.WantsMetadata;
import com.epimorphics.lda.renderers.Factories.FormatNameAndType;
import com.epimorphics.lda.shortnames.CompleteContext;
import com.epimorphics.lda.shortnames.CompleteContext.Mode;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.specs.EndpointDetails;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.util.CollectionUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;

public class TestMetadataOptions {

	static final Property Any = null;

	final Model config = ModelFactory.createDefaultModel();

	final Resource sparql = config.createResource("eh:/sparqlEndpoint");
	
	final Resource root = config.createResource("eh:/the-spec");

	final Resource theEndpoint = config.createResource("eh:/the-endpoint");
	
	@Test public void testingDefaultMetaFalse() throws URISyntaxException {
		MetaConfig mc = new MetaConfig(false);
		Set<Property> hardwiredProperties = EndpointMetadata.hardwiredProperties;
		
		Model config = ModelFactory.createDefaultModel();
		assertTrue(hardwiredProperties.contains(config.createProperty("http://purl.org/linked-data/api/vocab#wasResultOf")));
		testConfigProperties(hardwiredProperties, mc);
	}
	
	@Test public void testingDefaultMetaTrue() throws URISyntaxException {
		MetaConfig mc = new MetaConfig(true);
		testConfigProperties(new HashSet<Property>(), mc);
	}
	
	public void testConfigProperties(Set<Property> expected, MetaConfig mc) throws URISyntaxException {	
		Resource metaPage = assembleMetadata(mc, true, new Integer(10));
		Set<Property> properties = new HashSet<Property>();
		for (Statement s: metaPage.listProperties().toList()) {
			properties.add(s.getPredicate());
		}
	//
	// api:items is a special case because it must always be there.
	//
		properties.remove(API.items);
		expected.remove(API.items);
	//
		assertEquals(expected, properties);
	}
	
	public Resource assembleMetadata
		( MetaConfig mc
		, boolean isListEndpoint
		, Integer totalResults
		) throws URISyntaxException {
				
		config.add(root, RDF.type, API.API);
		config.add(root, API.sparqlEndpoint, sparql);
		config.add(root, API.endpoint, theEndpoint);

		config.add(theEndpoint, RDF.type, API.ListEndpoint);
		config.add(theEndpoint, API.uriTemplate, "/an/endpoint");
		
		APISpec aspec = new APISpec
			( mc
			, FileManager.get()
			, root
			, TestGeneratedMetadata.loader
			)
			;
				
		APIEndpointSpec espec = aspec.getEndpoints().get(0);
		
		SetsMetadata setsMeta = new SetsMetadata() {

			@Override public void setMetadata(String type, Model meta) {
				
			}};
		
		WantsMetadata wantsMeta = new WantsMetadata() {

			@Override public boolean wantsMetadata(String name) {
				return true;
			}};	
			
		EndpointDetails details = new EndpointDetails() {
				
			@Override public boolean isListEndpoint() {
				return isListEndpoint;
			}
				
			@Override public boolean hasParameterBasedContentNegotiation() {
				return false;
			}
		};

		Model objectModel = ModelFactory.createDefaultModel();
		MergedModels mergedModels = new MergedModels(objectModel);
		Model meta = mergedModels.getMetaModel();
		Resource thisMetaPage = meta.createResource("eh:/thisMetaPage" );
		URI ru = new URI(thisMetaPage.getURI());
		Bindings bindings = new Bindings();
		boolean suppressIPTO = true;	
		boolean hasMorePages = true;
		int page = 1, perPage = 10;
		Context context = new Context();
		CompleteContext cc = new CompleteContext(Mode.PreferLocalnames, context, objectModel);
		
		Resource uriForDefinition = objectModel.createResource(thisMetaPage.getURI());

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
			, new HashMap<String, View>()
			, new HashSet<FormatNameAndType>()
			, details
			, new HashSet<Resource>() // no licences
			);
		return thisMetaPage;
	}

}
