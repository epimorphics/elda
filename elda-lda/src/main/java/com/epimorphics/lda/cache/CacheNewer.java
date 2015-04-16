/**
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.cache;

interface CacheNewer {
	/**
	    Answer a new, freshly-allocated cache, which respects
	    the policy value. (Interpretation of the value is
	    cache-specific, but the empty string is used for a
	    sensible default.)
	*/
	Cache New( String label, String policyValue ); 
}