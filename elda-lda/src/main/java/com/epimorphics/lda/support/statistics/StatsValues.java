/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.support.statistics;

import java.util.HashMap;
import java.util.Map;

import com.epimorphics.lda.support.Times;

public class StatsValues {

	public static long failedMatchCount = 0;
	public static long requestCount = 0;
	public static long failedRequestCount = 0;
	public static long totalViewCacheHits = 0;
	public static long totalSelectCacheHits = 0;
	public static long totalTime = 0;
	
	public static Interval totalSelectionTime = new Interval();
	public static Interval totalViewTime = new Interval();
	public static Interval totalRenderTime = new Interval();
	public static Interval totalRenderSize = new Interval();
	public static Interval totalSelectQuerySize = new Interval();
	public static Interval totalViewQuerySize = new Interval();
	public static Interval totalStylesheetCompileTime = new Interval();
	
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
		totalStylesheetCompileTime.update( t.stylesheetCompileDuration() );
		StatsValues.updateFormatDurations( t.renderFormat, t.renderDuration() );
		StatsValues.updateFormatSizes( t.renderFormat, t.renderSize() );
		if (t.usedViewCache) totalViewCacheHits += 1;
		if (t.usedSelectionCache) totalSelectCacheHits += 1;
	}
	
	public static void updateFormatDependentValues(Map<String, Interval> x, String format, long duration) {
		Interval i = x.get( format );
		if (i == null) x.put( format, i = new Interval() );
		i.count += 1;
		i.update( duration );
	}
	
	public static Map<String, Interval> formatDurations = new HashMap<String, Interval>();
	public static Map<String, Interval> formatSizes = new HashMap<String, Interval>();
	
	public static void updateFormatDurations( String format, long duration ) {
		updateFormatDependentValues( formatDurations, format, duration );
	}
	
	public static void updateFormatSizes( String format, long size ) {
		updateFormatDependentValues( formatSizes, format, size );
	}

}
