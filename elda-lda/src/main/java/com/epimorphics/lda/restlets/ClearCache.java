/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.restlets;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import com.epimorphics.lda.cache.Cache;

@Path( "/control/clear-cache") public class ClearCache 
	{
	@POST @Produces("text/plain") public Response clearCachePOST() throws URISyntaxException { 
		Cache.Registry.clearAll();
		return Response.seeOther( new URI("control/show-cache")).build();
	}
	
	@GET @Produces("text/plain") public Response clearCacheGET() throws URISyntaxException { 
		Cache.Registry.clearAll();
		return Response.seeOther( new URI("control/show-cache?warn=true")).build();
	}
}
