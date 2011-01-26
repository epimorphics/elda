/**
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.cache;

public class PermaController extends ControllerBase implements Cache.Controller {

	private static final class Factory implements CacheNewer {
		public Cache New( String policyValue ) { return new PermaCache(); }
	}

	public PermaController() {
		super( new Factory() );
	}
}