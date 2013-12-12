/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.routing;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.epimorphics.lda.support.MultiMap;
import com.epimorphics.util.Couple;

/**
    A MatchTemplate is a compiled URI template that
    can match against a path and bind variables.
     
 	@author chris
*/
public class MatchTemplate<T> {
	
	private final String template;
	private final Pattern compiled;
	private final List<Couple<String, Integer>> where;
	private final int literals;
	private final int patterns;
	private final T value;
	private final Map<String, String> params;
	
	private MatchTemplate
		( int literals
		, int patterns
		, String template
		, Pattern compiled
		, Map<String, String> params
		, List<Couple<String, Integer>> where
		, T value 
		) {
		this.where = where;
		this.value = value;
		this.patterns = patterns + patternCount(params);
		this.literals = literals;
		this.compiled = compiled;
		this.template = template;
		this.params = params;
	}

	private int patternCount(Map<String, String> params ) {
		int result = 0;
		for (Map.Entry<String, String> e: params.entrySet())
			if (e.getValue().startsWith("{")) result += 1;
		return result;
	}
	
	@Override public String toString() {
		return "<MatchTemplate for '" + template + "' => " + value + ">";
	}

	/**
	    Compare this MatchTemplate with another. The one with the most
	    literal characters is the lesser; if they have the same number
	    of literals, the one with the fewer patterns is the lesser.
	    (Hence a sort will put "more specific" templates earlier.)
	*/
	public int compareTo( MatchTemplate<?> other ) {
		int result = other.literals - literals;
		if (result == 0) result = other.patterns - patterns;
		return result;
	}
	
	/**
	    A MatchTemplate comparator for use in sorting.
	*/
	public static Comparator<MatchTemplate<?>> compare = new Comparator<MatchTemplate<?>>() {
		@Override public int compare( MatchTemplate<?> a, MatchTemplate<?> b ) {
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
	    Answer the associated value for this template.
	*/
	public T value() {
		return value;
	}

	/**
	    Match the given uri string. If it matches, add entries to the
	    bindings map so that a template variable X maps to the corresponding
	    piece Y of the uri.
	 * @param queryParams 
	*/
	public boolean match( Map<String, String> bindings, String uri, MultiMap<String, String> queryParams ) {
		Matcher mu = compiled.matcher( uri );
		if (mu.matches()) {
			if (paramsMatch( bindings, queryParams )) {
				for (Couple<String, Integer> c: where) {
					bindings.put(c.a, mu.group(c.b) );
				}
				return true;
			}
		}
		return false;
	}
	
	private boolean paramsMatch( Map<String, String> bindings, MultiMap<String, String> queryParams ) {
		Map<String, String> perhaps = new HashMap<String, String>();
		List<String> toRemove = new ArrayList<String>();
		for (String key: params.keySet()) {
			if (queryParams.containsKey( key )) {
				toRemove.add( key );
				String v = queryParams.getOne( key );
				String p = params.get( key );
				if (p.startsWith( "{" )) {
					String varName = p.substring(1, p.length() - 1);
					perhaps.put(varName, v);
				} else if (!p.equals(v)) {
					return false;
				}
			} else {
				return false;
			}
		}
		for (String key: toRemove) queryParams.remove( key );
		bindings.putAll( perhaps );
		return true;
	}

	private static final Pattern varPattern = Pattern.compile( "\\{([a-zA-Z][a-zA-Z0-9_]*)\\}" );
	
	/**
	    Answer a MatchTemplate corresponding to the template string.
	*/
	public static <T> MatchTemplate<T> prepare( String template, T value ) {
		int q = template.indexOf( '?' );
		Map<String, String> params = new HashMap<String, String>();
		if (q > -1) {
			fillParams( params, template.substring(q + 1) );
			template = template.substring(0, q);
		}
		Matcher m = varPattern.matcher( template );
		int start = 0;
		int index = 0;
		int literals = 0;
		int patterns = 0;
		List<Couple<String, Integer>> where = new ArrayList<Couple<String, Integer>>();
		StringBuilder sb = new StringBuilder();
		while (m.find(start)) {
			index += 1;
			String name = m.group(1);
			where.add( new Couple<String, Integer>( name, index ) );
			String literal = template.substring( start, m.start() );
			literals += literal.length();
			patterns += 1;
			sb.append( literal );
			sb.append( "([^/]+)" );
			start = m.end();
		}
		String literal = template.substring( start );
		sb.append( literal );
		literals += literal.length();
		Pattern compiled = Pattern.compile( sb.toString() );
		return new MatchTemplate<T>( literals, patterns, template, compiled, params, where, value );
	}

	private static void fillParams( Map<String, String> params, String template ) {
		String [] fields = template.split( "&" );
		for (String f: fields) {
			String [] kv = f.split( "=", 2 );
			params.put( kv[0], kv[1] );
		}
	}
}