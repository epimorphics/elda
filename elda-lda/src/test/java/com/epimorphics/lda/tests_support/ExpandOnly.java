/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.tests_support;

import java.util.HashMap;
import java.util.Map;

import com.epimorphics.lda.shortnames.StandardShortnameService;
import com.hp.hpl.jena.rdf.model.Model;

/**
    An implementation of ShortnameService that implements expand()
    directly from a string of short=full pairs.
 
 	@author chris
*/
public class ExpandOnly extends StandardShortnameService 
	{
	private final Map<String, String> map = new HashMap<String, String>();

	/**
	    The string is a bunch of semicolon-separated short=full definitions.
	*/
	public ExpandOnly( Model config, String expansions )
		{
		super( config );
		define( expansions );
		}
	
	public ExpandOnly( String expansions ) 
		{ define( expansions ); }
	
	public ExpandOnly() 
		{}

	private void define(String expansions) {
		if (expansions.length() > 0)
			for (String e: expansions.split(" *; *") )
				{
				String [] parts = e.split(" *= *", 2 );
				map.put( parts[0], parts[1] );
				context.recordPreferredName( parts[0], parts[1] );
				}
	}
	
//	public void define( String shortName, String fullURI ) {
//		map.put( shortName, fullURI );
//	}
	
	@Override public String expand( String key ) 
		{
		String result = map.get( key );
		if (result == null) 
			{
			String star = map.get( "*" );
			return star == null ? super.expand( key ) : star.replaceAll( "\\*", key );
			}
		else
			return result;
		}
	}