/*
	See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
	for the licence for this software.

	(c) Copyright 2011 Epimorphics Limited
	$Id$
*/

package com.epimorphics.lda.bindings;

import java.net.URL;

/**
    A interface of one method which maps a possibly partial path
    to a URL.
    
 	@author chris
*/
public interface URLforResource {
	URL asResourceURL( String u );
	
	public static final URLforResource alwaysFails = new URLforResource() {
		@Override public URL asResourceURL( String p ) { 
			throw new RuntimeException( "this context can't make a URL for " + p ); 
		}
	};

}