/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Set;

import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.exceptions.EldaException;
import com.hp.hpl.jena.shared.WrappedException;

public class URIUtils {

    protected static Logger log = LoggerFactory.getLogger(URIUtils.class);
    
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
//		System.err.println( ">> replaceQueryParam: " + ru );
//		System.err.println( ">>  key = " + key + ", values = " + Arrays.asList( values ) );
		URI result = UriBuilder.fromUri( ru ).replaceQueryParam( key,  (Object []) values ).build();
//		System.err.println( ">> replaceQueryParam returns " + result );
		return result;
//		try {
//			String q = ru.getQuery();
//			String newQuery = q == null ? "" : URIUtils.strip( q, key );
//			String and = newQuery.isEmpty() ? "" : "&";
//			for (String value: values) {
//				newQuery = newQuery + and + key + "=" + URIUtils.quoteForValue(value);
//				and = "&";
//			}
//			return new URI
//				(
//				ru.getScheme(), 
//				ru.getAuthority(), 
//				ru.getPath(),
//				(newQuery.isEmpty() ? null : newQuery), 
//				ru.getFragment() 
//				);
//		} catch (URISyntaxException e) {			
//			throw new EldaException( "created a broken URI", "", EldaException.SERVER_ERROR, e );
//		}
	}

//	public static String strip( String query, String key ) {
//		String result = 
//			("&" + query)
//			.replaceAll( "[&]" + key + "=[^&]*", "" )
//			.replaceFirst( "^[&]", "" )
//			;
//		return result;
//	}
//
//	public static String quoteForValue( String value ) {
//		return value.replace( "&", "%26" );
//	}

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

	// TODO issue with URI not handling illegally unescaped [] when unescaping
	public static URI changeFormatSuffix(URI reqURI, Set<String> knownFormats, String formatName)  {
//		try {
//			URI result = new URI
//				( reqURI.getScheme()
//				, reqURI.getAuthority()
//				, replaceSuffix( knownFormats, formatName, reqURI.getPath() )
//				, reqURI.getQuery()
//				, reqURI.getFragment() 
//				);
//			return result;
//		} catch  (URISyntaxException e) {
//			throw new EldaException( "created a broken URI", "", EldaException.SERVER_ERROR, e );
//		}
		String newPath = replaceSuffix( knownFormats, formatName, reqURI.getPath() );
		URI result = UriBuilder.fromUri( reqURI ).replacePath( newPath ).build();
//		System.err.println( ">> changeFormatSuffix returns " + result );
		return result;
	}
	
	public static URI noLeadingSlash( String path ) {
		String fixedPath = path.startsWith("/") ? path.substring(1) : path;
		try {
			return new URI( null, null, fixedPath, null );
		} catch (URISyntaxException e) {
			throw new EldaException( e.getMessage() );
		}
	}

	public static URI resolveAgainstBase( URI requestUri, URI baseAsURI, String uiPath ) {
		
//		System.err.println( ">> resolveAgainstBase: " );
//		System.err.println( ">>   requestURI: " + requestUri );
//		System.err.println( ">>   baseAsURI:  " + baseAsURI );
//		System.err.println( ">>   uiPath:     " + uiPath );
		
		String baseAsString = baseAsURI.toString();
		if (!baseAsString.endsWith("/")) 
			baseAsURI = newURI( baseAsString + "/" );
				
		URI mid = baseAsURI.isAbsolute() ? baseAsURI : requestUri.resolve( baseAsURI );
				
		URI resolved = 
			mid
			.resolve( noLeadingSlash( uiPath ) )
			;
		
//		System.err.println( ">>   => resolved: " + resolved );
		
		URI built = UriBuilder.fromUri( resolved ).replaceQuery( requestUri.getRawQuery() ).build();
		try {
			URI uri = new URI(
			resolved.getScheme(),
			resolved.getUserInfo(),
			resolved.getHost(),
			resolved.getPort(),
			resolved.getPath(),
			requestUri.getQuery(),
			resolved.getFragment()
			);
			if (!built.equals(uri)) 
				log.warn( "resolveAgainstBase:" 
					+ "\n  old code delivers "  + uri 
					+ "\n  but new code delivers: " + built 
				);
			return built;
		} catch (URISyntaxException e) {
			throw new WrappedException( e );
		}
	}

	public static URI forceDecode(URI u) {
//		System.err.println( ">> forceDecode: " + u );
//		try {
//			return new URI
//				( 
//				u.getScheme(),
//				u.getUserInfo(),
//				u.getHost(),
//				u.getPort(),
//				u.getPath(),
//				u.getQuery(),
//				u.getFragment()
//				);
//		} catch (URISyntaxException e) {
//			throw new WrappedException( e );
//		}
		return u;
	}

}
