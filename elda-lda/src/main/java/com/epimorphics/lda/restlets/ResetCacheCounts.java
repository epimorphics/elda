/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.restlets;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import com.epimorphics.lda.cache.Cache;
import com.epimorphics.util.URIUtils;

@Path( "/control/reset-counts") public class ResetCacheCounts {
	
	@POST @Produces("text/html") public Response resetCache() {
		Cache.Registry.resetCounts();
		return Response.seeOther( URIUtils.newURI( "/control/show-cache" ) ).build();
	}

}
