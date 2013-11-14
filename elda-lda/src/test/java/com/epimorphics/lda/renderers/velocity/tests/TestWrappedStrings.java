package com.epimorphics.lda.renderers.velocity.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.epimorphics.lda.renderers.velocity.WrappedString;

public class TestWrappedStrings {
	
	@Test public void ensureRawAndCookedValuesAvailable() {
		ensureRawAndCookedValuesAvailable( "xyz123", "xyz123" );
		ensureRawAndCookedValuesAvailable( "xyz&123", "xyz&amp;123" );
		ensureRawAndCookedValuesAvailable( "xyz<123", "xyz&lt;123" );
		ensureRawAndCookedValuesAvailable( "<xyz123&", "&lt;xyz123&amp;" );
	}
	
	@Test public void ensurePerformsSpaceBreaks() {
		ensureInsertsSpaces( "123gho", "123gho" );
		ensureInsertsSpaces( "123 gho", "123_gho" );
		ensureInsertsSpaces( "123 gho ab", "123_gho_ab" );
		ensureInsertsSpaces( "is camel case", "isCamelCase" );
		ensureInsertsSpaces( "is CAMEL case", "isCAMELcase" );
	}
	
	private void ensureInsertsSpaces(String expected, String toCut) {
		assertEquals( expected, new WrappedString(toCut).cut().raw() );
	}

	void ensureRawAndCookedValuesAvailable( String content, String cooked ) {
		WrappedString ws = new WrappedString( content );
		assertEquals( content, ws.raw() );
		assertEquals( cooked, ws.toString() );
	}
	
	@Test public void ensureCutRespectsBoundaries() {
		ensureCutRespectsBoundaries( "babble", "babble" );
		ensureCutRespectsBoundaries( "red hen", "redHen" );
		ensureCutRespectsBoundaries( "red riding hood", "redRidingHood" );
		ensureCutRespectsBoundaries( "designed by", "designedBy" );
		ensureCutRespectsBoundaries( "empty faces", "empty_faces" );
		ensureCutRespectsBoundaries( "empty faces", "empty__faces" );
		ensureCutRespectsBoundaries( "empty faces", "empty___faces" );
		ensureCutRespectsBoundaries( "some URI example", "someURIexample" );
	}

	private void ensureCutRespectsBoundaries(String expected, String original) {
		String derived = new WrappedString( original ).cut().raw();
		assertEquals(expected, derived);
	}
	
	
}
