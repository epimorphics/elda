package com.epimorphics.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hp.hpl.jena.rdf.model.impl.Util;

public class NameUtils {

	/**
	    Answer true iff <code>proposed</code> matches the permitted
	    syntax of short names (which amount to the intersection of Javascript
	    names, to make native JSON handling convenient, and XML element names,
	    to make legal XML).
	*/
	public static boolean isLegalShortname(String l) {
		return labelPattern.matcher(l).matches();
	}
	
    public static Pattern labelPattern = Pattern.compile("[_a-zA-Z][0-9a-zA-Z_]*");

	public static final Pattern prefixSyntax = Pattern.compile( "^[A-Za-z][a-zA-Z0-9]*_" );
	
	public static String nameSpace( String uri ) {
		return uri.substring( 0, Util.splitNamespace( uri ) );
	}

	public static String localName( String uri ) {
		return uri.substring( Util.splitNamespace( uri ) );
	}

	/**
	    Answer N if <code>proposed</code> starts with a legal simple prefix 
	    name and then _, where N is the index of the first character past the
	    _, or -1 if it does not so start.
	*/
	public static int prefixEndsAt( String proposed ) {
		Matcher m = NameUtils.prefixSyntax.matcher( proposed ); 
		return m.find() ? m.end() : -1;
	}


}
