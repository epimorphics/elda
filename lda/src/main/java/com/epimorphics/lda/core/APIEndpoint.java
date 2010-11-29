/*
    See lda-top/LICENCE for the licence for this software.
    (c) copyright Epimorphics Limited 2010
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

import com.epimorphics.lda.renderers.Renderer;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * An particular api endpoint receives "calls" (can be GET or POST) via a Router, extracts
 * the parameters defining the request, combines that with
 * some underlying API specification, retrieves the relevant
 * results page and returns it with the appropriate Mime type.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public interface APIEndpoint {
    
    /**
     * The URI template at which this APIEndpoint should be attached
     */
    public String getURITemplate();
    
    /**
     * Called by the Router to invoke the API and return set of result matches
     * @param context The call parameters and other context information
     * @return result set ready for rendering
     */
    public APIResultSet call(CallContext context);
    
    /**
     * Return a metadata description for the query that would be run by this endpoint
     */
    public Resource getMetadata(CallContext context, Model metadata);
    
    /**
     * Return the specification for this endpoint
     */
    public APIEndpointSpec getSpec();
    
    /**
     * Return a render appropriate for the given mimetype
     */
    public Renderer getRendererFor(String mimetype);
}

