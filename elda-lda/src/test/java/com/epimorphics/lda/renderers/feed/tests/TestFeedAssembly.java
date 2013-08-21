package com.epimorphics.lda.renderers.feed.tests;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.epimorphics.lda.renderers.FeedRenderer;
import com.epimorphics.lda.renderers.FeedRendererFactory;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.tests.SNS;
import com.epimorphics.lda.vocabularies.SKOSstub;
import com.epimorphics.util.CollectionUtils;
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
	
	static final List<Property> defaultLabelProperties = CollectionUtils.list
		( API.label
		, SKOSstub.prefLabel
		, RDFS.label
		);
	
	static final List<Property> defaultDateProperties = CollectionUtils.list
		( DCTerms.modified
		, DCTerms.date
		, DCTerms.dateAccepted
		, DCTerms.dateSubmitted
		, DCTerms.created
		);
	
	@Test public void testRetainsMediaType() {
		FeedRenderer fr = new FeedRenderer( fakeMediaType, config, sns );
		assertSame( fakeMediaType, fr.getMediaType( null ) );
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
	
	protected FeedRenderer makeFeedRenderer( Resource config ) {
		return new FeedRenderer( FeedRendererFactory.atom, config, sns );
	}

}
