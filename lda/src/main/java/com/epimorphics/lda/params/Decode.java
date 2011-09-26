/*
	See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
	for the licence for this software.

	(c) Copyright 2011 Epimorphics Limited
	$Id$
*/
package com.epimorphics.lda.params;

import java.util.Set;

/**
    New (or relocated) code for handling parameters. Pulling it out
    here allows it to be generalised and remixed without requiring
    all the apparatus of the current APIQuery and ContextQueryUpdater
    (guilty, yr'onour).
 
 	@author chris
*/
public class Decode {
	
	public final boolean chatty;
	
	public Decode(boolean chatty) {
		this.chatty = chatty;
	}

	public Decode handleQueryParameters( Set<String> parameterNames ) {
		
		return this;
	}
	
	public void reveal() {
		// if (chatty) System.err.println( ">> DONE." );
	}

}
