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

import com.epimorphics.lda.support.Times;
import com.epimorphics.util.Util;

/**
	The ShowStats restlet stores some statistics about Elda's
	query times and cache hits, and renders it on demand into
	HTML.	
*/
@Path( "/control/show-stats") public class ShowStats {
	
	/**
	    Render the statistics into HTML and respond with it.
	*/
	@GET @Produces("text/html") public synchronized Response showStats() {
		StringBuilder sb = new StringBuilder();
		sb.append( "<h1>Elda: endpoint statistics.</h2>" );
	//
		if (requestCount == 0) {
			sb.append( "<i>No requests have been processed yet.</i>" );
		} else {
			long totalSparqlTime = totalSelectionTime.total + totalViewTime.total;
			sb.append( "<h2>requests and hits</h2>\n" );
			sb.append( "<table>\n" );
			sb.append( "<thead><tr><th>label</th><th>value</th></tr></thead>" );
			countRow( sb, "total requests", requestCount );
			countRow( sb, "failed requests", failedRequestCount );
			countRow( sb, "selection cache hits", totalSelectCacheHits );
			countRow( sb, "view cache hits", totalViewCacheHits );
			sb.append( "</table>\n" );
		//
			sb.append( "<h2>query and rendering timings.</h2>\n" );
			sb.append( "<table>\n" );			
			sb.append( "<thead><tr><th>label</th><th>value</th><th>min</th><th>mean</th><th>max</th></tr></thead>" );
			timeRow( sb, "total elapsed time", totalTime );
			timeRow( sb, "total item selection time", totalSelectionTime );
			timeRow( sb, "total view generation time", totalViewTime );
			timeRow( sb, "total SPARQL time", totalSparqlTime );
			timeRow( sb, "total rendering time", totalRenderTime );
			timeRow( sb, "remaining Elda time", totalTime - totalSparqlTime - totalRenderTime.total );
			sb.append( "</table>\n" );
		//
			sb.append( "<h2>query and rendering sizes.</h2>\n" );
			sb.append( "<table>\n" );
			sb.append( "<thead><tr><th>label</th><th>value</th><th>min</th><th>mean</th><th>max</th></tr></thead>" );
			sizeRow( sb, "total rendered size", totalRenderSize );
			sizeRow( sb, "total select query size", totalSelectQuerySize );
			sizeRow( sb, "total view query size", totalViewQuerySize );
			sb.append( "</table>\n" );
		}
	//
		sb.append( "<h2>rendering times by type</h2>\n" );
		sb.append( "<table>\n" );
		sb.append( "<thead><tr><th>format</th><th>total</th><th>min</th><th>mean</th><th>max</th></tr></thead>\n" );
		for (Map.Entry<String, Interval> e: formatDurations.entrySet()) {
			timeRow( sb, e.getKey(), e.getValue() );
		}
		sb.append( "</table>\n" );
	//
		sb.append( "<h2>rendering sizes by type</h2>\n" );
		sb.append( "<table>\n" );
		sb.append( "<thead><tr><th>format</th><th>total</th><th>min</th><th>mean</th><th>max</th></tr></thead>\n" );
		for (Map.Entry<String, Interval> e: formatSizes.entrySet()) {
			sizeRow( sb, e.getKey(), e.getValue() );
		}
		sb.append( "</table>\n" );
	//
		String html = Util.withBody( "Elda statistics", sb.toString() );
		return RouterRestlet.returnAs( html, "text/html" );
	}

	private void sizeRow(StringBuilder sb, String label, Interval i ) {
		sb
			.append( "<tr><td>" ).append( label ).append( ":</td>" )
			.append( "<td align='right'>" ).append( kb(i.total) ).append( "</td>" )
			.append( "<td align='right'>" ).append( kb(i.min) ).append( "</td>" )
			.append( "<td align='right'>" ).append( kb(i.mean()) ).append( "</td>" )
			.append( "<td align='right'>" ).append( kb(i.max) ).append( "</td>" )
			.append( "</tr>\n" )
			;
	}

