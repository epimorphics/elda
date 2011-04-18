/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.core;

import java.util.*;

/**
    A map from things to lots of things. DELIBERATELY not extension
    of Map or OneToManyMap or whatever, for dependency reduction.
    
 	@author chris
*/
public class MultiMap<K, V> {
	
	protected final HashMap<K, Set<V>> underlying = new HashMap<K, Set<V>>();

	public Iterator<K> keyIterator() {
		return underlying.keySet().iterator();
	}
	
	final Set<V> NoValues = new HashSet<V>();
	
	public Set<V> getAll(K key) {
		Set<V> values = underlying.get(key);
		return values == null ? NoValues : values;
	}

	public V get(K key) {
		Set<V> values = underlying.get(key);
		return values == null ? null : values.iterator().next();
	}
	
	public void add(K key, V value) {
//		System.err.println( ">> add " + key + " => " + value );
		Set<V> values = underlying.get(key);
		if (values == null) underlying.put(key, values = new HashSet<V>() );
		values.add(value);
	}
	
	public void add(K key, Set<V> value) {
		Set<V> values = underlying.get(key);
		if (values == null) underlying.put(key, values = new HashSet<V>() );
		values.addAll(value);
	}

	public void putAll(Map<K, V> vars) {
		for (Map.Entry<K, V> e: vars.entrySet()) {
			add(e.getKey(), e.getValue());
		}
	}

	public void putAll(MultiMap<K, V> vars) {
		for (Map.Entry<K, Set<V>> e: vars.underlying.entrySet()) {
			add(e.getKey(), e.getValue());
		}
	}
	
	@Override public String toString() {
		return "<MultiMap " + underlying + ">"; 
	}
}