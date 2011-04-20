/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.core;

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
	public static final String _TEMPLATE = "_template";
	public static final String _VIEW = "_view";
	public static final String _PROPERTIES = "_properties";
	public static final String _SELECT_PARAM = "_select";
	public static final String _FORMAT = "_format";
	public static final String _LANG = "_lang";
	
	public static final int DEFAULT_PAGE_SIZE = 10;
	public static final int MAX_PAGE_SIZE = 250;
	
	public static final String NAME_PREFIX = "name-";
	public static final String LANG_PREFIX = "lang-";
	public static final String MIN_PREFIX = "min-";
	public static final String MAX_PREFIX = "max-";
	public static final String MIN_EX_PREFIX = "minEx-";
	public static final String MAX_EX_PREFIX = "maxEx-";
	public static final String EXISTS_PREFIX = "exists-";

}
