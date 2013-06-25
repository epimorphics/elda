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
    	return "#" + identity + "." + label + " (select " + cs.size() + ", view " + cd.size() + " entries)";
    }
    
    @Override public synchronized void show( StringBuilder sb ) {
    	long now = System.currentTimeMillis();
    	float selectSeconds = cs.baseTime == 0 ? 0 : (now - cs.baseTime) / 1000.0f;
    	float viewSeconds = cd.baseTime == 0 ? 0 : (now - cd.baseTime) / 1000.0f;
    	float selectDropSeconds = cs.dropTime == 0 ? 0 : (now - cs.dropTime) / 1000.0f; 
    	float viewDropSeconds = cd.dropTime == 0 ? 0 : (now - cd.dropTime) / 1000.0f; 
    	sb
    		.append( summary() );
    	sb
    		.append( "<div style='margin-top: 1ex'>" )
    		.append( "SELECT: ").append( selectSeconds ).append( "s since first")
    		.append( ", " ).append( cs.requests ).append( " requests" )
    		.append( ", " ).append( cs.hits ).append( " hits" )
    		.append( ", " ).append( cs.misses ).append( " misses")
    		.append( ", " ).append( cs.drops ).append( " drops" )
    		.append( " (last " ).append( selectDropSeconds ).append( "s ago)" )
    		.append( ".</div>" )
    		.append( "\n" )
    		;
    	sb
			.append( "<div style='margin-top: 1ex'>" )
			.append( "VIEW: ").append( viewSeconds ).append( "s since first")
    		.append( ", " ).append( cd.requests ).append( " requests" )
			.append( ", " ).append( cd.hits ).append( " hits" )
			.append( ", " ).append( cd.misses ).append( " misses")
			.append( ", " ).append( cd.drops ).append( " drops" )
    		.append( " (last " ).append( viewDropSeconds ).append( "s ago)" )
			.append( ".</div>" )
			.append( "\n" )
			;
//    	for (Map.Entry<String, List<Resource>> e: cs.entrySet()) {
//    		sb.append( "<pre>" );
//    		sb.append( e.getKey().replaceAll( "\n", " " ).replaceAll( "&", "&amp;" ).replaceAll( "<", "&lt;" ) );
//    		sb.append( "</pre>\n" );
//    	}
    }
    
    static class Cachelet<K, V> {
    	protected final Map<K, V> map = new HashMap<K, V>();
    	
    	protected long baseTime = 0;
    	protected long dropTime = 0;
    	protected int requests = 0;
    	protected int hits = 0;
    	protected int misses = 0;
    	protected int drops = 0;
    	
    	public void resetCounts() {
    		baseTime = dropTime = 0;
    		requests = hits = misses = drops = 0;
    	}
    	
		public V get( String key ) {
			requests += 1;
			if (baseTime == 0) baseTime = System.currentTimeMillis();
			V result = map.get( key );
			if (result == null) misses += 1; else hits += 1;
			return result;
		}

		public void put( K key, V value ) {
			if (baseTime == 0) baseTime = System.currentTimeMillis();
			map.put( key, value );
		}

		public int size() {
			return map.size();
		}
		
		public void clear() {
			drops += 1;
			map.clear();
		}

		public Set<Map.Entry<K, V>> entrySet() {
			return map.entrySet();
		}
    }

    protected abstract boolean exceedsSelectLimit( Cachelet<String, List<Resource>> m) ;

    protected abstract boolean exceedsResultSetLimit( Cachelet<String, APIResultSet> m );

    private final Cachelet<String, APIResultSet> cd = new Cachelet<String, APIResultSet>();

    private final Cachelet<String, List<Resource>> cs = new Cachelet<String, List<Resource>>();

    @Override public synchronized APIResultSet getCachedResultSet( List<Resource> results, String view ) {
        return cd.get( results.toString() + "::" + view );
    }

    @Override public synchronized List<Resource> getCachedResources( String select ) {
        return cs.get( select );
    }

    @Override public synchronized void cacheDescription( List<Resource> results, String view, APIResultSet rs ) {
        if (log.isDebugEnabled()) log.debug( "caching descriptions for resources " + results );
        cd.put( results.toString() + "::" + view, rs );
        if (exceedsResultSetLimit( cd )) {
        	if (log.isDebugEnabled()) log.debug( "clearing description cache for " + label );
            cd.clear();
        }
    }

    @Override public synchronized void cacheSelection( String select, List<Resource> results ) {
    	if (log.isDebugEnabled()) log.debug( "caching resource selection for query " + select );
    	cs.put( select, results );
        if (exceedsSelectLimit( cs )) {
        	if (log.isDebugEnabled()) log.debug( "clearing select cache for " + label );
            cs.clear();
        }
    }

    @Override public synchronized void resetCounts() {
        cs.resetCounts();
        cd.resetCounts();
    }

    @Override public synchronized void clear() {
        cs.clear();
        cd.clear();
    }

    @Override public synchronized int numEntries() {
        return cd.size() + cs.size();
    }
}
