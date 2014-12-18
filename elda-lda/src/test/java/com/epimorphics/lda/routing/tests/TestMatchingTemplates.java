/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.routing.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.epimorphics.lda.routing.MatchSearcher;
import com.epimorphics.lda.routing.MatchTemplate;
import com.epimorphics.lda.support.MultiMap;
import com.epimorphics.lda.tests_support.MakeData;
import com.epimorphics.util.CollectionUtils;

public class TestMatchingTemplates {
	
	@Test public void testTemplateSorting() {
		MatchSearcher<String> s = new MatchSearcher<String>();
		String path_A = "/def/hospital/Hospital/instances";
		String path_B = "/def/hospital/Hospital/instances/lat/{lat}/long/{long}";
		s.register( path_A, "A" );
		s.register( path_B, "B" );
		assertEquals( CollectionUtils.list(path_B, path_A), s.templates() );
	}
	
	
	@Test public void ensure_searcher_finds_correct_value() {
		String path1 = "/abc/def", path2 = "/abc/{xyz}", path3 = "/other", path4 = "/abc/{x}{y}{z}";
		MatchSearcher<String> r = new MatchSearcher<String>();
		r.register(path3, "A" ); 
		r.register(path4, "B" );
		r.register(path2, "C" ); 
		r.register(path1, "D" ); 
		Map<String, String> b = new HashMap<String, String>();
		assertEquals("D", r.lookup(b, "/abc/def", null) );
		assertEquals("C", r.lookup(b, "/abc/27", null ) );
		assertEquals("A", r.lookup(b, "/other", null ) );
	}
	
	@Test public void ensure_matching_for_fixed_query_parameters() {
		MatchSearcher<String> r = new MatchSearcher<String>();
		Map<String, String> bindings = new HashMap<String, String>();
		MultiMap<String, String> params = new MultiMap<String, String>();
		r.register( "/anchor?k=v", "A" );
	//
		assertEquals( null, r.lookup( bindings, "/anchor", params ) );
		params.add( "k", "v" );
		assertEquals( "A", r.lookup( bindings, "/anchor", params ) );
	}
	
	
	@Test public void testing_template_with_big_query_parameter_patterns() {
		MatchSearcher<String> r = new MatchSearcher<String>();
		r.register("/eggs?arg={V}", "VAL"); 
	//		
		Map<String, String> b = new HashMap<String, String>();
		MultiMap<String, String> params = new MultiMap<String, String>();
		params.add("arg", "JHEREG");
	//		
		assertEquals("VAL", r.lookup(b, "/eggs", params) );
		assertEquals("JHEREG", b.get("V"));
	}
	
	@Test public void testing_template_with_big_multiple_parameter_patterns() {
		MatchSearcher<String> r = new MatchSearcher<String>();
		r.register("/comma?arg={X},{Y}", "VAL"); 
		
		Map<String, String> b = new HashMap<String, String>();
		MultiMap<String, String> params = new MultiMap<String, String>();
		params.add("arg", "314,271");
		
		assertEquals("VAL", r.lookup(b, "/comma", params) );
		
		assertEquals("314", b.get("X"));
		assertEquals("271", b.get("Y"));
	}
	
	@Test public void testing_template_with_big_multiple_parameter_patterns2() {
		MatchSearcher<String> r = new MatchSearcher<String>();
		r.register("/comma?arg=x:{X},y:{Y}", "VAL"); 
		
		Map<String, String> b = new HashMap<String, String>();
		MultiMap<String, String> params = new MultiMap<String, String>();
		params.add("arg", "x:314,y:271");
		
		assertEquals("VAL", r.lookup(b, "/comma", params) );
		
		assertEquals("314", b.get("X"));
		assertEquals("271", b.get("Y"));
	}
	
	@Test public void testing_template_with_big_multiple_parameter_patterns3() {
		MatchSearcher<String> r = new MatchSearcher<String>();
		r.register("/comma?arg=x:{X},y:{Y}&other={O}", "OTHER"); 
		
		Map<String, String> b = new HashMap<String, String>();
		MultiMap<String, String> params = new MultiMap<String, String>();
		params.add("arg", "x:314,y:271");
		params.add("other", "1829");
		
		assertEquals("OTHER", r.lookup(b, "/comma", params) );
		
		assertEquals("314", b.get("X"));
		assertEquals("271", b.get("Y"));
		assertEquals("1829", b.get("O"));
	}
	
	@Test public void ensure_matching_for_variable_query_parameters() {
		MatchSearcher<String> r = new MatchSearcher<String>();
		Map<String, String> bindings = new HashMap<String, String>();
		MultiMap<String, String> params = new MultiMap<String, String>();
		r.register( "/anchor?k={v}", "A" );
	//
		assertEquals( null, r.lookup( bindings, "/anchor", params ) );
		params.add( "k", "value" );
		assertEquals( "A", r.lookup( bindings, "/anchor", params ) );
		assertEquals( "value", bindings.get( "v" ) );
	}
	
	@Test public void ensure_MatchSearcher_can_remove_template() {		
		Map<String, String> b = new HashMap<String, String>();
		MatchSearcher<String> r = new MatchSearcher<String>();
		String path = "/going/away/";
		r.register( path, "GA" );
		assertEquals( "GA", r.lookup( b, path, null ) );
		r.unregister( path );
		assertEquals( null, r.lookup( b, path, null ) );
	}
	
	@Test public void ensure_matching_captures_variables() {
		String template = "/furber/any-{alpha}-{beta}/{gamma}";
		String uri = "/furber/any-99-100/boggle";
		Map<String, String> map = new HashMap<String, String>();
		Map<String, String> expected = MakeData.hashMap( "alpha=99 beta=100 gamma=boggle" );
		MatchTemplate<String> ut = MatchTemplate.prepare( template, "SPOO" );
		assertTrue( "the uri should match the pattern", ut.match(map, uri, null ) );
		assertEquals( expected, map );
	}
	
}
