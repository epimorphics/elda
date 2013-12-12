/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.support;

/**
    a Controls object contains control and report values for Elda
    execution. At this time we have only a cache-suppress control
    and the Times reports.
*/
public class Controls {
	
	/**
	    True iff this control permits the use of Elda caching.
	*/
	public final boolean allowCache;
	
	/**
	    The Times object to update with query timing reports.
	*/
	public final Times times;
	
	/**
	    A Controls that permits use of the cache and allocates its
	    own Times object.
	*/
	public Controls() {
		this( true, new Times() );
	}
	
	/** 
	    A Controls that uses the supplied values for allowCache and
	    the Times object.
	*/
	public Controls( boolean allowCache, Times times ) {
		this.allowCache = allowCache;
		this.times = times;
	}
}
