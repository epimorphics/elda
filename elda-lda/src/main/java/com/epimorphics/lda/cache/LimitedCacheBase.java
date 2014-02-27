/**
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
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
    	sb
			.append( "<div style='margin-top: 1ex'>" )
			.append( "COUNTS: ").append( viewSeconds ).append( "s since first")
    		.append( ", " ).append( cc.requests ).append( " requests" )
			.append( ", " ).append( cc.hits ).append( " hits" )
			.append( ", " ).append( cc.misses ).append( " misses")
			.append( ", " ).append( cc.drops ).append( " drops" )
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
    
    public static class TimedThing<T> {
    	
    	protected final T thing;
    	protected final long expiresAt;
    	
    	public TimedThing(T thing, long expiryDuration) {
    		this.thing = thing;
    		this.expiresAt = System.currentTimeMillis() + expiryDuration;
    	}
    	
    	public boolean isLive() {
    		return System.currentTimeMillis() < expiresAt;
    	}
    	
    	public boolean hasExpired() {
    		return !isLive();
    	}
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

		public void remove(K key) {
			map.remove(key);
		}
    }

    protected abstract boolean exceedsSelectLimit( Cachelet<String, List<Resource>> m) ;

    protected abstract boolean exceedsResultSetLimit( Cachelet<String, TimedThing<APIResultSet>> m );

    private final Cachelet<String, TimedThing<APIResultSet>> cd = new Cachelet<String, TimedThing<APIResultSet>>();

    private final Cachelet<String, List<Resource>> cs = new Cachelet<String, List<Resource>>();
    
    private final Cachelet<String, Integer> cc = new Cachelet<String, Integer>();

    @Override public synchronized APIResultSet getCachedResultSet( List<Resource> results, String view ) {
        String key = results.toString() + "::" + view;
		TimedThing<APIResultSet> t = cd.get( key );
        if (t == null) return null;
        if (t.hasExpired()) {
        	System.err.println( ">> removing expired key '" + key + "'" );
        	cd.remove(key); 
        }
        return t.thing;
    }

    @Override public synchronized List<Resource> getCachedResources( String select ) {
        return cs.get( select );
    }

    @Override public synchronized void cacheDescription( List<Resource> results, String view, APIResultSet rs, long duration ) {
        if (log.isDebugEnabled()) log.debug( "caching descriptions for resources " + results );
        cd.put( results.toString() + "::" + view, new TimedThing<APIResultSet>(rs, duration ));
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

	/**
	    Get the total number of items that this query will return, -1 for
	    "not known".
	*/
	@Override public synchronized int getCount(String countQueryString) {
		Integer already = cc.get(countQueryString);
		return already == null ? -1 : already.intValue();
	}
	
	/**
	    Put the total number of items that this query returns.
	*/
	@Override public synchronized void putCount(String countQueryString, int count) {
		if (cc.size() > countLimit()) cc.clear();
		cc.put(countQueryString, new Integer(count));
	}
	
	// temporary
	public int countLimit() {
		return 1000;
	}

    @Override public synchronized void resetCounts() {
        cs.resetCounts();
        cd.resetCounts();
        cc.resetCounts();
    }

    @Override public synchronized void clear() {
        cs.clear();
        cd.clear();
        cc.clear();
    }

    @Override public synchronized int numEntries() {
        return cd.size() + cs.size();
    }
}
