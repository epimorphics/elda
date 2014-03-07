/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.restlets;

import java.net.URI;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.epimorphics.lda.specmanager.SpecEntry;
import com.epimorphics.lda.specmanager.SpecManagerFactory;
import com.epimorphics.lda.support.pageComposition.ComposeConfigDisplay;
import com.epimorphics.util.Util;

@Path("/api-config") public class ConfigRestlet {
	    
	@GET @Produces("text/html") public Response generateConfigPage
		( @PathParam("path") String pathstub
		, @Context ServletContext sc
		, @Context UriInfo ui 
		) {
		URI base = ui.getBaseUri();
		/* result ignored */ RouterRestlet.getRouterFor( sc );
	//
		List<SpecEntry> specs = SpecManagerFactory.allSpecs();
		String page = new ComposeConfigDisplay().configPageMentioning( specs, base, pathstub );
		return RouterRestlet.returnAs( null, Util.withBody( "API configuration", page ), "text/html" );
	}
	
}
