/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
*/
package com.epimorphics.lda.acceptance.tests;

import java.util.List;

/**
    A AskTest is a URI which is deemed to have been the source
    of the bindings and a collection of Ask tests to apply.
*/
public class UriAndAsks {
	
	protected final String uri;
	protected final List<Ask> asks;

	public UriAndAsks( String uri, List<Ask> asks ) {
		this.uri = uri; 
		this.asks = asks;
	}
}