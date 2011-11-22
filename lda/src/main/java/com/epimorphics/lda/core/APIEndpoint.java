/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
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

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.renderers.Renderer;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.support.Controls;
import com.epimorphics.util.MediaType;
import com.epimorphics.util.Triad;
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
    
    /**
     * The URI template at which this APIEndpoint should be attached
     */
    public String getURITemplate();
    
    /**
     	Called by the Router to invoke the API and return set of result matches
     	
     	@param context The call parameters and other context information
     	@return Triad(rs, format, cc): the ResultSet for rendering, the name
     		of the format, and the call context used for the result set.
    */
    public Triad<APIResultSet, String, Bindings> call( Controls c, URI reqestURI, Bindings context );
    
    /**
     	Return a metadata description for the query that would be run by this endpoint
    */
    public Resource getMetadata( Bindings context, URI requestURI, Model metadata );
    
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
        TODO: consider the possibility that there's more than one.
    */
	public Renderer getRendererByType( MediaType mt );
}

