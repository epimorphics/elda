package com.epimorphics.lda.shortnames;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
    Some utilities for handling short names.
*/
public class ShortnameUtils {

	static private final Pattern shortSyntax = Pattern.compile( "^[a-zA-Z][a-zA-Z0-9_]*$" );

	public static final Pattern prefixSyntax = Pattern.compile( "^[A-Za-z][a-zA-Z0-9]*_" );
	
	/**
	    Answer true iff <code>proposed</code> matches the permitted
	    syntax of short names (which amount to the intersection of Javascript
	    names, to make native JSON handling convenient, and XML element names,
	    to make legal XML).
	*/
	public static boolean isLegalShortname( String proposed ) {
		return shortSyntax.matcher( proposed ).find(); 
	}
	
	/**
	    Answer N if <code>proposed</code> starts with a legal simple prefix 
	    name and then _, where N is the index of the first character past the
	    _, or -1 if it does not so start.
	*/
	public static int prefixEndsAt( String proposed ) {
		Matcher m = prefixSyntax.matcher( proposed ); 
		return m.find() ? m.end() : -1;
	}
}
