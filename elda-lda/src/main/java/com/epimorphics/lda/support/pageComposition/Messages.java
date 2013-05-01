/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.support.pageComposition;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import com.epimorphics.lda.exceptions.EldaException;

/**
    Support provides support for servlets/restlets that does not
    depend on Jersey.
*/
public class Messages {

	public static String niceMessage( EldaException e ) {
		return
			"<html>"
			+ "\n<head>"
			+ "\n<title>Error " + e.code + "</title>"
			+ "\n</head>"
			+ "\n<body style='background-color: #ffdddd'>"
			+ "\n<h2>Error " + e.code + "</h2>"
			+ "\n<p>" + e.getMessage() + "</p>"
			+ (e.moreMessage == null ? "" : "<p>" + e.moreMessage + "</p>")
			+ "\n</body>"
			+ "\n</html>"
			+ "\n"
			;
	}

	public static String niceMessage( String message, String subText ) {
		return
			"<html>"
			+ "\n<head>"
			+ "\n<title>Error</title>"
			+ "\n</head>"
			+ "\n<body style='background-color: #ffeeee'>"
			+ "\n<h2>" + subText + "</h2>"
			+ "\n<p>" + message + "</p>"
			+ "\n</body>"
			+ "\n</html>"
			+ "\n"
			;
	}

	public static String niceMessage( String message ) {
		return niceMessage( message, "there seems to be a problem." );
	}

	public static String brief( String message ) {
		int nl = message.indexOf( '\n' );
		return nl < 0 ? message : message.substring(0, nl) + "...";
	}

	public static String shorten(String l) {
		int len = l.length();
		if (len < 1000000) return l;
		return l.substring(0, 300) + "\n...\n" + l.substring(len - 700, len - 1);
	}

	public static String shortStackTrace( Throwable e ) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream( bos );
		e.printStackTrace( ps );
		ps.flush();
		return shorten( bos.toString() );
	}

}
