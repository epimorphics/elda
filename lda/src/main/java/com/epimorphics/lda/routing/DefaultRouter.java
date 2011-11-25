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
import com.epimorphics.lda.bindings.Lookup;
import com.epimorphics.lda.core.APIEndpoint;

/**
    The default router is a wrapper around a MatchSearcher, qv.
    The adaption is to return a Match object and to uplift the bindings.
    The Router also tracks the URI templates associated with ItemTemplates.
  
  	@author eh
*/
public class DefaultRouter extends MatchSearcher<APIEndpoint> implements Router {

	/**
	    Answer the (endpoint, bindings) Match for the given path,
	    or null if there isn't one.
	*/
	@Override public Match getMatch( String path ) {
        Map<String, String> bindings = new HashMap<String, String>();
        APIEndpoint e = lookup( bindings, path );
        return e == null ? null : new Match( e, Bindings.uplift( bindings ) );
	}
	
	static class BaseAndTemplate {
		final String base;
		final String template;
		
		BaseAndTemplate( String base, String template ) { 
			this.base = base; this.template = template; 
		}
	}
	
	protected MatchSearcher<BaseAndTemplate> ms = new MatchSearcher<BaseAndTemplate>();
	
	/**
	    Answer the filled-in URI template associated with the given
	    item path, or null if there isn't one.
	*/
	@Override public String findItemURIPath( String path ) {
		Map<String, String> bindings = new HashMap<String, String>();
		BaseAndTemplate bt = ms.lookup( bindings, path );
		if (bt != null) {
			String apiBase = bt.base == null ? "" : bt.base;
			return apiBase + Bindings.expandVariables( Lookup.Util.asLookup( bindings ), bt.template );
		}
		return null;
	}
	
	/**
	    Register the endpoint ep associated with the URI template ut.
	    Also record the association between the item template (if any)
	    and that URI template, for use in findItemURIPath.
	*/
	@Override public void register( String ut, APIEndpoint ep ) {
		super.register( ut, ep );
		String it = ep.getSpec().getItemTemplate();
		if (it != null) {
			String apiBase = ep.getSpec().getAPISpec().getBase();
			String path = it.replaceFirst( "https?://[^/]*/", "/" );
			ms.register( path, new BaseAndTemplate( apiBase, ep.getURITemplate() ) );
		}
	}
}