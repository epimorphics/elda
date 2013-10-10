package com.epimorphics.lda.renderers.feed.tests;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.Test;
import org.w3c.dom.Document;

import com.epimorphics.lda.core.APIResultSet.MergedModels;
import com.epimorphics.lda.renderers.FeedRenderer;
import com.epimorphics.lda.renderers.FeedRendererFactory;
import com.epimorphics.lda.renderers.FeedRenderer.FeedResults;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.support.Times;
import com.epimorphics.lda.tests.SNS;
import com.epimorphics.lda.vocabularies.*;
import static com.epimorphics.util.CollectionUtils.list;

import com.epimorphics.util.DOMUtils;
import com.epimorphics.util.MediaType;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDFS;

import org.custommonkey.xmlunit.*;
import org.custommonkey.xmlunit.exceptions.XpathException;

public class TestFeedAssembly {
	
	protected final ShortnameService sns = new SNS("");
	
	protected final MediaType fakeMediaType = new MediaType( "media", "type" );
	
	protected final Model configModel = ModelFactory.createDefaultModel();

	protected final Model dataModel = ModelFactory.createDefaultModel();
	
	protected final Resource item = dataModel.createResource( "eh:/item" );
	
	protected final Resource config = configModel.createResource( "eh:/root" );
	
	static final List<Property> defaultLabelProperties = list
		( API.label
		, SKOSstub.prefLabel
		, RDFS.label
		);
	
	static final List<Property> defaultAuthorProperties = list
		( DCTerms.creator
		, DCTerms.contributor
		);
	
