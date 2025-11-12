/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.restlets;

import com.epimorphics.lda.support.pageComposition.ComposeStatsDisplay;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

/**
 * The ShowStats restlet provides access to an HTML rendering of
 * Elda's StatsValues.
 */
@Path("/control/show-stats")
public class ShowStats {

    /**
     * Render the statistics into HTML and respond with it.
     */
    @GET
    @Produces("text/html")
    public synchronized Response showStats() {
        return RouterRestlet.returnAs(RouterRestlet.NO_EXPIRY, new ComposeStatsDisplay().renderStatsPage(), "text/html");
    }
}
