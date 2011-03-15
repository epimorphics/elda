/**
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.cache;

import java.util.HashMap;
import java.util.Map;

import com.epimorphics.lda.sources.Source;

/**
    A Cache.Controller base class that is passed an actual
    cache factory on construction.
*/
public class ControllerBase implements Cache.Controller {

	protected final CacheNewer factory;
	
	protected ControllerBase( CacheNewer m ) {
		this.factory = m;
	}
	
	static final Map<String, Cache> caches = new HashMap<String, Cache>();

	@Override public synchronized void clear( Source s ) {
		String key = s.toString();
		Cache c = caches.get( key );
		if (c != null) c.clear();
		caches.remove( key );
	}

	@Override public synchronized void clearAll() {
		for (Map.Entry<String, ? extends Cache> e: caches.entrySet()) e.getValue().clear();
		caches.clear();
	}
	
	@Override public synchronized Cache cacheFor( Source s, String policyValue ) {		    
		String key = s.toString();
	    Cache x = caches.get( key );
	    if (x == null) caches.put( key, x = factory.New( key, policyValue ) );
	    return x;
	
	}
}