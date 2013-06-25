package com.epimorphics.lda.support.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.epimorphics.lda.support.PrefixEditor;

public class TestPrefixEditor {
	
	static final String subjectA = "http://alpha/alphaPath/end.rdf";
	static final String subjectB = "http://beta.com/path/ended.rdf";
	static final String subjectC = "http://education.data.gov.uk/doc/school";

	@Test public void testEmptyRenamerMakesNoChange() {
		PrefixEditor r = new PrefixEditor();
		assertEquals( subjectA, r.rename( subjectA ) );
		assertEquals( subjectB, r.rename( subjectB ) );
		assertEquals( subjectC, r.rename( subjectC ) );
	}
	
	@Test public void testIrrelevantRenamerMakesNoChange() {
		PrefixEditor r = new PrefixEditor().set( "http://atlantis/path", "http://localhost" );
		assertEquals( subjectA, r.rename( subjectA ) );
		assertEquals( subjectB, r.rename( subjectB ) );
		assertEquals( subjectC, r.rename( subjectC ) );
	}
	
	@Test public void testSingleMatch() {
		PrefixEditor r = new PrefixEditor().set( "http://education.uk/", "http://localhost/" );
		assertEquals( "http://localhost/pathpart", r.rename( "http://education.uk/pathpart" ) );
		assertEquals( subjectA, r.rename( subjectA ) );
		assertEquals( subjectB, r.rename( subjectB ) );
		assertEquals( subjectC, r.rename( subjectC ) );
	}
	
	@Test public void testMultipleMatches() {
		String A = "http://execute/path", B = "http://reason/road";
		PrefixEditor r = new PrefixEditor()
			.set( subjectA, A )
			.set( subjectB, B )
		;
		assertEquals( A + "ZZZ", r.rename( subjectA + "ZZZ" ) );
		assertEquals( A + "ZZZ", r.rename( subjectA + "ZZZ" ) );
	}
	
	@Test public void testOrdersLongestFirst() {
		String preA = "http://execute/path", postA = "http://shortest/"; 
		String preB = "http://execute/path/extra", postB = "http://longer";
		PrefixEditor r = new PrefixEditor().set( preA, postA ).set( preB,  postB );
		assertEquals( postB, r.rename( preB ) );
		assertEquals( postA, r.rename( preA ) );
	}

}
