/**
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.cache;

import java.net.URI;
import java.util.List;
import java.util.Map;

import com.epimorphics.lda.cache.Cache.Clock;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.core.ResponseResult;
import com.hp.hpl.jena.rdf.model.Resource;

public class LimitTriplesController extends ControllerBase {

	static final int DEFAULT = 20000;
	
	static class LimitTriplesCache extends LimitedCacheBase {

		private final int limit;

		public LimitTriplesCache( String label, int limit ) {
			this(Clock.SystemClock, label, limit);
		}
		
		public LimitTriplesCache( Clock clock, String label, int limit ) {
			super( clock, label );
			this.limit = limit;
		}

		@Override protected boolean exceedsCountLimit(Cachelet<String, TimedThing<Integer>> cr) {
			return cr.size() > limit/10;
		}

		@Override protected synchronized boolean exceedsSelectLimit( Cachelet<String, TimedThing<List<Resource>>> m ) {
			return m.size() > limit;
		}

		@Override protected synchronized boolean exceedsResultSetLimit( Cachelet<String, TimedThing<APIResultSet>> m) {
			long size = 0;
			for (Map.Entry<String, TimedThing<APIResultSet>> e: m.entrySet()) size += e.getValue().thing.modelSize();
			return size > limit;
		}

		@Override protected boolean exceedsResponseLimit(Cachelet<URI, TimedThing<ResponseResult>> cr) {
			long size = 0;
			for (Map.Entry<URI, TimedThing<ResponseResult>> e: cr.entrySet()) 
				size += e.getValue().thing.resultSet.modelSize();
			return size > limit;
		}
	}
	
	protected final static class Factory implements CacheNewer {
		
		final Clock clock;
		
		public Factory(Clock clock) {
			this.clock = clock;
		}
		
		@Override public Cache New( String label, String policyValue ) {
			int limit = policyValue.length() == 0 ? DEFAULT : Integer.parseInt( policyValue );
			return new LimitTriplesCache( clock, label, limit );
		}
	}
	
	public LimitTriplesController(Clock c) {
		super( new Factory(c) );
	}
	
	public LimitTriplesController() {
		this(Clock.SystemClock);
	}
}
