/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

/*
	(c) Copyright 2010 Epimorphics Limited
	[see end of file]
	$Id$
*/

package com.epimorphics.lda.cache;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.core.*;
import com.epimorphics.lda.sources.Source;
import com.hp.hpl.jena.rdf.model.Resource;

public class PermaCache implements Cache {

    static Logger log = LoggerFactory.getLogger( PermaCache.class );
    
    public static Cache forSource( Source source ) {
        String key = source.toString();
        PermaCache x = caches.get( key );
        if (x == null) caches.put( key, x = new PermaCache() );
        return x;
    }
    
    /* (non-Javadoc)
	 * @see com.epimorphics.lda.cache.Cache#getCachedResultSet(java.util.List, java.lang.String)
	 */
    @Override
	public APIResultSet getCachedResultSet( List<Resource> results, String view ) { 
        return cd.get( results.toString() + "::" + view );
    }
    
    /* (non-Javadoc)
	 * @see com.epimorphics.lda.cache.Cache#getCachedResources(java.lang.String)
	 */
    @Override
	public List<Resource> getCachedResources( String select ) { 
        return cs.get( select );
    }
    
    /* (non-Javadoc)
	 * @see com.epimorphics.lda.cache.Cache#cacheDescription(java.util.List, java.lang.String, com.epimorphics.lda.core.APIResultSet)
	 */
    @Override
	public void cacheDescription( List<Resource> results, String view, APIResultSet rs ) {
        log.debug( "caching descriptions for resources " + results );
        cd.put( results.toString() + "::" + view, rs );        
    }
    
    /* (non-Javadoc)
	 * @see com.epimorphics.lda.cache.Cache#cacheSelection(java.lang.String, java.util.List)
	 */
    @Override
	public void cacheSelection( String select, List<Resource> results ) {
        log.debug( "caching resource selection for query " + select );
        cs.put( select, results );        
    }
    
    static final Map<String, PermaCache> caches = new HashMap<String, PermaCache>();
    
    private final Map<String, APIResultSet> cd = new HashMap<String, APIResultSet>();
    
    private final Map<String, List<Resource>> cs = new HashMap<String, List<Resource>>();

	public static void clearAll() {
		caches.clear();		
	}
}
    
/*
    (c) Copyright 2010 Epimorphics Limited
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
