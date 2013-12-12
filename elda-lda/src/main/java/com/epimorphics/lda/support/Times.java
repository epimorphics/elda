/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.support;

/**
    A Times object holds information about the times (and mayhap other
    details) associated with an Elda query.
*/
public class Times {
	
	long selectionDuration;
	long viewDuration;
	long startTime;
	long renderDuration;
	public String renderFormat;
	String forPath;
	long endTime;
	long selectQuerySize;
	long viewQuerySize;
	long renderSize;
	long stylesheetCompileTime;
	public boolean usedSelectionCache;
	public boolean usedViewCache;	

	public Times( String forPath ) {
		this.forPath = forPath;
		this.startTime = System.currentTimeMillis();
		this.endTime = this.startTime;
	}
	
	public Times() {
		this( "(none)" );
	}
	
	public void setSelectionDuration( long time ) {
		selectionDuration = time;
	}
	
	public void setViewDuration( long time ) {
		viewDuration = time;
	}

	public Times done() {
		endTime = System.currentTimeMillis();
		return this;
	}

	public void setRenderDuration( long time, String name ) {
		renderDuration = time;
		renderFormat = name;
	}

	public long renderDuration() {
		return renderDuration;
	}
	
	public String renderFormat() {
		return renderFormat;
	}
	
	public void usedSelectionCache() {	
		usedSelectionCache = true;
	}

	public void usedViewCache() {
		usedViewCache = true;
	}

	public long totalTime() {
		return endTime - startTime;
	}

	public long selectionDuration() {
		return selectionDuration;
	}

	public long viewDuration() {
		return viewDuration;
	}
	
	public long stylesheetCompileDuration() {
		return stylesheetCompileTime;
	}
	
	/**
	    Answer the size of the rendered result, in bytes.
	*/
	public long renderSize() {
		return renderSize;
	}

	/**
	    Record the size in bytes of the rendered output.
	*/
	public void setRenderedSize( long renderSize ) {
		this.renderSize = renderSize;
	}
	
	/**
	    Answer the recorded size in bytes of the select query.
	*/
	public long selectQuerySize() { 
		return selectQuerySize; 
	}
	
	/**
	    Answer the recorded size in bytes of all the queries
	    contributing to the view.
	*/
	public long viewQuerySize() { 
		return viewQuerySize; 
	}

	/**
	    Record the size in bytes of a piece of the view query.
	*/
	public void addToViewQuerySize( String fragment ) {
		this.viewQuerySize = fragment.length() * 2;
	}

	/**
	    Record the size in bytes of the select query.
	*/
	public void setSelectQuerySize( String selectQuery ) {
		this.selectQuerySize = selectQuery.length() * 2;
	}

	/**
	    Set the duration of a stylesheet compilation.
	*/
	public void setStylesheetCompileTime( long duration ) {
		stylesheetCompileTime = duration;
	}
}