/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.support;

import com.epimorphics.lda.core.MultiMap;

public class MultiValuedMapSupport 
	{

	public static MultiMap<String, String> parseQueryString( String queryString ) 
		{
		MultiMap<String, String> result = new MultiMap<String, String>();
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
