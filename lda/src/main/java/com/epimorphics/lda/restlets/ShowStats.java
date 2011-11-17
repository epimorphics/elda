/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.restlets;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.epimorphics.util.Util;

@Path( "/control/show-stats") public class ShowStats {
	
	@GET @Produces("text/html") public synchronized Response showStats() {
		StringBuilder sb = new StringBuilder();
		sb.append( "<h1>Elda: endpoint timings.</h2>" );
	//
		if (requestCount == 0) {
			sb.append( "<i>No requests have been processed yet.</i>" );
		} else {
			long totalSparqlTime = totalSelectionTime.total + totalViewTime.total;
			sb.append( "<table>\n" );
			sb.append( "<thead><tr><th>label</th><th>value</th><th>min</th><th>mean</th><th>max</th></tr></thead>" );
			
			countRow( sb, "satisfied requests", requestCount );
			countRow( sb, "selection cache hits", totalSelectCacheHits );
			countRow( sb, "view cache hits", totalViewCacheHits );
			timeRow( sb, "total elapsed time", totalTime );
			timeRow( sb, "total item selection time", totalSelectionTime );
			timeRow( sb, "total view generation time", totalViewTime );
			timeRow( sb, "total SPARQL time", totalSparqlTime );
			timeRow( sb, "total rendering time", totalRenderTime );
			timeRow( sb, "remaining Elda time", totalTime - totalSparqlTime - totalRenderTime.total );
			sb.append( "</table>\n" );
		}
	//
		sb.append( "<h2>rendering times by type</h2>\n" );
		sb.append( "<table>\n" );
		sb.append( "<thead><tr><th>label</th><th>value</th><th>min</th><th>mean</th><th>max</th></tr></thead>\n" );
		for (Map.Entry<String, Interval> e: formatDurations.entrySet()) {
			timeRow( sb, e.getKey(), e.getValue() );
		}
		sb.append( "</table>\n" );
	//
		String html = Util.withBody( "Elda timings", sb.toString() );
		return RouterRestlet.returnAs( html, "text/html" );
	}

	private void timeRow(StringBuilder sb, String label, Interval i ) {
		sb
			.append( "<tr><td>" ).append( label ).append( ":</td>" )
			.append( "<td align='right'>" ).append( i.total ).append( " ms</td>" )
			.append( "<td align='right'>" ).append( i.min ).append( " ms</td>" )
			.append( "<td align='right'>" ).append( i.total / requestCount ).append( " ms</td>" )
			.append( "<td align='right'>" ).append( i.max ).append( " ms</td>" )
			.append( "</tr>\n" )
			;
	}

	private void countRow( StringBuilder sb, String label, long count ) {
		sb.append( "<tr><td>" ).append( label ).append( ":</td><td>" ).append( count ).append( "</td></tr>\n" );
	}

	private void timeRow( StringBuilder sb, String label, long duration ) {
		sb.append( "<tr><td>" ).append( label ).append( ":</td><td align='right'>" ).append( duration ).append( " ms </td></tr>\n" );
	}

	static long matchFailures = 0;
	static long requestCount = 0;
	static long totalTime = 0;
	
	static class Interval {
		long min = Long.MAX_VALUE, total = 0, max = Long.MIN_VALUE;
		
		void update( long duration ) {
			update( duration, false );
		}		
		
		void update( long duration, boolean suppressMin ) {
			total += duration;
			if (duration > max) max = duration;
			if (duration < min && suppressMin == false) min = duration;
		}
	}
	
	static Map<String, Interval> formatDurations = new HashMap<String, Interval>();
	
	static void updateFormatDurations( String format, long duration ) {
		Interval i = formatDurations.get( format );
		if (i == null) formatDurations.put( format, i = new Interval() );
		i.update( duration );
	}
	
	static Interval totalSelectionTime = new Interval();
	static Interval totalViewTime = new Interval();
	static Interval totalRenderTime = new Interval();
	
	static long totalViewCacheHits = 0;
	static long totalSelectCacheHits = 0;
	
	public static synchronized void endpointNoMatch() {
		matchFailures += 1;
	}

	public static synchronized void accumulate( Times t ) {
		requestCount += 1;
		totalTime += t.totalTime();
		totalSelectionTime.update( t.selectionDuration(), t.usedSelectionCache );
		totalViewTime.update( t.viewDuration(), t.usedViewCache );
		totalRenderTime.update( t.renderDuration() );
		updateFormatDurations( t.renderFormat, t.renderDuration() );
		if (t.usedViewCache) totalViewCacheHits += 1;
		if (t.usedSelectionCache) totalSelectCacheHits += 1;
	}
}