	private void timeRow(StringBuilder sb, String label, Interval i ) {
		sb
			.append( "<tr><td>" ).append( label ).append( ":</td>" )
			.append( "<td align='right'>" ).append( ms(i.total) ).append( "</td>" )
			.append( "<td align='right'>" ).append( ms(i.min) ).append( "</td>" )
			.append( "<td align='right'>" ).append( ms(i.mean()) ).append( "</td>" )
			.append( "<td align='right'>" ).append( ms(i.max) ).append( "</td>" )
			.append( "</tr>\n" )
			;
	}

	private String kb(long b) {
		if (b < 1024) return Long.toString(b) + " b";
		return Long.toString((b + 512)/1024) + " kb";
	}

	private String ms(long ms) {
		if (ms < 1000) return Long.toString(ms) + " ms";
		return Double.toString( ms / 1000.0 ) + " s";
	}

	private void countRow( StringBuilder sb, String label, long count ) {
		sb.append( "<tr><td>" ).append( label ).append( ":</td><td>" ).append( count ).append( "</td></tr>\n" );
	}

	private void timeRow( StringBuilder sb, String label, long duration ) {
		sb.append( "<tr><td>" ).append( label ).append( ":</td><td align='right'>" ).append( ms(duration) ).append( "</td></tr>\n" );
	}

	static class Interval {
		long min = Long.MAX_VALUE, total = 0, max = Long.MIN_VALUE, count = 0;
		
		void update( long duration ) {
			update( duration, false );
		}		
		
		void update( long duration, boolean suppressMin ) {
			total += duration;
			count += 1;
			if (duration > max) max = duration;
			if (duration < min && suppressMin == false) min = duration;
		}
		
		long mean() {
			return count == 0 ? -1 : total/count;
		}
	}
	
	static Map<String, Interval> formatDurations = new HashMap<String, Interval>();
	static Map<String, Interval> formatSizes = new HashMap<String, Interval>();
	
	static void updateFormatDurations( String format, long duration ) {
		updateFormatDependentValues( formatDurations, format, duration );
	}
	
	static void updateFormatSizes( String format, long size ) {
		updateFormatDependentValues( formatSizes, format, size );
	}

	private static void updateFormatDependentValues(Map<String, Interval> x, String format, long duration) {
		Interval i = x.get( format );
		if (i == null) x.put( format, i = new Interval() );
		i.count += 1;
		i.update( duration );
	}
	
	static long failedMatchCount = 0;
	static long requestCount = 0;
	static long failedRequestCount = 0;
	static long totalViewCacheHits = 0;
	static long totalSelectCacheHits = 0;
	static long totalTime = 0;
	
	static Interval totalSelectionTime = new Interval();
	static Interval totalViewTime = new Interval();
	static Interval totalRenderTime = new Interval();
	static Interval totalRenderSize = new Interval();
	static Interval totalSelectQuerySize = new Interval();
	static Interval totalViewQuerySize = new Interval();
		
	/**
	    Record an occurence of a non-matched URI.
	*/
	public static synchronized void endpointNoMatch() {
		failedMatchCount += 1;
	}
	
	public static synchronized void endpointException() {
		failedRequestCount += 1;
	}

	/**
	    Accumulate more statistics information from the given Times
	    object.
	*/
	public static synchronized void accumulate( Times t ) {
		requestCount += 1;
		totalTime += t.totalTime();
		totalSelectionTime.update( t.selectionDuration(), t.usedSelectionCache );
		totalViewTime.update( t.viewDuration(), t.usedViewCache );
		totalRenderTime.update( t.renderDuration() );
		totalRenderSize.update( t.renderSize() );
		totalSelectQuerySize.update( t.selectQuerySize() );
		totalViewQuerySize.update( t.viewQuerySize() );
		updateFormatDurations( t.renderFormat, t.renderDuration() );
		updateFormatSizes( t.renderFormat, t.renderSize() );
		if (t.usedViewCache) totalViewCacheHits += 1;
		if (t.usedSelectionCache) totalSelectCacheHits += 1;
	}
}
