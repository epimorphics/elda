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

import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.sources.Source;
import com.hp.hpl.jena.rdf.model.Resource;

/**
    The Cache interface names the methods used by the Query
    constructor/issuer to fetch and store results that may be
    requested again. The implementation of the cache is not
    prescribed.
*/
public interface Cache {

	/**
	    Answer the API result set remembered for the given list of results and
	    view, or null if there isn't one.
	*/
	public APIResultSet getCachedResultSet(List<Resource> results, String view );

	public List<Resource> getCachedResources( String select );

	public void cacheDescription(List<Resource> results, String view, APIResultSet rs);

	public void cacheSelection(String select, List<Resource> results);
	
	public void clear();

	public int numEntries();
	
	public interface Controller {
		/**
		    Answer a Cache associated with the given Source. Create one if
		    there isn't one known.
		*/
		public Cache cacheFor( Source s, String policyValue );	
		
		/**
		    Clear and remove the cache associated with the given source. 
		*/
		public void clear( Source s );
		
		/**
		 	Clear and remove all the caches that this maker knows about.
		*/
		public void clearAll();
	}
	
	public static class Registry {
		
		protected static final Map<String, Controller> map = new HashMap<String, Controller>();
		
		public static synchronized void add( String policyName, Controller cm ) {
			map.put( policyName, cm );
		}

		static { 
	    	Cache.Registry.add( "default", new LimitTriplesController() );
	    	Cache.Registry.add( "perma-cache", new PermaController() );
	    	Cache.Registry.add( "limit-entries", new LimitEntriesController() );
	    	Cache.Registry.add( "limit-triples", new LimitTriplesController() );
		}
		
		public static synchronized Cache cacheFor( String policy, Source source ) {
			String [] p = policy.split( ":", 2 );
			String policyName = p[0], policyValue = (p.length == 2 ? p[1] : "");
			Controller cm = map.get( policyName );
			if (cm == null) throw new RuntimeException( "no CacheMaker for policy '" + policyName + "'" );
		    return cm.cacheFor( source, policyValue );
		}
		
		public static void clearAll() {
			for (Map.Entry<String, Controller> e: map.entrySet())
				e.getValue().clearAll();
		}
		
	}
}