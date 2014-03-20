/**
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.

    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.cache;

import java.net.URI;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.core.ResponseResult;
import com.epimorphics.lda.restlets.RouterRestletSupport;
import com.hp.hpl.jena.rdf.model.Resource;

public abstract class LimitedCacheBase implements Cache {

    private static Logger log = LoggerFactory.getLogger( LimitedCacheBase.class );

    protected final String label;
    protected final int identity;
    protected final Clock clock;

    public LimitedCacheBase( Clock clock, String label ) {
        this.label = label;
        this.clock = clock;
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
    	sb
			.append( "<div style='margin-top: 1ex'>" )
			.append( "RESULT: ").append( viewSeconds ).append( "s since first")
    		.append( ", " ).append( cr.requests ).append( " requests" )
			.append( ", " ).append( cr.hits ).append( " hits" )
			.append( ", " ).append( cr.misses ).append( " misses")
			.append( ", " ).append( cr.drops ).append( " drops" )
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
    	
    	public final T thing;
    	public final long expiresAt;
    	
    	public TimedThing(T thing, long expiresAt) {
    		this.thing = thing;
    		this.expiresAt = expiresAt;
    	}
    	
    	// negative expiry times mean there is no expiry time.
    	public boolean hasExpired(Clock c) {
    		return 0 < expiresAt && expiresAt < c.currentTimeMillis();
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
    	
		public V get( K key ) {
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
    
    @Override public synchronized TimedThing<ResponseResult> fetch(URI uri) {
    	
    	// if (true) return null;
    	
    	TimedThing<ResponseResult> tt = cr.get(uri);
    	if (tt == null) return null;
    	if (tt.hasExpired(clock)) {
//       	System.err.println( ">> removing expired key '" + uri + "'" );
    		cr.remove(uri); 
    		return null;
    	}
    	return tt;
    }
    
    @Override public synchronized void store(URI uri, ResponseResult toStore, long expiresAt) {
//    	System.err.println( ">> caching " + uri + " until " + RouterRestletSupport.expiresAtAsRFC1123(expiresAt));
    	cr.put(uri, new TimedThing<ResponseResult>(toStore, expiresAt));
    	if (exceedsResponseLimit( cr )) cr.clear();
    }
    
    protected abstract boolean exceedsResponseLimit(Cachelet<URI, TimedThing<ResponseResult>> cr);
    
    protected abstract boolean exceedsCountLimit(Cachelet<String, TimedThing<Integer>> cr);

	protected abstract boolean exceedsSelectLimit( Cachelet<String, TimedThing<List<Resource>>> m) ;

    protected abstract boolean exceedsResultSetLimit( Cachelet<String, TimedThing<APIResultSet>> m );

    private final Cachelet<String, TimedThing<APIResultSet>> cd = new Cachelet<String, TimedThing<APIResultSet>>();

    private final Cachelet<String, TimedThing<List<Resource>>> cs = new Cachelet<String, TimedThing<List<Resource>>>();
    
    private final Cachelet<String, TimedThing<Integer>> cc = new Cachelet<String, TimedThing<Integer>>();
    
    private final Cachelet<URI, TimedThing<ResponseResult>> cr = new Cachelet<URI, TimedThing<ResponseResult>>();

    @Override public synchronized TimedThing<APIResultSet> getCachedResultSet( List<Resource> results, String view ) {
        String key = results.toString() + "::" + view;
		TimedThing<APIResultSet> t = cd.get( key );
        if (t == null) return null;
        if (t.hasExpired(clock)) {
//        	 System.err.println( ">> removing expired key '" + key + "'" );
        	cd.remove(key); 
        	return null;
        }
        return t;
    }

    @Override public synchronized List<Resource> getCachedResources( String select ) {
        TimedThing<List<Resource>> t = cs.get( select );
        if (t == null) return null;
        if (t.hasExpired(clock)) {
        	cs.remove(select);
        	return null;
        }
        return t.thing;
    }

    @Override public synchronized void cacheDescription( List<Resource> results, String view, APIResultSet rs, long expiresAt ) {
        if (log.isDebugEnabled()) log.debug( "caching descriptions for resources " + results );
        cd.put( results.toString() + "::" + view, new TimedThing<APIResultSet>(rs, expiresAt ));
        if (exceedsResultSetLimit( cd )) {
        	if (log.isDebugEnabled()) log.debug( "clearing description cache for " + label );
            cd.clear();
        }
    }

    @Override public synchronized void cacheSelection( String select, List<Resource> results, long expiresAt ) {
    	if (log.isDebugEnabled()) log.debug( "caching resource selection for query " + select );
    	cs.put( select, new TimedThing<List<Resource>>(results, expiresAt) );
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
		TimedThing<Integer> already = cc.get(countQueryString);
		if (already == null) return -1;
		if (already.hasExpired(clock)) {
			cc.remove(countQueryString);
			return -1;
		}
		return already.thing.intValue();
	}
	
	/**
	    Put the total number of items that this query returns.
	*/
	@Override public synchronized void putCount(String countQueryString, int count, long expiresAt) {
		cc.put(countQueryString, new TimedThing<Integer>(new Integer(count), expiresAt));
		if (exceedsCountLimit(cc)) cc.clear();
	}

    @Override public synchronized void resetCounts() {
        cs.resetCounts();
        cd.resetCounts();
        cc.resetCounts();
        cr.resetCounts();
    }

    @Override public synchronized void clear() {
        cs.clear();
        cd.clear();
        cc.clear();
        cr.clear();
    }

    @Override public synchronized int numEntries() {
        return cd.size() + cs.size();
    }
}
