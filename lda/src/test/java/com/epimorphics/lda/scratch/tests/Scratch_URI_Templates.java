package com.epimorphics.lda.scratch.tests;

import static org.junit.Assert.*;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import com.epimorphics.lda.routing.MatchTemplate;
import com.epimorphics.lda.tests_support.MakeData;
import com.epimorphics.lda.tests_support.NotImplementedException;

public class Scratch_URI_Templates {

	@Test public void probe() {
		String path = "/abd/def";
		assertTrue( MatchTemplate.prepare(path, "value").match(null, path) );
	}
	
	@Test public void search_thinking() {
		String path1 = "/foo/bar/baz", path2 = "/foo/bill/ben", path3 = "/other/thing";
		Table t = new Table();
		t.add(path1, "A" );
		t.add(path2, "B" );
		t.add(path3, "C" );
	//
		t.printOn( System.out );
	//
		assertEquals( "A", t.lookup( path1 ) );
		assertEquals( "B", t.lookup( path2 ) );
		assertEquals( "C", t.lookup( path3 ) );
	}
	
	static class Table {

		State initial = new State();
		
		public void add( String path, String result ) {
			initial.add( Arrays.asList( path.split("/") ), result );
		}
		
		public void printOn( PrintStream out ) {
			initial.printOn( out );
			out.println();
		}
		
		static class State {
			
			final Map<String, State> followers = new HashMap<String, State>();
			String result = null;
			
			public void printOn( PrintStream out ) {
				out.print( "(" );
				String pre = "";
				for (Map.Entry<String, State> e: followers.entrySet()) {
					out.print( pre ); pre = "; ";
					out.print( e.getKey() );
					out.print( " => " );
					e.getValue().printOn( out );
				}
				if (result == null) {
					out.print( " [...] " );
				} else {
					out.print( " | " );
					out.print( result );
				}
				out.print( ")" );
			}
			
			public boolean hasPattern() {
				return false;
			}
			
			public void add( List<String> segments, String result ) {
				if (segments.isEmpty()) {
					if (this.result == null) this.result = result;
					else throw new RuntimeException( "already have result: " + this.result + ", now given " + result );
				} else {
					String seg = segments.get(0);
					if (!followers.containsKey(seg)) followers.put(seg, new State() );
					followers.get(seg).add( segments.subList(1, segments.size() ), result );
				}
			}

			public boolean hasSegment(String s) {
				return followers.containsKey( s );
			}
			
			public State next( String s ) {
				return followers.get(s);
			}
			
			public boolean completed() {
				return result != null;
			}
			
			public String result() {
				return result;
			}
		}
		
		public String lookup( String path ) {
			State s = initial;
			String [] segments = path.split( "/" );
			for (String segment: segments) {
				if (s.hasPattern()) {
					throw new NotImplementedException();
				} else {
					if (s.hasSegment(segment)) {
						s = s.next(segment);
					} else {
						return null;
					}
				}
			}
			if (s.completed()) return s.result();
			return null;
		}
		
	}
	
	@Test public void path_thinking() {
		String path1 = "/abc/def", path2 = "/abc/{xyz}", path3 = "/other", path4 = "/abc/{x}{y}{z}";
		MatchSearcher<String> r = new MatchSearcher<String>();
		r.add(path3, "A" ); 
		r.add(path4, "B" );
		r.add(path2, "C" ); 
		r.add(path1, "D" ); 
		assertEquals("D", r.lookup("/abc/def") );
		assertEquals("C", r.lookup("/abc/27" ) );
		assertEquals("A", r.lookup("/other" ) );
	}
	
	public static class MatchSearcher<T> {
		
		List<MatchTemplate<T>> templates = new ArrayList<MatchTemplate<T>>();
		
		public void add( String path, T result ) {
			templates.add( MatchTemplate.prepare( path, result ) );
		}
		
		public T lookup(String path) {
			Collections.sort( templates, MatchTemplate.compare );			
			Map<String, String> bindings = new HashMap<String, String>();
			for (MatchTemplate<T> t: templates) {
				if (t.match(bindings, path)) return t.value();
			}
			return null;
		}
	}
	
	@Test public void pattern_thinking() {
		String template = "/furber/any-{alpha}-{beta}/{gamma}";
		String uri = "/furber/any-99-100/boggle";
		Map<String, String> map = new HashMap<String, String>();
		Map<String, String> expected = MakeData.hashMap( "alpha=99 beta=100 gamma=boggle" );
		MatchTemplate<String> ut = MatchTemplate.prepare( template, "SPOO" );
		assertTrue( "the uri should match the pattern", ut.match(map, uri ) );
		assertEquals( expected, map );
	}
	
}
