/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.query.QueryParameter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

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
		String newPath = replaceSuffix( knownFormats, formatName, reqURI.getPath() );
		URI result = UriBuilder.fromUri( reqURI ).replacePath( newPath ).build();
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
		
		String baseAsString = baseAsURI.toString();
		if (!baseAsString.endsWith("/")) baseAsURI = newURI( baseAsString + "/" );
				
		URI mid = baseAsURI.isAbsolute() ? baseAsURI : requestUri.resolve( baseAsURI );
				
		URI resolved = mid.resolve( noLeadingSlash( uiPath ) );
		
		return  UriBuilder.fromUri( resolved ).replaceQuery( requestUri.getRawQuery() ).build();
	}

	/**
	    Return a Resource in <code>m</code> who's URI is based on 
	    <code>ru</code>, but with the _page parameter removed (for an item 
	    endpoint) or updated to <code>page</code> (for a list endpoint).
	*/
	public static Resource adjustPageParameter( Model m, URI ru, boolean isListEndpoint, int page) {
		URI x = isListEndpoint
			? replaceQueryParam( ru, QueryParameter._PAGE, Integer.toString(page) )
			: replaceQueryParam( ru, QueryParameter._PAGE );
		return m.createResource( x.toString() );
	}

	/**
	    Return a URI based on <code>ru</code> with any _page or _pageSize
	    query settings removed. 
	*/
	public static URI withoutPageParameters( URI ru ) {
    	URI rqp1 = replaceQueryParam( ru, QueryParameter._PAGE );
    	return replaceQueryParam( rqp1, QueryParameter._PAGE_SIZE );
	}

}
