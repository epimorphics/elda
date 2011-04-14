package com.epimorphics.lda.scratch.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Ignore;
import org.junit.Test;

import com.epimorphics.lda.tests_support.MakeData;
import com.epimorphics.util.Couple;

public class Scratch_URI_Templates {

	@Test public void probe() {
		String path = "/abd/def";
		assertTrue( UT.prepare(path).match(null, path) );
	}
	
	@Test @Ignore public void path_thinking() {
		String path1 = "/abc/def", path2 = "/abc/{xyz}", path3 = "/other", path4 = "/abc/{x}{y}{z}";
		Router r = new Router();
		r.add(path3); 
		r.add(path4);
		r.add(path2); 
		r.add(path1); 
		assertEquals(path1, r.lookup("/abc/def") );
		assertEquals(path2, r.lookup("/abc/27" ) );
		assertEquals(path3, r.lookup("/other" ) );
	}
	
	static class Router {

		List<String> paths = new ArrayList<String>();
		
		public void add(String path) {
			paths.add(path);
		}
		
		public String lookup(String path) {
			List<UT> uts = new ArrayList<UT>(paths.size());
			for (String p: paths) uts.add( UT.prepare( p ) );
		//
			System.err.println( ">> before sorting: " );
			for (UT u: uts) System.err.println( ">>  " + u.template() );
		//
			Collections.sort( uts, UT.compare );			
		//
			System.err.println( ">> after sorting: " );
			for (UT u: uts) System.err.println( ">>  " + u.template() );
		//
			Map<String, String> bindings = new HashMap<String, String>();
			for (UT u: uts) {
				if (u.match(bindings, path)) {
					return u.template();
				}
			}
			return null;
		}
	}
	
	@Test public void pattern_thinking() {
		String template = "/furber/any-{alpha}-{beta}/{gamma}";
		String uri = "/furber/any-99-100/boggle";
		Map<String, String> map = new HashMap<String, String>();
		Map<String, String> expected = MakeData.hashMap( "alpha=99 beta=100 gamma=boggle" );
		UT ut = UT.prepare( template );
		assertTrue( "the uri should match the pattern", ut.match(map, uri ) );
		assertEquals( expected, map );
	}

	static class UT {
		
		private final String template;
		private final Pattern compiled;
		private final List<Couple<String, Integer>> where;
		private final int literals;
		private final int slashes;
		
		private UT( int literals, int slashes, String template, Pattern compiled, List<Couple<String, Integer>> where ) {
			this.where = where;
			this.slashes = slashes;
			this.literals = literals;
			this.compiled = compiled;
			this.template = template;
		}
	
		public int compareTo( UT other ) {
			int result = literals - other.literals;
			if (result == 0) result = slashes - other.slashes;
			return result;
		}
		
		public static Comparator<UT> compare = new Comparator<UT>() {

			@Override public int compare( UT a, UT b ) {
				return a.compareTo( b );
			}
		};
		
		public String template() {
			return template;
		}

		public boolean match( Map<String, String> bindings, String uri ) {
			Matcher mu = compiled.matcher( uri );
			if (mu.matches()) {
				for (Couple<String, Integer> c: where) {
					bindings.put(c.a, mu.group(c.b) );
				}
				return true;
			} else {
				return false;
			}
		}
		
		static final Pattern varPattern = Pattern.compile( "\\{([a-zA-Z]*)\\}" );
		
		static UT prepare( String template ) {
			Matcher m = varPattern.matcher( template );
			int start = 0;
			int index = 0;
			int literals = 0;
			int slashes = 0;
			List<Couple<String, Integer>> where = new ArrayList<Couple<String, Integer>>();
			StringBuilder sb = new StringBuilder();
			while (m.find(start)) {
				index += 1;
				String name = m.group(1);
				where.add( new Couple<String, Integer>( name, index ) );
				String literal = template.substring( start, m.start() );
				literals += literal.length();
				sb.append( literal );
				sb.append( "([^/]+)" );
				start = m.end();
			}
			String literal = template.substring( start );
			sb.append( literal );
			literals += literal.length();
			Pattern compiled = Pattern.compile( sb.toString() );
			return new UT( literals, slashes, template, compiled, where );
		}
	}
	
}
