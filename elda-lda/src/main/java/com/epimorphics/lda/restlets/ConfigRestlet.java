/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.restlets;

import com.epimorphics.lda.configs.LoadedConfigs;
import com.epimorphics.lda.support.pageComposition.ComposeConfigDisplay;
import com.epimorphics.util.Util;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;

@Path("/api-config")
public class ConfigRestlet {

    @GET
    @Produces("text/html")
    public Response generateConfigPage(
            @PathParam("path") String pathstub,
            @Context ServletContext sc,
            @Context UriInfo ui
    ) {
        URI base = ui.getBaseUri();
        /* result ignored */
        RouterRestlet.getRouterFor(sc);
        //
        String page = new ComposeConfigDisplay().configPageMentioning(LoadedConfigs.instance, base, pathstub);
        return RouterRestlet.returnAs(null, Util.withBody("API configuration", page), "text/html");
    }

}
