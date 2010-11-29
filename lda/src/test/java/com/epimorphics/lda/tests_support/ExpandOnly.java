/*
    See lda-top/LICENCE for the licence for this software.
    (c) copyright Epimorphics Limited 2010
    $Id$
*/

package com.epimorphics.lda.tests_support;

import java.util.HashMap;
import java.util.Map;

/**
    An implementation of ShortnameService that only implements expand()
    and which is initialised from a string of short=full pairs.
 
 	@author chris
*/
public class ExpandOnly extends ShortnameFake 
	{
	private final Map<String, String> map = new HashMap<String, String>();

	/**
	    The string is a bunch of semicolon-separated short=full definitions.
	*/
	public ExpandOnly( String expansions )
		{
		if (expansions.length() > 0)
			for (String e: expansions.split(" *; *") )
				{
				String [] parts = e.split(" *= *", 2 );
				map.put( parts[0], parts[1] );
				}
		}
	
	@Override public String expand( String key ) 
		{
		String result = map.get( key );
		if (result == null) 
			{
			String star = map.get( "*" );
			if (star == null)
				throw new IllegalArgumentException( "missing key: " + key );
			else
				return star.replaceAll( "\\*", key );
			}
		else
			return result;
		}
	}