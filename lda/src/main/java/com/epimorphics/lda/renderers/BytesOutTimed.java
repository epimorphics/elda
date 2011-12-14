/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.renderers;

import java.io.OutputStream;

import com.epimorphics.lda.renderers.Renderer.BytesOut;
import com.epimorphics.lda.support.Times;
import com.epimorphics.util.CountStream;
import com.epimorphics.util.StreamUtils;

/**
 	A BytesOutTimed is a BytesOut that counts the bytes written and
 	the time taken to a supplied Times.
 
 	@author chris
*/
public abstract class BytesOutTimed implements BytesOut {

	@Override public final void writeAll( Times t, OutputStream os ) {
		try {
			long base = System.currentTimeMillis();
			CountStream cos = new CountStream( os );
			writeAll( cos );
			StreamUtils.flush( os );
	        t.setRenderedSize( cos.size() );
	        t.setRenderDuration( System.currentTimeMillis() - base, getFormat() );
		} catch (Throwable e) {
			throw new RuntimeException( "exception during streaming: " + e.getMessage() );
		}
	}
	
	/**
	    Subclass implements to provide writing functionality that
	    this class wraps for statistics.
	*/
	protected abstract void writeAll( OutputStream os );
	
	/**
	    The format name of the stream (so it can be reported in the timings).
	*/
	protected abstract String getFormat();
}