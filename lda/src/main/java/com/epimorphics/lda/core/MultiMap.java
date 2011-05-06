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
	
	protected final HashMap<K, List<V>> underlying = new HashMap<K, List<V>>();

	public Iterator<K> keyIterator() {
		return underlying.keySet().iterator();
	}
	
	final List<V> NoValues = new ArrayList<V>();
	
	public List<V> getAll(K key) {
		List<V> values = underlying.get(key);
		return values == null ? NoValues : values;
	}

	public Set<K> keySet() {
		return underlying.keySet();
	}
	
	public V getOne(K key) {
		List<V> values = underlying.get(key);
		return values == null ? null : values.get(0);
	}
	
	public void add(K key, V value) {
//		System.err.println( ">> add " + key + " => " + value );
		List<V> values = underlying.get(key);
		if (values == null) underlying.put(key, values = new ArrayList<V>() );
		values.add(value);
	}
	
	public void add(K key, Set<V> value) {
		List<V> values = underlying.get(key);
		if (values == null) underlying.put(key, values = new ArrayList<V>() );
		values.addAll(value);
	}

	public void putAll(Map<K, V> vars) {
		for (Map.Entry<K, V> e: vars.entrySet()) {
			add(e.getKey(), e.getValue());
		}
	}

	public void putAll(MultiMap<K, V> vars) {
		for (Map.Entry<K, List<V>> e: vars.underlying.entrySet()) {
			for (V v: e.getValue()) add( e.getKey(), v );
		}
	}
	
	@Override public String toString() {
		return "<MultiMap " + underlying + ">"; 
	}
}