/**
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.cache;

import java.util.Map;
import java.util.List;

import com.epimorphics.lda.core.APIResultSet;
import com.hp.hpl.jena.rdf.model.Resource;

public class LimitTriplesController extends ControllerBase {

	static final int DEFAULT = 20000;
	
	static class LimitTriplesCache extends LimitedCacheBase {

		private final int limit;
		
		public LimitTriplesCache( String label, int limit ) {
			super( label );
			this.limit = limit;
		}

		@Override protected boolean exceedsSelectLimit( Map<String, List<Resource>> m ) {
			return false; // return m.size() > limit;
		}

		@Override protected boolean exceedsResultSetLimit(Map<String, APIResultSet> m) {
			long size = 0;
			for (Map.Entry<String, APIResultSet> e: m.entrySet()) size += e.getValue().size();
			return size > limit;
		}
	}
	
	protected final static class Factory implements CacheNewer {
		
		@Override public Cache New( String label, String policyValue ) {
			int limit = policyValue.length() == 0 ? DEFAULT : Integer.parseInt( policyValue );
			return new LimitTriplesCache( label, limit );
		}
	}
	
	public LimitTriplesController() {
		super( new Factory() );
	}
}
