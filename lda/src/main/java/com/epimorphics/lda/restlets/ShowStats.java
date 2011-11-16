/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.restlets;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.epimorphics.util.Util;

@Path( "/control/show-stats") public class ShowStats {
	
	@GET @Produces("text/html") public Response showStats() {
		StringBuilder sb = new StringBuilder();
		sb.append( "<h1>Elda: endpoint timings.</h2>" );
		sb.append( "<div>failures to match: " ).append( matchFailures ).append( "</div>\n" );
		sb.append( "<div>successful matches: " ).append( matchFound ).append( "</div>\n" );
		sb.append( "<div>total elapsed time: " ).append( totalTime ).append( "ms </div>\n" );
		if (matchFound > 0)
			sb.append( "<div>mean time per success: " ).append( totalTime / matchFound ).append( " ms</div>\n" );
		String html = Util.withBody( "Metadata", sb.toString() );
		return RouterRestlet.returnAs( html, "text/html" );
	}

	static int matchFailures = 0;
	static int matchFound = 0;
	static long totalTime = 0;
	
	public static synchronized void endpointNoMatch() {
		matchFailures += 1;
	}

	public static synchronized void endpointTookMs( long time ) {
		matchFound += 1;
		totalTime += time;
	}
}
