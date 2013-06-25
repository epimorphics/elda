/**
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.cache;

import java.util.*;

import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.exceptions.EldaException;
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
	    Make an entry in the cache that binds the given list of results
	    to the given select query string.
	*/
	public void cacheSelection( String selectQuery, List<Resource> results );

	/**
	    Answer the list of resources associated with this select query if
	    any is available (entered and not discarded).
	*/
	public List<Resource> getCachedResources( String select );

	/**
	    Make an entry in the caches that associates the result set <code>rs</code>
	    with the given list of resources and view.
	 */
	public void cacheDescription( List<Resource> results, String view, APIResultSet rs );
	
	/**
	    Answer the API result set remembered for the given list of results and
	    view, or null if there isn't one.
	 */
	public APIResultSet getCachedResultSet(List<Resource> results, String view );
	
	/**
	    Clear this cache.
	*/
	public void clear();

	/**
	    Reset to suitable zeroes the counts of this cache.
	*/
	public void resetCounts();
	
	/**
	    Append an HTML description of this cache to <code>sb</code>.
	*/
	public void show( StringBuilder sb );

	/**
	    Answer how many entries are in this cache.
	*/
	public int numEntries();
	
	/**
	    Answer a summary description of this cache. In particular, include
	    a unique ID.
	*/
	public String summary();
	
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
		
		/**
		    Answer a summary of this cache controller's state.
		*/
		public String summary();

		/**
		    Answer an HTML description of all the cache's states.
		*/
		public void showAll(StringBuilder sb);

		/**
		    Reset the counts of all the caches in this controller.
		*/
		public void resetCounts();
	}
	
	/**
	    The registry records named cache policies.
	*/
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
			// System.err.println( ">> cacheFor " + policy + " [" + source + "]" );
			String [] p = policy.split( ":", 2 );
			String policyName = p[0], policyValue = (p.length == 2 ? p[1] : "");
			Controller cm = map.get( policyName );
			if (cm == null) EldaException.NotFound( "cacheMaker policy", policyName );
		    return cm.cacheFor( source, policyValue );
		}
		
		public static void clearAll() {
			for (Map.Entry<String, Controller> e: map.entrySet()) {
				// System.err.println( ">> clearing cache controller " + e.getKey() + " (" + e.getValue().summary() + ")" );
				e.getValue().clearAll();
			}
		}
		
		public static void showAll( StringBuilder sb ) {
			for (Map.Entry<String, Controller> e: map.entrySet()) {
				sb.append( "<h2>details for cache group '" + e.getKey() + "'</h2>\n" );
				e.getValue().showAll( sb );			
			}
		}

		protected static int identityCounter = 0;
		
		public static synchronized int newIdentity() {
			return ++identityCounter;
		}

		/**
		    Reset the counts of all the registered caches.
		*/
		public static void resetCounts() {
			for (Map.Entry<String, Controller> e: map.entrySet()) {
				e.getValue().resetCounts();			
			}
		}
	}
}

		