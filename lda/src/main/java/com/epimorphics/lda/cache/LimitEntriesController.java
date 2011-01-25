package com.epimorphics.lda.cache;

import java.util.List;
import java.util.Map;

import com.epimorphics.lda.core.APIResultSet;
import com.hp.hpl.jena.rdf.model.Resource;

public class LimitEntriesController extends ControllerBase {

	static final int DEFAULT = 100;
	
	static class Thingy extends LimitedCacheBase {

		private final int limit;
		
		public Thingy( int limit ) {
			this.limit = limit;
		}

		@Override protected boolean exceedsSelectLimit( Map<String, List<Resource>> m ) {
			return m.size() > limit;
		}

		@Override protected boolean exceedsResultSetLimit(Map<String, APIResultSet> m) {
			return m.size() > limit;
		}
	}
	
	protected final static class Factory implements CacheFactory {
		
		@Override public Cache New( String policyValue ) {
			int limit = policyValue.length() == 0 ? DEFAULT : Integer.parseInt( policyValue );
			return new Thingy( limit );
		}
	}
	
	protected LimitEntriesController() {
		super( new Factory() );
	}

}
