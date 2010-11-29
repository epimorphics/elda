/*
    See lda-top/LICENCE for the licence for this software.
    (c) copyright Epimorphics Limited 2010
    $Id$
*/

package com.epimorphics.lda.support;

import javax.ws.rs.core.MultivaluedMap;

import com.sun.jersey.core.util.MultivaluedMapImpl;

public class MultiValuedMapSupport 
	{

	public static MultivaluedMap<String, String> parseQueryString( String queryString ) 
		{
		MultivaluedMap<String, String> result = new MultivaluedMapImpl();
		String[] pairs = queryString.split( "&" );
	    for (int i = 0; i < pairs.length; i++) 
	    	{
	        if (pairs[i].isEmpty()) break;
	        String[] pair = pairs[i].split( "=" );
	        result.add( pair[0], pair[1] );
	    	}
	    return result;
		}
	}
