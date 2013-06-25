package com.epimorphics.lda.routing.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.epimorphics.lda.routing.ServletUtils;

public class TestServletUtils {

	String [][] expectations = new String[][] {
		// expect, wildPrefix, specPath, name
		{ "example", "*", "/ignored/*.ttl", "example.ttl" } 
		, { "exampleafter", "*after", "/ignored/*.ttl", "example.ttl" } 
		, { "beforeexample", "before*", "/ignored/*.ttl", "example.ttl" } 
		, { "beforeexampleafter", "before*after", "*.ttl", "example.ttl" } 
		, { "Aexam-pleB", "A*B", "*-*.ttl", "exam-ple.ttl" } 
	};
	
	@Test public void testNameToPrefix() {
		for (String [] testCase: expectations) {
			String expect = testCase[0];
			String wildPrefix = testCase[1];
			String specPath = testCase[2];
			String name = testCase[3];
			String result = ServletUtils.nameToPrefix(wildPrefix, specPath, name );
			assertEquals( expect, result );
		}
	}
}
