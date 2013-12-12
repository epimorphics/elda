/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

/******************************************************************
    File:        Router.java
    Created by:  Dave Reynolds
    Created on:  31 Jan 2010
 * 
 * (c) Copyright 2010, Epimorphics Limited
 * $Id:  $
 *****************************************************************/

package com.epimorphics.lda.routing;

import java.net.URI;
import java.util.List;

import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.support.MultiMap;

/**
 	Abstraction for the dispatch part of the API. Supports dynamic
 	registration of URI templates against API instances. Could
 	be implemented directly by a servlet, via Restlet or via
 	JAX-RS. Initially assume JAX-RS, just need to work out how.
  
 	@author <a href="mailto:der@epimorphics.com">Dave Reynolds</a>
 	@version $Revision: $
 */
public interface Router {

    /**
     	Register a new API instance.
     	@param URITemplate the path template, relative server root
     	@param api the api implementation
    */
    void register(String context, String URITemplate, APIEndpoint api);
    
    /**
     	Remove a registered api
    */
    void unregister(String context, String URITemplate);
    
    /**
     	Match the request path to the known endpoints and return
     	a Match object (giving the APIEndpoint and any template bindings)
     	or null if the request does not match.
    */
    public Match getMatch( String path, MultiMap<String, String> queryParams );
    
    /**
        Return a list of URI templates registered with this Router.
    */
    public List<String> templates();
    
    /**
        Return the URI template of an endpoint in this Router
        which has an ItemEndpoint matching the itemPath.
    */
    public String findItemURIPath( String context, URI requestURI, String itemPath  );

    /**
        Return the number of URI templates served by this Router.
    */
	public int countTemplates();
    
}

