package com.epimorphics.lda.exceptions;

import com.epimorphics.lda.sources.Source;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;

public class EldaException extends RuntimeException {

	private static final long serialVersionUID = -189771483274502564L;

	public final int code;
	public final String moreMessage;
	
	public EldaException( String message, String moreMessage, int code, Throwable cause ) {
		super( message, cause );
		this.code = code;
		this.moreMessage = moreMessage;
	}

	public EldaException( String message, String moreMessage, int code ) {
		this( message, moreMessage, code, null );
	}
	
	public EldaException( String message ) {
		this( message, "", SERVER_ERROR, null );
	}
	
	public static final int BAD_REQUEST = 400;
	public static final int NOT_FOUND = 404;
	
	public static final int SERVER_ERROR = 500;
	
	public static void NotFound( String kind, String name ) {
		throw new EldaException( kind + " not found: " + name, null, BAD_REQUEST, null );
	}

	public static void NoItemFound() {
		throw new EldaException( "no items matching that request were found.", "", NOT_FOUND, null );
	}

	/**
	    Throw a BAD REQUEST exception with the given message.
	*/
	public static void BadRequest( String message ) {
		throw new EldaException( message, "", BAD_REQUEST );
	}
	
	public static void NoDeploymentURIFor(String name) {
		throw new EldaException( "No deployment uri for Endpoint " + name, "", BAD_REQUEST, null );
	}

	public static void ARQ_Exception( Source source, QueryExceptionHTTP ie ) {
		throw new EldaException( "Problem running query for " + source + ": " + ie.getMessage(), "", SERVER_ERROR, ie);
	}

	public static void Broken( String message ) {
		throw new EldaException( message );
	}

	public static void BadSpecification( String message ) {
		throw new EldaException( "bad specification: " + message );
	}

	public static void BadBooleanParameter( String param, String val) {
		 throw new EldaException( "value of " + param + " must be true or false, not " + val, "", BAD_REQUEST, null );		
	}

	public static void EmptyTDB(String name) {
		throw new EldaException( "TDB endpoint " + name + " is empty -- surely an error." );
	}

	public static void NullParameter(String p) {
		throw new EldaException( "value for parameter " + p + " is null" );
	}
}
