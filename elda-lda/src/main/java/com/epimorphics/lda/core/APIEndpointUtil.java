/*
	See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
	for the licence for this software.

	(c) Copyright 2011 Epimorphics Limited
	$Id$
*/
package com.epimorphics.lda.core;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.renderers.Renderer;
import com.epimorphics.lda.renderers.RendererFactory;
import com.epimorphics.lda.routing.Match;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.support.MultiMap;
import com.epimorphics.lda.support.NoteBoard;
import com.epimorphics.util.MediaType;
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
	public static ResponseResult call( APIEndpoint.Request r, NoteBoard nb, Match match, String contextPath, MultiMap<String, String> queryParams ) {
		APIEndpoint ep = match.getEndpoint();
		
		Bindings vs = new Bindings( r.context )
			.updateAll( match.getBindings() )
			.put( "_suffix", r.format )
			.put( "_APP", contextPath )
			.put( "_HOST", getHostAndPort( r.requestURI ) )
			;
		
		Bindings cc = Bindings.createContext( vs, queryParams );
		return ep.call( r.withBindings(cc), nb );
	}

	private static String getHostAndPort(URI u) {
		String host = u.getHost();
		int port = u.getPort();
		return port < 1 ? host : host + ":" + port;
	}

	/**
	    <p>
	    Answer the renderer particular to the endpoint <code>ep</code>. If name is
	    not null, then the renderer is the one with that name in the endpoint itself.
	    Otherwise, if the endpoint has any renderers with a media type in the list
	    <code>types</code>, it picks the first such renderer. Otherwise it falls
	    back to the default renderer (which, by default, is JSON).
	    </p>
	    
	    <p>
	    Epimorphic extension:
	    If the endpoint's spec has a binding other than "no" for the variable
	    <code>_suppress_media_type</code>, then the search of media types is
	    not done, so that name=null will fall through to the default renderer.
	    </p>
	*/
	public static Renderer getRenderer( APIEndpoint ep, String name, List<MediaType> types ) {
		APIEndpointSpec spec = ep.getSpec();
		if (name == null) {
			String suppress = spec.getBindings().getAsString( "_supress_media_type", "no" );
	        if (suppress.equals("no")) {
	            for (MediaType mt: types) {
	                Renderer byType = ep.getRendererByType( mt );
	                if (byType != null) return byType;
	            }
	        }
	        RendererFactory rf = spec.getRendererFactoryTable().getDefaultFactory();
			return rf.buildWith( ep, spec.sns() );           
	        }
	    else
	        return ep.getRendererNamed( name );
	}
}
