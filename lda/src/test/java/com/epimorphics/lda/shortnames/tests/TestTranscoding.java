package com.epimorphics.lda.shortnames.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.epimorphics.lda.shortnames.Transcoding;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.RDF;

public class TestTranscoding {
	
	PrefixMapping pm = PrefixMapping.Factory.create()
		.setNsPrefix( "rdf", RDF.getURI() );

	@Test public void testUnprefixed() {
		assertEquals( null, Transcoding.decode( pm, "something" ) );
		assertEquals( null, Transcoding.decode( pm, "not_something" ) );
		assertEquals( RDF.getURI() + "something", Transcoding.decode( pm, "rdf_something" ) );
	}
	
	@Test public void testEncodedURI() {
		assertEquals( "unencoded", Transcoding.decode( pm, "uri_unencoded" ) );
		assertEquals( "en\u00AAcoded", Transcoding.decode( pm, "uri_enAAcoded" ) );
		assertEquals( "en\u00BBcoded", Transcoding.decode( pm, "uri_enBBcoded" ) );
		assertEquals( "en\u00AA\u00BBcoded", Transcoding.decode( pm, "uri_enAABBcoded" ) );
		assertEquals( "en\u0012coded", Transcoding.decode( pm, "uri_en12coded" ) );
	}
	
	@Test public void testEncodedLocalname() {
		assertEquals( RDF.getURI() + "something", Transcoding.decode( pm, "pre_rdf_something" ) );
		assertEquals( RDF.getURI() + "some\u0055ing", Transcoding.decode( pm, "pre_rdf_some55ing" ) );
		assertEquals( RDF.getURI() + "somethin\u003Fg", Transcoding.decode( pm, "pre_rdf_somethin3Fg" ) );
	}
	
	@Test public void testEncode() {
		assertEquals( "uri_http3A2F2Fdomain", Transcoding.encode( pm, "http://domain" ) );
		assertEquals( "uri_http3A2F2Fdomain2Fother", Transcoding.encode( pm, "http://domain/other" ) );
		assertEquals( "rdf_first", Transcoding.encode( pm, RDF.first.getURI() ) );
		assertEquals( "pre_rdf_a2Db", Transcoding.encode( pm, RDF.getURI() + "a-b") );
	}
	
}
