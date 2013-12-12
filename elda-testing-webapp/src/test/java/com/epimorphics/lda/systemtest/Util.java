/*
	See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
	for the licence for this software.
	
	(c) Copyright 2011 Epimorphics Limited
	$Id$
*/
package com.epimorphics.lda.systemtest;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class Util {

	interface CheckContent {
		boolean check( String s );
		String failMessage();
	}

	public static String stringFrom( InputStream s ) throws IOException {
		final char[] buffer = new char[0x10000];
		StringBuilder out = new StringBuilder();
		Reader in = new InputStreamReader( s, "UTF-8" );
		int read;
		do {
		  read = in.read( buffer, 0, buffer.length );
		  if (read > 0) out.append( buffer, 0, read );
		} while (read >= 0);
		return out.toString();
	}

	static Util.CheckContent ignore = new Util.CheckContent() {
	
		@Override public boolean check(String s) {
			return true;
		}
	
		@Override public String failMessage() {
			return "cannot fail";
		}
		
	};

}
