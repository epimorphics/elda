/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.routing.tests;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.epimorphics.lda.routing.MatchSearcher;
import com.epimorphics.lda.routing.MatchTemplate;
import com.epimorphics.lda.tests_support.MakeData;

public class TestMatchingTemplates {
	
	@Test public void ensure_searcher_finds_correct_value() {
		String path1 = "/abc/def", path2 = "/abc/{xyz}", path3 = "/other", path4 = "/abc/{x}{y}{z}";
		MatchSearcher<String> r = new MatchSearcher<String>();
		r.add(path3, "A" ); 
		r.add(path4, "B" );
		r.add(path2, "C" ); 
		r.add(path1, "D" ); 
		Map<String, String> b = new HashMap<String, String>();
		assertEquals("D", r.lookup(b, "/abc/def") );
		assertEquals("C", r.lookup(b, "/abc/27" ) );
		assertEquals("A", r.lookup(b, "/other" ) );
	}
	
	@Test public void ensure_MatchSearcher_can_remove_template() {		
		Map<String, String> b = new HashMap<String, String>();
		MatchSearcher<String> r = new MatchSearcher<String>();
		String path = "/going/away/";
		r.add( path, "GA" );
		assertEquals( "GA", r.lookup( b, path ) );
		r.remove( path );
		assertEquals( null, r.lookup( b, path ) );
	}
	
	@Test public void ensure_matching_captures_variables() {
		String template = "/furber/any-{alpha}-{beta}/{gamma}";
		String uri = "/furber/any-99-100/boggle";
		Map<String, String> map = new HashMap<String, String>();
		Map<String, String> expected = MakeData.hashMap( "alpha=99 beta=100 gamma=boggle" );
		MatchTemplate<String> ut = MatchTemplate.prepare( template, "SPOO" );
		assertTrue( "the uri should match the pattern", ut.match(map, uri ) );
		assertEquals( expected, map );
	}
	
}
