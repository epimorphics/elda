/**
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.

    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.core.APIResultSet;
import com.hp.hpl.jena.rdf.model.Resource;

public abstract class LimitedCacheBase implements Cache {

    private static Logger log = LoggerFactory.getLogger( LimitedCacheBase.class );

    protected final String label;
    protected final int identity;

    public LimitedCacheBase( String label ) {
        this.label = label;
        this.identity = Cache.Registry.newIdentity();
    }
    
    @Override public synchronized String summary() {
    	return "#" + identity + "." + label + " (" + cd.size() + ", " + cs.size() + " entries)";
    }
    
    @Override public synchronized void show( StringBuilder sb ) {
    	sb.append( summary() );
    	for (Map.Entry<String, List<Resource>> e: cs.entrySet()) {
    		sb.append( "<pre>" );
    		sb.append( e.getKey().replaceAll( "\n", " " ).replaceAll( "&", "&amp;" ).replaceAll( "<", "&lt;" ) );
    		sb.append( "</pre>\n" );
    	}
    }

    protected abstract boolean exceedsSelectLimit( Map<String, List<Resource>> m) ;

    protected abstract boolean exceedsResultSetLimit( Map<String, APIResultSet> m );

    private final Map<String, APIResultSet> cd = new HashMap<String, APIResultSet>();

    private final Map<String, List<Resource>> cs = new HashMap<String, List<Resource>>();

    @Override public synchronized APIResultSet getCachedResultSet( List<Resource> results, String view ) {
        return cd.get( results.toString() + "::" + view );
    }

    @Override public synchronized List<Resource> getCachedResources( String select ) {
        return cs.get( select );
    }

    @Override public synchronized void cacheDescription( List<Resource> results, String view, APIResultSet rs ) {
        log.debug( "caching descriptions for resources " + results );
        cd.put( results.toString() + "::" + view, rs );
        if (exceedsResultSetLimit( cd )) {
            log.info( "clearing description cache for " + label );
//        	System.err.println( "clearing description cache for " + label );
            cd.clear();
        }
    }

    @Override public synchronized void cacheSelection( String select, List<Resource> results ) {
        log.debug( "caching resource selection for query " + select );
        cs.put( select, results );
        if (exceedsSelectLimit( cs )) {
            log.info( "clearing select cache for " + label );
            cs.clear();
        }
    }

    @Override public synchronized void clear() {
        cs.clear();
        cd.clear();
    }

    @Override public synchronized int numEntries() {
        return cd.size() + cs.size();
    }
}
