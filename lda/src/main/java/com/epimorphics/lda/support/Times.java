/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
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
	public boolean usedSelectionCache;
	public boolean usedViewCache;

	public Times( String forPath ) {
		this.forPath = forPath;
		this.startTime = System.currentTimeMillis();
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
		return  endTime - startTime;
	}

	public long selectionDuration() {
		return selectionDuration;
	}

	public long viewDuration() {
		return viewDuration;
	}
}