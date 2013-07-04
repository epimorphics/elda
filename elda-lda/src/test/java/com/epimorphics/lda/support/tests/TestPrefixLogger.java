package com.epimorphics.lda.support.tests;

import static com.epimorphics.util.CollectionUtils.set;
import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import com.epimorphics.lda.support.PrefixLogger;
import com.epimorphics.util.CollectionUtils;

public class TestPrefixLogger {

	Pattern qnp = PrefixLogger.candidatePrefix;
	
	@Test public void testMe() {
		assertEquals( set(), findPrefixes( "" ) );
	}
	
	@Test public void testB() {
		assertEquals( set(), findPrefixes( "none here" ) );
	}
	
	@Test public void testC() {
		assertEquals( set(), findPrefixes( "17 select" ) );
	}
	
	@Test public void testD() {
		assertEquals( set( "http" ), findPrefixes( "<http://example.com>" ) );
	}
	
	@Test public void testE() {
		assertEquals( CollectionUtils.set( "eh", "file" ), findPrefixes( "<eh:/whatever> <file:///my.file>" ) );
	}
	
	@Test public void testF() {
		assertEquals( set( "rdf" ), findPrefixes( "rdf:type" ) );
		assertEquals( set( "rdf" ), findPrefixes( "rdf:rdf:" ) );
		assertEquals( set( "rdf" ), findPrefixes( "rdf:type" ) );
	}
	
	@Test public void testG() {
		assertEquals( set( "bwq-iss", "qb" ), findPrefixes( "bwq-iss:latest qb:observation ?item " ) );
	}
	
	@Test public void testH() {
		assertEquals( set( "my-prefix" ), findPrefixes( "my-prefix:nothing" ) );
		// assertEquals( set( "my+prefix" ), findPrefixes( "my+prefix:nothing" ) );
		assertEquals( set( "my-17prefix" ), findPrefixes( "my-17prefix:nothing" ) );
		assertEquals( set( "my.prefix" ), findPrefixes( "my.prefix:nothing" ) );
	}
	
	@Test public void testI() {
		assertEquals( set( "A" ), findPrefixes( "[A:]" ) );
		assertEquals( set( "a-b" ), findPrefixes( "<a-b:>" ) );
		assertEquals( set( "a.b.c" ), findPrefixes( "(a.b.c:::]" ) );
		// assertEquals( set( "a+.b" ), findPrefixes( ">[a+.b:a+.c;" ) );
	}
	
	@Test public void testJ() {
		assertEquals( set( "A" ), findPrefixes( "[A:]" ) );
		assertEquals( set( "a_b" ), findPrefixes( "<a_b:>" ) );
		assertEquals( set( "a_b_c" ), findPrefixes( "(a_b_c:::]" ) );
		assertEquals( set( "a" ), findPrefixes( ">[_a:" ) );
	}
	
	private Set<String> findPrefixes( String s ) {
		Set<String> result = new HashSet<String>();
		Matcher m = qnp.matcher( s );
		while (m.find()) result.add( m.group(1) );
		return result;
	}

}
