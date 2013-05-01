package com.epimorphics.lda.exceptions;

import java.util.Set;

public class ReusedShortnameException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public final String shortName;
	public final Set<String> uris;

	public ReusedShortnameException( String shortName, Set<String> uris ) {
		super( message( shortName, uris ));
		this.shortName = shortName;
		this.uris = uris;
	}

	private static String message( String shortName, Set<String> uris ) {
		return "Shortname " + shortName + " is bound to multiple URIs, viz: " + uris;
	}
}
