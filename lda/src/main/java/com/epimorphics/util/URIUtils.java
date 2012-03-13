/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import com.epimorphics.lda.exceptions.EldaException;
import com.hp.hpl.jena.shared.WrappedException;

public class URIUtils {

	/**
	    Answer the URI with the given spelling. If there's a syntax error,
	    throw a wrapped exception.
	*/
	public static URI newURI( String u ) 
		{
		try 
			{ return new URI( u ); }
		catch (URISyntaxException e) 
			{ throw new EldaException( "created a broken URI: " + u, "", EldaException.SERVER_ERROR, e ); }
		}

	/**
	    Answer the URI ru with any existing query parameters named <code>key</code>
	    discarded and replaced by key=value1&key=value2 ...
	*/
	public static URI replaceQueryParam(URI ru, String key, String... values) {
		try {
			String q = ru.getQuery();
			String newQuery = q == null ? "" : URIUtils.strip( q, key );
			String and = newQuery.isEmpty() ? "" : "&";
			for (String value: values) {
				newQuery = newQuery + and + key + "=" + URIUtils.quoteForValue(value);
				and = "&";
			}
			return new URI
				(
				ru.getScheme(), 
				ru.getAuthority(), 
				ru.getPath(),
				(newQuery.isEmpty() ? null : newQuery), 
				ru.getFragment() 
				);
		} catch (URISyntaxException e) {			
			throw new EldaException( "created a broken URI", "", EldaException.SERVER_ERROR, e );
		}
	}

	public static String strip( String query, String key ) {
		String result = 
			("&" + query)
			.replaceAll( "[&]" + key + "=[^&]*", "" )
			.replaceFirst( "^[&]", "" )
			;
		return result;
	}

	public static String quoteForValue( String value ) {
		return value.replace( "&", "%??" );
	}

	private static String replaceSuffix( Set<String> knownFormats, String newSuffix, String oldPath ) {
		int dot_pos = oldPath.lastIndexOf( '.' ), slash_pos = oldPath.lastIndexOf( '/' );
		if (dot_pos > -1 && dot_pos > slash_pos) {
			String oldSuffix = oldPath.substring( dot_pos + 1 );
			if (knownFormats.contains( oldSuffix )) 
				return appendSuffix( oldPath.substring(0, dot_pos), newSuffix );
		}
		return appendSuffix( oldPath, newSuffix );
	}

	private static String appendSuffix( String oldPath, String newSuffix ) {
		return newSuffix.equals("") ? oldPath : oldPath + "." + newSuffix;
	}

	public static URI changeFormatSuffix(URI reqURI, Set<String> knownFormats, String formatName)  {
		try {
			return new URI
				( reqURI.getScheme()
				, reqURI.getAuthority()
				, replaceSuffix( knownFormats, formatName, reqURI.getPath() )
				, reqURI.getQuery()
				, reqURI.getFragment() 
				);
		} catch  (URISyntaxException e) {
			throw new EldaException( "created a broken URI", "", EldaException.SERVER_ERROR, e );
		}
	}
	
	private static String noLeadingSlash( String path ) {
		return path.startsWith("/") ? path.substring(1) : path;
	}

	public static URI resolveAgainstBase( URI requestUri, URI baseAsURI, String uiPath ) {
		
		String baseAsString = baseAsURI.toString();
		if (!baseAsString.endsWith("/")) 
			baseAsURI = newURI( baseAsString + "/" );
				
		System.err.println();
		URI mid = baseAsURI.isAbsolute() ? baseAsURI : requestUri.resolve( baseAsURI );
		
		
		URI resolved = 
			mid
			.resolve( noLeadingSlash( uiPath ) )
			;
		
		try {
			return new URI(
			resolved.getScheme(),
			resolved.getUserInfo(),
			resolved.getHost(),
			resolved.getPort(),
			resolved.getPath(),
			requestUri.getQuery(),
			resolved.getFragment()
			);		
		} catch (URISyntaxException e) {
			throw new WrappedException( e );
		}
	}

	public static URI forceDecode(URI u) {
		try {
			return new URI
				( 
				u.getScheme(),
				u.getUserInfo(),
				u.getHost(),
				u.getPort(),
				u.getPath(),
				u.getQuery(),
				u.getFragment()
				);
		} catch (URISyntaxException e) {
			throw new WrappedException( e );
		}
	}

}
