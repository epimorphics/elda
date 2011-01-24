/*
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

	public APIResultSet getCachedResultSet(List<Resource> results, String view );

	public List<Resource> getCachedResources( String select );

	public void cacheDescription(List<Resource> results, String view, APIResultSet rs);

	public void cacheSelection(String select, List<Resource> results);
	
	public interface CacheMaker {
		public Cache create( Source s );		
	}
	
	public static class Registry {
		
		protected static final Map<String, CacheMaker> map = new HashMap<String, CacheMaker>();
		
		public static synchronized void add( String policyName, CacheMaker cm ) {
			map.put( policyName, cm );
		}

		static { 
			try { Class.forName( PermaCache.class.getName() ); }
			catch (Exception e) { throw new RuntimeException( e ); }
		}
		
		public static synchronized Cache forSource( String policyName, Source source ) {
			CacheMaker cm = map.get( policyName );
			if (cm == null) throw new RuntimeException( "no CacheMaker for policy '" + policyName + "'" );
		    return cm.create( source );
		}
		
	}
	
}