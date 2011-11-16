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
		sb.append( "<table>\n" );
		sb.append( "<tr><td>failures to match" ).append( "</td><td>" ).append( matchFailures ).append( "</td></tr>\n" );
		sb.append( "<tr><td>successful matches" ).append( "</td><td>" ).append( matchFound ).append( "</td></tr>\n" );
		sb.append( "<tr><td>total elapsed time" ).append( "</td><td>" ).append( totalTime ).append( "ms </td></tr>\n" );
		sb.append( "<tr><td>total SPARQL time" ).append( "</td><td>" ).append( sparqlTime ).append( "ms </td></tr>\n" );
		sb.append( "<tr><td>elapsed - SPARQL time" ).append( "</td><td>" ).append( totalTime - sparqlTime ).append( "ms </td></tr>\n" );
		if (matchFound > 0) {
			sb.append( "<tr><td>mean time per success" ).append( "</td><td>" ).append( totalTime / matchFound ).append( " ms</td></tr>\n" );
			sb.append( "<tr><td>mean non-SPARQL per success" ).append( "</td><td>" ).append( (totalTime - sparqlTime) / matchFound ).append( " ms</td></tr>\n" );
		}
		sb.append( "</table>\n" );
		String html = Util.withBody( "Metadata", sb.toString() );
		return RouterRestlet.returnAs( html, "text/html" );
	}

	static int matchFailures = 0;
	static int matchFound = 0;
	static long totalTime = 0;
	static long sparqlTime = 0;
	
	public static synchronized void endpointNoMatch() {
		matchFailures += 1;
	}

	public static synchronized void endpointTookMs( long time, long sparql ) {
		matchFound += 1;
		totalTime += time;
		sparqlTime += sparql;
	}
}
