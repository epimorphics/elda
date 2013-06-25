package com.epimorphics.lda.shortnames.tests;

import static org.junit.Assert.assertEquals;

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
		assertEquals( "unencoded", Transcoding.decode( pm, "unknown_unencoded" ) );
		assertEquals( "en\u00AAcoded", Transcoding.decode( pm, "unknown_en_aacoded" ) );
		assertEquals( "en\u00BBcoded", Transcoding.decode( pm, "unknown_en_bbcoded" ) );
		assertEquals( "en\u00AA\u00BBcoded", Transcoding.decode( pm, "unknown_en_aa_bbcoded" ) );
		assertEquals( "en\u0012coded", Transcoding.decode( pm, "unknown_en_12coded" ) );
	}

	static final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	@Test public void testUnderbarMajorEscapes() {
		assertEquals( "x_", Transcoding.decode( pm, "unknown_x__" ) );
		for (int i = 0; i < alphabet.length(); i += 1) {
			char ch = alphabet.charAt(i);
			assertEquals( "x" + ch, Transcoding.decode( pm, "unknown_x_" + ch ) );
		}
	}
	
	@Test public void testEncodedLocalname() {
		assertEquals( RDF.getURI() + "something", Transcoding.decode( pm, "pre_rdf_something" ) );
		assertEquals( RDF.getURI() + "some\u0055ing", Transcoding.decode( pm, "pre_rdf_some_55ing" ) );
		assertEquals( RDF.getURI() + "somethin\u003Fg", Transcoding.decode( pm, "pre_rdf_somethin_3fg" ) );
	}
	
	@Test public void testEncode() {
		assertEquals( "unknown_httpXZdomain", Transcoding.encode( pm, "http://domain" ) );
		assertEquals( "unknown_httpXdomainSZother", Transcoding.encode( pm, "http://domain/other" ) );
		assertEquals( "rdf_first", Transcoding.encode( pm, RDF.first.getURI() ) );
		assertEquals( "pre_rdf_aMb", Transcoding.encode( pm, RDF.getURI() + "a-b") );
	}
	
	@Test public void testInversion() {
		String example = "http://www.nosuchproperty3/broad_Modifier/localname";
		String encoded = Transcoding.encode(pm, example);
		String decoded = Transcoding.decode(pm, encoded);
		assertEquals( example, decoded );
	}
	
}
