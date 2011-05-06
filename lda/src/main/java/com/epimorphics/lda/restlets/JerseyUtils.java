/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.restlets;

import javax.ws.rs.core.MultivaluedMap;

import com.epimorphics.lda.core.MultiMap;

public class JerseyUtils {

	/**
	    Convert a (rs.core) MultivaluedMap to (our, local) MultiMap.
	    This is just to allow bridging from Jersey restlets into our
	    (eventually, Jersey-free internally) code.
	*/
	public static <K, V> MultiMap<K, V> convert( MultivaluedMap<K, V> map ) {
		MultiMap<K, V> result = new MultiMap<K, V>();
		for (K key: map.keySet()) {
			for (V value: map.get(key)) result.add(key, value);        			
		}
		return result;
	}

}
