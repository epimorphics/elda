/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.query;

import java.util.regex.Pattern;

/**
    Transparent names for the reserved _spoo query parameters
    and their default values (if they have them) and the
    allowable parameter prefixes.
    
 	@author chris
*/
public class QueryParameter {

	public static final String _PAGE = "_page";
	public static final String _PAGE_SIZE = "_pageSize";
	public static final String _DISTANCE = "_distance";
	public static final String _SEARCH = "_search";
	public static final String _WHERE = "_where";
	public static final String _SUBJECT = "_subject";
	public static final String _SORT = "_sort";
	public static final String _ORDERBY = "_orderBy";
	public static final String _TEMPLATE = "_template";
	public static final String _VIEW = "_view";
	public static final String _PROPERTIES = "_properties";
	public static final String _SELECT_PARAM = "_select";
	public static final String _METADATA= "_metadata";
	public static final String _FORMAT = "_format";
	public static final String _LANG = "_lang";
	public static final String _COUNT = "_count";
	public static final String _GRAPH = "_graph";

	// used to force a distinction between a page and a primary topic
	public static final String _MARK = "_mark";
	public static final String callback = "callback";
	
	public static final int DEFAULT_PAGE_SIZE = 10;
	public static final int MAX_PAGE_SIZE = 250;
	
	public static final String NAME_PREFIX = "name-";
	public static final String LANG_PREFIX = "lang-";
	public static final String MIN_PREFIX = "min-";
	public static final String MAX_PREFIX = "max-";
	public static final String MIN_EX_PREFIX = "minEx-";
	public static final String MAX_EX_PREFIX = "maxEx-";
	public static final String EXISTS_PREFIX = "exists-";
	
	public static final String NEAR_LAT = "near-lat";
	public static final String NEAR_LONG = "near-long";
	
	public static final Pattern callbackPattern = Pattern.compile( "^[a-zA-Z_][a-zA-Z_0-9]*$" );

	/**
	    Answer true iff <code>p</code> is one of the reserved parameters:
	    starts with "_", is "near-lat" or "near-long", or is "callback".
	*/
	public static boolean isReserved(String p) {
		return 
			p.startsWith("_") 
			|| p.equals("near-lat") || p.equals("near-long") 
			|| p.equals( callback )
			;
	}
}
