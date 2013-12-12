/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.support.pageComposition;

import java.util.Map;

import com.epimorphics.lda.support.statistics.Interval;
import com.epimorphics.lda.support.statistics.StatsValues;
import com.epimorphics.util.Util;

public class ComposeStatsDisplay {	
	
	public String renderStatsPage() {
		StringBuilder sb = new StringBuilder();
		sb.append( "<h1>Elda: endpoint statistics.</h2>" );
	//
		if (StatsValues.requestCount == 0) {
			sb.append( "<i>No requests have been processed yet.</i>" );
		} else {
			long totalSparqlTime = StatsValues.totalSelectionTime.total + StatsValues.totalViewTime.total;
			sb.append( "<h2>requests and hits</h2>\n" );
			sb.append( "<table>\n" );
			sb.append( "<thead><tr><th>label</th><th>value</th></tr></thead>" );
			countRow( sb, "total requests", StatsValues.requestCount );
			countRow( sb, "failed requests", StatsValues.failedRequestCount );
			countRow( sb, "selection cache hits", StatsValues.totalSelectCacheHits );
			countRow( sb, "view cache hits", StatsValues.totalViewCacheHits );
			sb.append( "</table>\n" );
		//
			sb.append( "<h2>query and rendering timings.</h2>\n" );
			sb.append( "<table>\n" );			
			sb.append( "<thead><tr><th>label</th><th>value</th><th>min</th><th>mean</th><th>max</th></tr></thead>" );
			timeRow( sb, "total elapsed time", StatsValues.totalTime );
			timeRow( sb, "total item selection time", StatsValues.totalSelectionTime );
			timeRow( sb, "total view generation time", StatsValues.totalViewTime );
			timeRow( sb, "total stylesheet compile time", StatsValues.totalStylesheetCompileTime );
			timeRow( sb, "total SPARQL time", totalSparqlTime );
			timeRow( sb, "total rendering time", StatsValues.totalRenderTime );
			timeRow( sb, "remaining Elda time", StatsValues.totalTime - totalSparqlTime - StatsValues.totalRenderTime.total );
			sb.append( "</table>\n" );
		//
			sb.append( "<h2>query and rendering sizes.</h2>\n" );
			sb.append( "<table>\n" );
			sb.append( "<thead><tr><th>label</th><th>value</th><th>min</th><th>mean</th><th>max</th></tr></thead>" );
			sizeRow( sb, "total rendered size", StatsValues.totalRenderSize );
			sizeRow( sb, "total select query size", StatsValues.totalSelectQuerySize );
			sizeRow( sb, "total view query size", StatsValues.totalViewQuerySize );
			sb.append( "</table>\n" );
		}
	//
		sb.append( "<h2>rendering times by type</h2>\n" );
		sb.append( "<table>\n" );
		sb.append( "<thead><tr><th>format</th><th>total</th><th>min</th><th>mean</th><th>max</th></tr></thead>\n" );
		for (Map.Entry<String, Interval> e: StatsValues.formatDurations.entrySet()) {
			timeRow( sb, e.getKey(), e.getValue() );
		}
		sb.append( "</table>\n" );
	//
		sb.append( "<h2>rendering sizes by type</h2>\n" );
		sb.append( "<table>\n" );
		sb.append( "<thead><tr><th>format</th><th>total</th><th>min</th><th>mean</th><th>max</th></tr></thead>\n" );
		for (Map.Entry<String, Interval> e: StatsValues.formatSizes.entrySet()) {
			sizeRow( sb, e.getKey(), e.getValue() );
		}
		sb.append( "</table>\n" );
	//
		String html = Util.withBody( "Elda statistics", sb.toString() );
		return html;
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
}
