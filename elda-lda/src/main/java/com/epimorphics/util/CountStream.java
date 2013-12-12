/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.util;

import java.io.IOException;
import java.io.OutputStream;

/**
    A CountStream wraps an OutputStream and counts the bytes
    as they fly past.
 
  	@author chris
*/
public class CountStream extends OutputStream {

	long count = 0;
	final OutputStream os;
	
	public CountStream(OutputStream os) {
		this.os = os;
	}

	public long size() {
		return count;
	}

	@Override public void write(int b) throws IOException {
		count += 1;
		os.write( b );
	}
    
	@Override public void write(byte b[], int off, int len) throws IOException {
		count += len;
		os.write( b, off, len );
	}
	
}