	static final List<Property> defaultRightsProperties = list
		( DCTerms.rights
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
		assertEquals( defaultDateProperties, fr.getDateProperties(config) );
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
	
	@Test public void testDefaultRightsProperties() {
		FeedRenderer fr = makeFeedRenderer( config );
		assertEquals( defaultRightsProperties, fr.getRightsProperties() );
	}
	
	@Test public void testDeConfiguredRightsProperties() {
		Property a = configModel.createProperty( "eh:/a" );
		Property b = configModel.createProperty( "eh:/b" );
		Property c = configModel.createProperty( "eh:/c" );
		Property[] properties = new Property[] {a, b, c};
		RDFList l = configModel.createList( properties );
		config.addProperty( EXTRAS.feedRightsProperties, l );
		FeedRenderer fr = makeFeedRenderer( config );
		assertEquals( Arrays.asList( properties ), fr.getRightsProperties() );
	}
	
	@Test public void testDefaultFeedRights() {
		FeedRenderer fr = makeFeedRenderer( config );
		assertNull( fr.getFeedRights() );
	}
	
	@Test public void testConfiguredFeedRights() {
		String rightsString = "© copyright 1066";
		config.addProperty( EXTRAS.feedRights, rightsString );
		FeedRenderer fr = makeFeedRenderer( config );
		assertEquals( rightsString, fr.getFeedRights() );
	}
	
	@Test public void testDefaultNamespace() {
		FeedRenderer fr = makeFeedRenderer( config );
		assertEquals( EXTRAS.getURI(), fr.getNamespace() );
	}

	@Test public void testDefaultAuthors() {
		FeedRenderer fr = makeFeedRenderer( config );
		assertTrue( fr.getAuthors().isEmpty() );
	}

	@Test public void testConfiguredAuthors() {
		Literal a = configModel.createLiteral( "author_A" );
		Resource b = configModel.createResource( "eh:/authorB" );
		RDFNode[] properties = new RDFNode[] {a, b};
		RDFList l = configModel.createList( properties );
		config.addProperty( EXTRAS.feedAuthors, l );
		FeedRenderer fr = makeFeedRenderer( config );
		assertEquals( list( a, b ), fr.getAuthors() );
	}
	
	@Test public void testDefaultAuthorProperties() {
		FeedRenderer fr = makeFeedRenderer( config );
		assertEquals( defaultAuthorProperties, fr.getAuthorProperties() );
	}
	
	@Test public void testConfiguredAuthorProperties() {
		Property a = configModel.createProperty( "eh:/A" );
		Property b = configModel.createProperty( "eh:/B" );
	//
		Property[] properties = new Property[] {a, b};
		RDFList l = configModel.createList( properties );
		config.addProperty( EXTRAS.feedAuthorProperties, l );
	//
		FeedRenderer fr = makeFeedRenderer( config );
		assertEquals( list(a, b), fr.getAuthorProperties() );
	}
	
	@Test public void testRendersFeedAuthorsIfPresent() {		
		Resource a = configModel.createProperty( "eh:/author_A" );
		RDFNode b = configModel.createLiteral( "author_B" );
	//
		RDFNode[] properties = new RDFNode[] {a, b};
		RDFList authors = configModel.createList( properties );
		config.addProperty( EXTRAS.feedAuthors, authors );
	//
		String rendering = renderFeed();
	//
		if (!rendering.contains( "<author>" + a.toString() + "</author>" )) {
			fail( "rendering\n" + rendering + "\nshould contain author\n" + a );
		}
	//
		if (!rendering.contains( "<author>" + b.toString() + "</author>" )) {
			fail( "rendering\n" + rendering + "\nshould contain author\n" + b );
		}
	}
	
	@Test public void testRendersManagesCyclesIfPresent() {	
		Property a = configModel.createProperty( "eh:/author_A" );
		Property b = configModel.createProperty( "eh:/author_B" );
		RDFNode[] properties = new RDFNode[] {a, b};
		RDFList authors = configModel.createList( properties );		
		config.addProperty( EXTRAS.feedAuthorProperties, authors );
	//
		item.addProperty( a, "the author is A" );
		item.addProperty( b, "the author is B" );
		item.addProperty( a, item );
	//
		renderFeed();
	//
		// we don't get here if the cycle isn't handled.
	}
	
	@Test public void testRendersEntryAuthorsIfPresent() {	
		Property a = configModel.createProperty( "eh:/author_A" );
		Property b = configModel.createProperty( "eh:/author_B" );
		RDFNode[] properties = new RDFNode[] {a, b};
		RDFList authors = configModel.createList( properties );		
		config.addProperty( EXTRAS.feedAuthorProperties, authors );
	//
		item.addProperty( a, "the author is A" );
		item.addProperty( b, "the author is B" );
		
		String rendering = renderFeed();
	//
		if (!rendering.contains( "<author>the author is A</author>" )) {
			fail( "rendering\n" + rendering + "\nshould contain author\n" + a );
		}
	//
		if (rendering.contains( "<author>the author is B</author>" )) {
			fail( "rendering\n" + rendering + "\nshould not contain author\n" + b );
		}
	}
	
	@Test public void testRendersFeedRightsIfPresent() {

		String rightsString = "© copyright 1066";
		config.addProperty( EXTRAS.feedRights, rightsString );
		
		String rendering = renderFeed();
		
		if (!rendering.contains( "<rights>" + rightsString + "</rights>" )) {
			fail( "rendering\n" + rendering + "\nshould contain rights \n" + rightsString );
		}
	}

	private String renderFeed() {
		FeedRenderer fr = makeFeedRenderer( config );
		Document  d = DOMUtils.newDocument();
		List<Resource> items = new ArrayList<Resource>();
		items.add( item );
	//
		FeedResults results = new FeedResults( config, items, new MergedModels( dataModel ) );
	//
		Map<String, String> tb = new HashMap<String, String>();
		tb.put( "eh:/author_A", "A" );
		tb.put( "eh:/author_B", "B" );
	//
		fr.renderFeedIntoDocument( d, tb, results );
		Times t = new Times();
		PrefixMapping pm = PrefixMapping.Factory.create();
		String rendering = DOMUtils.renderNodeToString( t, d, pm );
		return rendering;
	}
	
	@Test public void testRendersEntryRightsIfPresent() throws XpathException {

		String rightsString = "© copyright 1066";
		FeedRenderer fr = makeFeedRenderer( config );
		
		Document  d = DOMUtils.newDocument();
		List<Resource> items = new ArrayList<Resource>();
		
		item.addProperty( DCTerms.rights, rightsString );
		items.add( item );
		
		FeedResults results = new FeedResults( config, items, new MergedModels( dataModel ) );

		fr.renderFeedIntoDocument( d, new HashMap<String, String>(), results );
		Times t = new Times();
		PrefixMapping pm = PrefixMapping.Factory.create();
		String rendering = DOMUtils.renderNodeToString( t, d, pm );
		
		XMLAssert.assertXpathExists( "feed/entry/rights", d );
		
		if (!rendering.contains( "<rights>" + rightsString + "</rights>" )) {
			fail( "rendering\n" + rendering + "\nshould contain rights \n" + rightsString );
		}
	}
	
	@Test public void testConfiguredNamespace() {
		String explicit = "eh:/explicitNamespace";
		config.addProperty( EXTRAS.feedNamespace, explicit );
		FeedRenderer fr = makeFeedRenderer( config );
		assertEquals( explicit, fr.getNamespace() );
	//
		Document  d = DOMUtils.newDocument();
		List<Resource> items = new ArrayList<Resource>();
	//
		item.addProperty( RDFS.label, "Please Look After This Bear" );
		items.add( item );
	//
		FeedResults results = new FeedResults( config, items, new MergedModels( dataModel ) );
		fr.renderFeedIntoDocument( d, new HashMap<String, String>(), results );
	//
		String namespace = explicit;
		Times t = new Times();
		PrefixMapping pm = PrefixMapping.Factory.create();
		String rendering = DOMUtils.renderNodeToString( t, d, pm );
	//
		if (!rendering.contains( "<content xmlns=\"" + namespace + "\"" ))
			fail( "rendering \n" + rendering + "\n does not contain correct content namespace\n" + namespace );
	}
	
	@Test public void testNamespaceEmbedsInContent() {
		
	}
	
	@Test public void testSingleItemXMLrendering() throws XpathException {
		FeedRenderer fr = makeFeedRenderer( config );
		Map<String, String> termBindings = new HashMap<String, String>();
		Document  d = DOMUtils.newDocument();
	//
		List<Resource> items = new ArrayList<Resource>();
	//
		item.addProperty( RDFS.label, "Please Look After This Bear" );
		items.add( item );
	//
		FeedResults results = new FeedResults( config, items, new MergedModels( dataModel ) );
		fr.renderFeedIntoDocument( d, termBindings, results );
	//
		XMLAssert.assertXpathExists( "feed", d );
		XMLAssert.assertXpathExists( "feed/title", d );
		XMLAssert.assertXpathExists( "feed/link", d );
		XMLAssert.assertXpathExists( "feed/id", d );
		XMLAssert.assertXpathExists( "feed/updated", d );
		XMLAssert.assertXpathExists( "feed/entry", d );
	//
		XMLAssert.assertXpathExists( "feed/entry/title", d );
		XMLAssert.assertXpathExists( "feed/entry/updated", d );
		XMLAssert.assertXpathExists( "feed/entry/id", d );
		XMLAssert.assertXpathExists( "feed/entry/content", d );
	//
		XMLAssert.assertXpathExists( "feed/entry/content/label", d );
	//
		// TODO check that shortname translated to correct namespace.
		XMLAssert.assertXpathExists( "feed/entry/content/label", d );
		
		String namespace = "http://www.epimorphics.com/vocabularies/lda#";
		Times t = new Times();
		PrefixMapping pm = PrefixMapping.Factory.create();
		String rendering = DOMUtils.renderNodeToString( t, d, pm );
	//
		if (!rendering.contains( "<content xmlns=\"" + namespace + "\"" ))
			fail( "rendering \n" + rendering + "\n does not contain correct content namespace\n" + namespace );
	}
	
	protected FeedRenderer makeFeedRenderer( Resource config ) {
		return new FeedRenderer( FeedRendererFactory.atom, config, sns );
	}

}
