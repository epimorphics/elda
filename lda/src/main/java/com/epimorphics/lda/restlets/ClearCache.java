package com.epimorphics.lda.restlets;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.epimorphics.lda.cache.Cache;

@Path( "/control/clear-cache") public class ClearCache 
	{
	@GET @Produces("text/plain") public Response clearCache() 
		{
		Cache.clearAll();
		return RouterRestlet.returnAs( "caches cleared", "text/plain" );
		}
	}
