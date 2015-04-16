/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.restlets;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.epimorphics.lda.cache.Cache;

@Path( "/control/show-cache") public class ShowCache {

	@GET @Produces("text/html") public Response clearCache(@Context UriInfo ui) 
		{
		MultivaluedMap<String, String> params = ui.getQueryParameters();
		
		StringBuilder sb = new StringBuilder();
		sb.append( "<html><head></head><body style='background-color: #ccffcc'>\n" );
		sb.append( "<h1>Elda cache state</h1>\n" );
		if (params.containsKey("warn")) {
			sb.append( "<div style='margin-bottom: 2ex'>" )
			.append( "<b>Warning</b>: cache was cleared using GET, should have been POST." )
			.append( "</div>\n" );
		}
		sb.append( "<form method='POST' action='reset-counts'><input type='SUBMIT' value='RESET COUNTS'></form>\n" );
		sb.append( "<form method='POST' action='clear-cache'><input type='SUBMIT' value='CLEAR CACHE'></form>\n" );
		sb.append( "<h1>cache state</h1>\n" );
		Cache.Registry.showAll( sb );
		sb.append( "</body></html>\n" );
		return RouterRestlet.returnAs( RouterRestlet.NO_EXPIRY, sb.toString(), "text/html" );
		}
}
