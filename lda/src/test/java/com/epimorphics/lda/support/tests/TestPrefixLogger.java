package com.epimorphics.lda.support.tests;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import com.epimorphics.lda.support.PrefixLogger;
import com.hp.hpl.jena.test.JenaTestBase;

public class TestPrefixLogger {

	Pattern qnp = PrefixLogger.candidatePrefix;
	
	@Test public void testMe() {
		assertEquals( JenaTestBase.setOfStrings( "" ), findPrefixes( "" ) );
	}
	
	@Test public void testB() {
		assertEquals( JenaTestBase.setOfStrings( "" ), findPrefixes( "none here" ) );
	}
	
	@Test public void testC() {
		assertEquals( JenaTestBase.setOfStrings( "" ), findPrefixes( "17 select" ) );
	}
	
	@Test public void testD() {
		assertEquals( JenaTestBase.setOfStrings( "http" ), findPrefixes( "<http://example.com>" ) );
	}
	
	@Test public void testE() {
		assertEquals( JenaTestBase.setOfStrings( "eh file" ), findPrefixes( "<eh:/whatever> <file:///my.file>" ) );
	}
	
	@Test public void testF() {
		assertEquals( JenaTestBase.setOfStrings( "rdf" ), findPrefixes( "rdf:type" ) );
		assertEquals( JenaTestBase.setOfStrings( "rdf" ), findPrefixes( "rdf:" ) );
		assertEquals( JenaTestBase.setOfStrings( "rdf" ), findPrefixes( "rdf:type" ) );
	}
	
	@Test public void testG() {
		assertEquals( JenaTestBase.setOfStrings( "bwq-iss qb" ), findPrefixes( "bwq-iss:latest qb:observation ?item " ) );
	}
	
	@Test public void testH() {
		assertEquals( JenaTestBase.setOfStrings( "" ), findPrefixes( "" ) );
	}
	
	@Test public void testI() {
	}

	private Set<String> findPrefixes( String s ) {
		Set<String> result = new HashSet<String>();
		Matcher m = qnp.matcher( s );
		while (m.find()) result.add( m.group(1) );
		return result;
	}

}
