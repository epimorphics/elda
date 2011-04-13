package com.epimorphics.lda.scratch.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import com.epimorphics.lda.tests_support.MakeData;
import com.epimorphics.util.Couple;

public class Scratch_URI_Templates {

	@Test public void thinking() {
		String template = "/furber/any-{alpha}-{beta}/{gamma}";
		String uri = "/furber/any-99-100/boggle";
		Map<String, String> map = new HashMap<String, String>();
		Map<String, String> expected = MakeData.hashMap( "alpha=99 beta=100 gamma=boggle" );
		UT ut = UT.prepare( template );
		assertTrue( "the uri should match the pattern", ut.match(map, uri ) );
		assertEquals( expected, map );
	}

	static class UT {
		private final Pattern compiled;
		private final List<Couple<String, Integer>> where;
		
		private UT( Pattern compiled, List<Couple<String, Integer>> where ) {
			this.where = where;
			this.compiled = compiled;
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
		
		static final Pattern p = Pattern.compile( "\\{([a-zA-Z]*)\\}" );
		
		static UT prepare( String template ) {
			Matcher m = p.matcher( template );
			int start = 0;
			int index = 0;
			List<Couple<String, Integer>> where = new ArrayList<Couple<String, Integer>>();
			StringBuilder sb = new StringBuilder();
			while (m.find(start)) {
				index += 1;
				String name = m.group(1);
				where.add( new Couple<String, Integer>( name, index ) );
				sb.append( template.substring( start, m.start() ) );
				sb.append( "([^/]+)" );
				start = m.end();
			}
			sb.append( template.substring( start ) );
			Pattern compiled = Pattern.compile( sb.toString() );
			return new UT( compiled, where );
		}
	}
	
}
