/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.support;

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
	
	public boolean containsKey( K key ) {
		return underlying.containsKey( key );
	}
	
	public Set<V> getAll(K key) {
		Set<V> values = underlying.get(key);
		return values == null ? NoValues : values;
	}

	public Set<K> keySet() {
		return underlying.keySet();
	}
	
	public V getOne(K key) {
		Set<V> values = underlying.get(key);
		return values == null ? null : values.iterator().next();
	}
	
	public void add(K key, V value) {
//		System.err.println( ">> add " + key + " => " + value );
		Set<V> values = underlying.get(key);
		if (values == null) underlying.put(key, values = new HashSet<V>() );
		values.add(value);
	}
	
	/**
	   Add all the bindings from <code>key</code> to the elements of 
	   <code>values</code> to this MultiMap.
	*/
	public void add(K key, Set<V> value) {
		Set<V> values = underlying.get(key);
		if (values == null) underlying.put(key, values = new HashSet<V>() );
		values.addAll(value);
	}

	/**
	    Add all the entries from <code>map</code> to this
	    MultiMap.
	*/
	public void putAll(Map<K, V> map) {
		for (Map.Entry<K, V> e: map.entrySet()) {
			add( e.getKey(), e.getValue() );
		}
	}

	/**
	    Add all the entries from the MultiMap <code>map</code>
	    to this MultiMap.
	*/
	public void putAll(MultiMap<K, V> map) {
		for (Map.Entry<K, Set<V>> e: map.underlying.entrySet()) {
			for (V v: e.getValue()) add( e.getKey(), v );
		}
	}
	
	@Override public String toString() {
		return "<MultiMap " + underlying + ">"; 
	}

	public void addAll( MultiMap<K, V> map ) {
		for (Map.Entry<K, Set<V>> e: map.underlying.entrySet()) {
			add( e.getKey(), e.getValue() );
		}
	}

	public void remove(String key) {
		underlying.remove( key );
	}
}