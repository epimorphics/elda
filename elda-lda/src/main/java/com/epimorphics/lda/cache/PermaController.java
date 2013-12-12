/**
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.cache;

public class PermaController extends ControllerBase implements Cache.Controller {

	private static final class Factory implements CacheNewer {
		@Override public Cache New( String label, String policyValue ) { 
			return new PermaCache( label ); }
	}

	public PermaController() {
		super( new Factory() );
	}
}