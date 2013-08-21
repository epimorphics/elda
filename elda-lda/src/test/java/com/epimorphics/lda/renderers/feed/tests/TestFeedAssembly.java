package com.epimorphics.lda.renderers.feed.tests;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.epimorphics.lda.renderers.FeedRenderer;
import com.epimorphics.lda.renderers.FeedRendererFactory;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.tests.SNS;
import com.epimorphics.lda.vocabularies.EXTRAS;
import com.epimorphics.lda.vocabularies.SKOSstub;
import static com.epimorphics.util.CollectionUtils.list;
import com.epimorphics.util.MediaType;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDFS;

public class TestFeedAssembly {
	
	protected ShortnameService sns = new SNS("");
	
	protected final MediaType fakeMediaType = new MediaType( "media", "type" );
	
	protected Model configModel = ModelFactory.createDefaultModel();
	
	protected final Resource config = configModel.createResource( "eh:/root" );
	
	static final List<Property> defaultLabelProperties = list
		( API.label
		, SKOSstub.prefLabel
		, RDFS.label
		);
	
	static final List<Property> defaultDateProperties = list
		( DCTerms.modified
		, DCTerms.date
		, DCTerms.dateAccepted
		, DCTerms.dateSubmitted
		, DCTerms.created
		);
	
	@Test public void testRetainsMediaType() {
		FeedRenderer retains_fr = new FeedRenderer( fakeMediaType, config, sns );
		assertSame( fakeMediaType, retains_fr.getMediaType( null ) );
	}
	
	@Test public void testDefaultLabelProperties() {
		FeedRenderer fr = makeFeedRenderer( config );
		List<Property> properties = fr.getLabelProperties(config);
		assertEquals( defaultLabelProperties, properties );
	}
	
	@Test public void testDefaultDateProperties() {
		FeedRenderer fr = makeFeedRenderer( config );
		List<Property> properties = fr.getDateProperties(config);
		assertEquals( defaultDateProperties, properties );
	}
	
	// use the default date properties as a convenient set of examples
	@Test public void testConfiguredSingleDateProperties() {
		for (Property p: defaultDateProperties) {
			assertEquals( list(p), makeDateFeedRenderer(p).getDateProperties(config));			
		}
	}	
	
	// assumes there are at least two default date properties
	@Test public void testConfiguredMultiDateProperties() {
		for (int i = 1; i < defaultDateProperties.size(); i += 1) {
			Property a = defaultDateProperties.get(i-1), b = defaultDateProperties.get(i);
			assertEquals( list(a, b), makeDateFeedRenderer(a, b).getDateProperties(config));			
		}
	}
	
	private FeedRenderer makeDateFeedRenderer(Property ...properties) {
		RDFList l = configModel.createList( properties );
		config.addProperty( EXTRAS.feedDateProperties, l );
		return makeFeedRenderer(config);
	}

	// use the default label properties as a convenient set of examples
	@Test public void testConfiguredSingleLabelProperties() {
		for (Property p: defaultLabelProperties) {
			assertEquals( list(p), makeDateFeedRenderer(p).getDateProperties(config));			
		}
	}
	
	// assumes there are at least two default date properties
	@Test public void testConfiguredMultiLabelProperties() {
		for (int i = 1; i < defaultLabelProperties.size(); i += 1) {
			Property a = defaultLabelProperties.get(i-1), b = defaultLabelProperties.get(i);
			assertEquals( list(a, b), makeDateFeedRenderer(a, b).getDateProperties(config));			
		}
	}
	
	protected FeedRenderer makeFeedRenderer( Resource config ) {
		return new FeedRenderer( FeedRendererFactory.atom, config, sns );
	}

}
