/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.util;

import java.io.*;

import com.hp.hpl.jena.shared.WrappedException;

/**
    Some utilities for handling UTF8 streams.
    
    @author chris
*/
public class StreamUtils {

	/**
	    Answer an OutputStreamWriter that encodes in UTF-8.
	*/
	public static OutputStreamWriter asUTF8( OutputStream os ) {
		try { 
			return new OutputStreamWriter( os, "UTF-8" );
		} catch (UnsupportedEncodingException e) {
			throw new WrappedException( e );
		}
	}

	/**
	    Flush an OutputStream. Rethrow IO exceptions, wrapped.
	*/
	public static void flush(OutputStream os) {
		try { os.flush(); } 
		catch (IOException e) { throw new WrappedException( e  ); }
	}
	
}