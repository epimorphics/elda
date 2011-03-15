/**
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.cache;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hp.hpl.jena.rdf.model.Resource;

import com.epimorphics.lda.core.APIResultSet;

/**
    The cache that remembers everything (until the world explodes
    with an OOM) -- a LimitedCache with no limits.
*/
public class PermaCache extends LimitedCacheBase implements Cache {

    public PermaCache( String label ) {
		super(label);
	}

	static Logger log = LoggerFactory.getLogger( PermaCache.class );

    @Override protected synchronized boolean exceedsSelectLimit( Map<String, List<Resource>> m ) {
		return false;
	}
    
    @Override protected synchronized boolean exceedsResultSetLimit( Map<String, APIResultSet> m ) {
		return false;
	}
    
    public synchronized static void clearAll() {
		PermaController.caches.clear();		
	}
}