/*
	See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
	for the licence for this software.

	(c) Copyright 2011 Epimorphics Limited
	$Id$
*/
package com.epimorphics.lda.core;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.bindings.VarValues;
import com.epimorphics.lda.routing.Match;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.util.Triad;

public class APIEndpointUtil {
	
    protected static Logger log = LoggerFactory.getLogger(APIEndpointUtil.class);

    /**
        Utility method for calling an endpoint when given a pretty much
        untrammelled collection of arguments.
        
     	@param match the (endpoint, bindings) pair for this invocation
     	@param requestUri the request URI to be presumed
     	@param queryParams map from parameter names to stringy values
     
     	@return a triple (rs, format, cc) of the ResultSet of the invocation,
     		the name of the format suggested for rendering, and the
     		CallContext constructed and used in the invocation.
    */
	public static Triad<APIResultSet, String, CallContext> call( Match match, URI requestUri, MultiMap<String, String> queryParams ) {
		APIEndpoint ep = match.getEndpoint();
		APIEndpointSpec spec = ep.getSpec();
		log.debug("Info: calling APIEndpoint " + spec);
		VarValues vs = new VarValues( spec.getBindings() ).putAll( match.getBindings() );
		return ep.call( CallContext.createContext( requestUri, queryParams, vs ) );
	}
}
