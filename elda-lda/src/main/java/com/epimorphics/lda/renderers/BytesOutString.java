/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.renderers;

import java.io.*;

import com.epimorphics.lda.renderers.Renderer.BytesOut;
import com.epimorphics.lda.support.Times;
import com.hp.hpl.jena.shared.WrappedException;

/**
    A BytesOutString writes string content, UTF-8 encoded, to
    the supplied stream.
 
 	@author chris
*/
public class BytesOutString implements BytesOut {

	final String content;
	
	public BytesOutString( String content ) {
		this.content = content;
	}

	@Override public void writeAll(Times t, OutputStream os) {
		try {
			OutputStream bos = new BufferedOutputStream(os);
			OutputStreamWriter osw = new OutputStreamWriter(bos, "UTF-8" );
			osw.write( content );
			osw.flush();
			osw.close();
		} catch (IOException e) {
			throw new WrappedException( e );
		}
	}
	
}