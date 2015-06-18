/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

/******************************************************************
    File:        Api.java
    Created by:  Dave Reynolds
    Created on:  31 Jan 2010
 * 
 * (c) Copyright 2010, Epimorphics Limited
 * $Id:  $
 *****************************************************************/

package com.epimorphics.lda.core;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.apache.http.client.utils.URIBuilder;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.query.QueryParameter;
import com.epimorphics.lda.renderers.Renderer;
import com.epimorphics.lda.shortnames.CompleteContext;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.support.Controls;
import com.epimorphics.lda.support.NoteBoard;
import com.epimorphics.util.MediaType;
import com.epimorphics.util.URIUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * An particular api endpoint receives "calls" (can be GET or POST) via a Router, extracts
 * the parameters defining the request, combines that with
 * some underlying API specification, retrieves the relevant
 * results page and returns it with the appropriate Mime type.
 * 
 * @author <a href="mailto:der@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public interface APIEndpoint {	
	
	public static class Request {
		public final Controls c;
		public final URI requestURI;
		public final Bindings bindings;
		public final CompleteContext.Mode mode;
		
		public final String format;
		public final List<String> formatNames;
			
		public Request(Controls c, URI requestURI, Bindings context) {
			this(c, requestURI, context, CompleteContext.Mode.RoundTrip, Collections.<String>emptyList(), "");
		}
			
		private Request(Controls c, URI requestURI, Bindings bindings, CompleteContext.Mode mode, List<String> formatNames, String format) {
			this.c = c;
			this.requestURI = requestURI;
			this.bindings = bindings;
			this.mode = mode;
			this.format = format;
			this.formatNames = formatNames;
		}
	
		public Request withMode(CompleteContext.Mode mode) {
			return new Request(c, requestURI, bindings, mode, formatNames, format);
		}
	
		public Request withBindings(Bindings newBindings) {
			return new Request(c, requestURI, newBindings, mode, formatNames, format);
		}
	
		public Request withFormats(List<String> formatNames, String format) {
			return new Request(c, requestURI, bindings, mode, formatNames, format);
		}

		/**
			getURIwithFormat() returns the specified-format form of the requestURI, where
			the format name is explicitly specified as suffix .format and any 
			existing _format= is stripped. This form of the URI is suitable as
			a cache key because the renderer name is explicitly present, 
			whereas the requestURI might not (when a renderer is selected by content negotiation).
		*/
		public URI getURIwithFormat() {
			URI a = UriBuilder.fromUri(requestURI).replaceQueryParam("_format").build();
			URI b = URIUtils.changeFormatSuffix(a, formatNames, format);
			return b;
		}

		/**
			getURIPlain returns the requestURI with suffix and query parameters
			that do not affect the SPARQL select and view queries removed.
		*/
		public URI getURIplain() {			
			URI x = UriBuilder.fromUri(URIUtils.changeFormatSuffix(requestURI, formatNames, ""))
				.replaceQueryParam(QueryParameter._FORMAT)
				.replaceQueryParam(QueryParameter._METADATA)
				.replaceQueryParam(QueryParameter._MARK)
				.replaceQueryParam(QueryParameter.callback)
				.build()
				;
			return x;
		}
	}
    
    /**
     * The URI template at which this APIEndpoint should be attached
     */
    public String getURITemplate();
    
    /**
     	Called by the Router to invoke the API and return set of result matches
     	
     	@param r the request
     	@return ResponseResult: the ResultSet for rendering and the call context used for the result set.
    */    
    public ResponseResult call( Request r, NoteBoard nb );
    
    /**
     	Return a metadata description for the query that would be run by this endpoint
    */
    public Resource getMetadata( Bindings context, URI requestURI, String formatName, Model metadata );
    
    /**
        Return the specification for this endpoint
    */
    public APIEndpointSpec getSpec();

    /**
        Return the renderer known by the given name.
    */
	public Renderer getRendererNamed( String name );

    /**
        Return the renderer known by the given media type.
    */
	public Renderer getRendererByType( MediaType mt );
	
	/**
	    Return any default bindings that this endpoint has set up (which need
	    not be ones specified by a config).
	*/
	public Bindings defaults();

	/**
	 	An API spec (and hence an endpoint) may have been defined with a
	 	prefix path string, which follows the context path. Return the
	 	prefix path, or the empty string if it was defined with no prefix path.
	*/
	public String getPrefixPath();
}

