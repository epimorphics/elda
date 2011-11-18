/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
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
	
	public final boolean allowCache;
	
	public final Times times;
	
	public Controls( boolean allowCache, Times times ) {
		this.allowCache = allowCache;
		this.times = times;
	}
}
