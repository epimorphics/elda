/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.routing;

import java.util.HashMap;
import java.util.Map;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.APIEndpoint;

/**
    The default router is just a wrapper around a MatchSearcher, qv.
    The adaption is to return a Match object and to uplift the bindings.
  
  	@author eh
*/
public class DefaultRouter extends MatchSearcher<APIEndpoint> implements Router {

	@Override public Match getMatch( String path ) {
        Map<String, String> bindings = new HashMap<String, String>();
        APIEndpoint e = lookup( bindings, path );
        if (e == null) return null;
        else return new Match( e, Bindings.uplift( bindings ) );
	}
}