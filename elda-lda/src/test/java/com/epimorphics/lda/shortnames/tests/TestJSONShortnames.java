package com.epimorphics.lda.shortnames.tests;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.epimorphics.lda.renderers.json.JSONPropertyNaming;
import com.epimorphics.lda.tests_support.MakeData;
import com.epimorphics.util.CollectionUtils;
import com.hp.hpl.jena.shared.PrefixMapping;

/**
 	Tests for constructing JSON shortnames when rendering.
*/
public class TestJSONShortnames {
	
	@Test public void testSingleSimpleName() {
		ensureURIsmapsToShortnames( "eh:/C=C", "eh:/A=a eh:/B=b", "eh:/C" );		
	}
	
	@Test public void testIgnoresPredefinedNames() {
		ensureURIsmapsToShortnames( "", "eh:/C=whatever eh:/A=a eh:/B=b", "eh:/C" );
	}
	
	@Test public void testSeveralSimpleName() {
		ensureURIsmapsToShortnames( "eh:/C=C eh:/D=D eh:/E=E", "eh:/A=a eh:/B=b", "eh:/C eh:/D eh:/E" );		
	}
	
	@Test public void testTwoNamesLocalnameClash() {
		ensureURIsmapsToShortnames( "eh:/NS1/C=ns1_C eh:/NS2/C=ns2_C", "eh:/A=a eh:/B=b", "eh:/NS1/C eh:/NS2/C" );		
	}
	
//	@Test public void testNameUsesPrefixes() {
//		ensureURIsmapsToShortnames( "http://purl.org/dc/elements/1.1/random=dc11_random http://purl.org/vocab/changeset/schema#random=cs_random", "eh:/A=a eh:/B=b", "http://purl.org/dc/elements/1.1/random http://purl.org/vocab/changeset/schema#random" );		
//	}
//	
//	@Test public void testNameUsesTranscodingIfNoPrefixAndAmbiguous() {
//		ensureURIsmapsToShortnames( "eh:/nons/random=unknown_ehCSnonsSZrandom http://purl.org/vocab/changeset/schema#random=cs_random", "eh:/A=a eh:/B=b", "eh:/nons/random http://purl.org/vocab/changeset/schema#random" );		
//	}

	PrefixMapping pm = PrefixMapping.Factory.create()
		.setNsPrefix( "ns1", "eh:/NS1/" )
		.setNsPrefix( "ns2", "eh:/NS2/" )
		;
	
	public void ensureURIsmapsToShortnames( String expected, String given, String URIs ) {
		Map<String, String> g = MakeData.hashMap( given );
		Map<String, String> e = MakeData.hashMap( expected );
		Set<String> uris = CollectionUtils.set( URIs.split( " +" ) );
		Map<String, String> answer = complete( pm, g, uris );
		assertEquals( e, answer );
	}

	private Map<String, String> complete( PrefixMapping given_pm, Map<String, String> given, Set<String> uris ) {
		return new JSONPropertyNaming( given_pm ).complete(given, uris);
	}
}
