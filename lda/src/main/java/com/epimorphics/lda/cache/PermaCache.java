/**
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.cache;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.core.*;
import com.epimorphics.lda.sources.Source;
import com.hp.hpl.jena.rdf.model.Resource;

/**
    The cache that remembers everything (until the world explodes
    with an OOM).
*/
public class PermaCache implements Cache {

    static Logger log = LoggerFactory.getLogger( PermaCache.class );
    
    @Override public APIResultSet getCachedResultSet( List<Resource> results, String view ) { 
        return cd.get( results.toString() + "::" + view );
    }
    
    @Override public List<Resource> getCachedResources( String select ) { 
        return cs.get( select );
    }
    
    @Override public void cacheDescription( List<Resource> results, String view, APIResultSet rs ) {
        log.debug( "caching descriptions for resources " + results );
        cd.put( results.toString() + "::" + view, rs );        
    }
    
    @Override public void cacheSelection( String select, List<Resource> results ) {
        log.debug( "caching resource selection for query " + select );
        cs.put( select, results );        
    }
    
    static final Map<String, PermaCache> caches = new HashMap<String, PermaCache>();
    
    private final Map<String, APIResultSet> cd = new HashMap<String, APIResultSet>();
    
    private final Map<String, List<Resource>> cs = new HashMap<String, List<Resource>>();

    static class PermaCacheMaker implements Cache.CacheMaker {

		@Override public Cache create( Source s ) {		    
			String key = s.toString();
		    PermaCache x = caches.get( key );
		    if (x == null) caches.put( key, x = new PermaCache() );
		    return x;
		}
    }
    
    static {
    	Cache.Registry.add( "default", new PermaCacheMaker() );
    	Cache.Registry.add( "perma-cache", new PermaCacheMaker() );
    }
    
	public static void clearAll() {
		caches.clear();		
	}
}