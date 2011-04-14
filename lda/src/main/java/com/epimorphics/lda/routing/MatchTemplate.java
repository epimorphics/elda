/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.routing;

import java.util.*;
import java.util.regex.*;

import com.epimorphics.util.Couple;

/**
    A MatchTemplate is a compiled URI template that
    can match against a path and bind variables.
     
 	@author chris
*/
public class MatchTemplate {
	
	private final String template;
	private final Pattern compiled;
	private final List<Couple<String, Integer>> where;
	private final int literals;
	private final int slashes;
	
	private MatchTemplate( int literals, int slashes, String template, Pattern compiled, List<Couple<String, Integer>> where ) {
		this.where = where;
		this.slashes = slashes;
		this.literals = literals;
		this.compiled = compiled;
		this.template = template;
	}

	/**
	    Compare this MatchTemplate with another. The one with the most
	    literal characters is the lesser; if they have the same number
	    of literals, the one with the fewer number of path segments --
	    well, slashes actually -- is the lesser.
	*/
	public int compareTo( MatchTemplate other ) {
		int result = other.literals - literals;
		if (result == 0) result = other.slashes - slashes;
		return result;
	}
	
	/**
	    A MatchTemplate comparator for use in sorting.
	*/
	public static Comparator<MatchTemplate> compare = new Comparator<MatchTemplate>() {
		@Override public int compare( MatchTemplate a, MatchTemplate b ) {
			return a.compareTo( b );
		}
	};
	
	/**
	    Answer the URI template string from which this MatchTemplate was
	    constructed.
	*/
	public String template() {
		return template;
	}

	/**
	    Match the given uri string. If it matches, add entries to the
	    bindings map so that a template variable X maps to the corresponding
	    piece Y of the uri.
	*/
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
	
	private static final Pattern varPattern = Pattern.compile( "\\{([a-zA-Z]*)\\}" );
	
	/**
	    Answer a MatchTemplate corresponding to the template string.
	*/
	public static MatchTemplate prepare( String template ) {
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
		return new MatchTemplate( literals, slashes, template, compiled, where );
	}
}