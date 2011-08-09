/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.restlets;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.epimorphics.util.Util;
import com.epimorphics.lda.cache.Cache;

@Path( "/control/reset-counts") public class ResetCacheCounts {
	
	@POST @Produces("text/html") public Response resetCache() {
		Cache.Registry.resetCounts();
		return Response.seeOther( Util.newURI( "/control/show-cache" ) ).build();
	}

}
