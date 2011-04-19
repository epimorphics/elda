/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.scratch.tests;

import static org.junit.Assert.*;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.epimorphics.lda.routing.MatchTemplate;
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
//		t.printOn( System.out );
	//
		assertEquals( "A", t.lookup( path1 ) );
		assertEquals( "B", t.lookup( path2 ) );
		assertEquals( "C", t.lookup( path3 ) );
	}
	
	@Test public void search_res() {
		ReBox<String> rb = new ReBox<String>();
		rb.add( "/foo/bar/baz", "A" );
		assertEquals( "A", rb.match( "/foo/bar/baz" ) );
		assertEquals( null, rb.match( "/foo/bar/ba" ) );
		assertEquals( null, rb.match( "/foo/bar/bazz" ) );
	}
	
	@Test public void search_res2() {
		ReBox<String> rb = new ReBox<String>();
		rb.add( "/foo/bar/baz", "A" );
		rb.add( "/foo/bar", "B" );
		rb.add( "/foo/bar/baz/bert", "C" );
		assertEquals( "A", rb.match( "/foo/bar/baz" ) );
		assertEquals( "B", rb.match( "/foo/bar" ) );
		assertEquals( "C", rb.match( "/foo/bar/baz/bert" ) );
		assertEquals( null, rb.match("/foo/bar/b") );
		assertEquals( null, rb.match("/foo/bar/baz/berti") );
	}
	
	@Test public void search_res3() {
		ReBox<String> rb = new ReBox<String>();
		rb.add( "/foo/bar/baz", "A" );
		assertEquals( "A", rb.match( "/foo/bar/baz" ) );
		assertEquals( null, rb.match( "/foo/bar/ba" ) );
		assertEquals( null, rb.match( "/foo/bar/bazz" ) );
	}
	
	static class ReBox<T> {

		Map<String, T> templates = new HashMap<String, T>();
		
		public void add(String template, T value ) {
			templates.put(template, value);
			}
		
		public T match( String s ) {
			Machine<T> m = compile( templates );
			return m.match(s);
		}
		
		Machine<T> compile( Map<String, T> templates ) {
			Machine<T> m = new Machine<T>();
			for (Map.Entry<String, T> e: templates.entrySet()) {
				String t = e.getKey();
				int state = 0;
				for (int i = 0; i < t.length(); i += 1) {
					char ch = t.charAt(i);
					if (ch == '{') {
						throw new RuntimeException("NOT YET" );
					} else {
						int next = m.freshState();
						m.go(state, ch, next);
						state = next;
					}
				}
				m.isfinal(state, e.getValue());
			}
			return m;
		}
		
	}
	
	static class Machine<T> {
		
		int counter = 0;
		
		Map<Integer, Set<Integer>> states = new HashMap<Integer, Set<Integer>>();
		Map<Integer, T> answers = new HashMap<Integer, T>();
		
		public int freshState() {
			return ++counter;
		}

		public void go(int state, char ch, int next) {
			int key = (state << 16) + ch;
			Set<Integer> to = states.get(key);
			if (to == null) states.put(key, to = new HashSet<Integer>());
			to.add(next);
		}
		
		public void isfinal(int state, T value) {
			answers.put(state, value);
		}

		private void showme() {
			System.out.println( "Some State Machine" );
			List<Map.Entry<Integer, Set<Integer>>> x = new ArrayList<Map.Entry<Integer,Set<Integer>>>(states.size());
			for (Map.Entry<Integer, Set<Integer>> e: states.entrySet())	x.add( e );
			Collections.sort( x, new Comparator<Map.Entry<Integer, Set<Integer>>>() {
				@Override public int compare(Map.Entry<Integer, Set<Integer>> o1, Map.Entry<Integer, Set<Integer>> o2) {
					return o1.getKey() - o2.getKey();
				}})
				;
			for (Map.Entry<Integer, Set<Integer>> e: x) {
				int sc = e.getKey();
				System.out.println( " S" + (sc >> 16) + " " + (char)(sc & 0xff) + " => S*" + e.getValue() );
			}
			System.out.println( answers );
		}
		
		public T match(String s) {
//			showme();
			Set<Integer> current = new HashSet<Integer>(); 
			current.add(0);
			Set<Integer> nexties = new HashSet<Integer>();
		//
			for (int i = 0; i < s.length(); i += 1) {
				char ch = s.charAt(i);
//				System.err.println( ">> doing: " + ch + ", current = " + current );
				for (Integer state: current) {
					Set<Integer> next = states.get( (state << 16) + ch );
					if (next != null) nexties.addAll( next );
				}
//				System.err.println( ">> nexties: " + nexties );
				// if (nexties.isEmpty()) { System.err.println( ">> BOO!" ); return null; }
				// Set<Integer> x = nexties; nexties = current; current = x; nexties.clear();
				current = nexties; nexties = new HashSet<Integer>();
			}
		//
			for (Integer state: current) {
				T answer = answers.get(state);
				if (answer != null) return answer;
			}
			return null;
//			Iterator<Integer> it = current.iterator();
//			return it.hasNext() ? answers.get( it.next() ) : null;
		}
		
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
	
	
}
