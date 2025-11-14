/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.restlets;

import com.epimorphics.lda.cache.Cache;
import com.epimorphics.util.URIUtils;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

@Path("/control/reset-counts")
public class ResetCacheCounts {

    @POST
    @Produces("text/html")
    public Response resetCache() {
        Cache.Registry.resetCounts();
        return Response.seeOther(URIUtils.newURI("/control/show-cache")).build();
    }

}
