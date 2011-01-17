/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
    Some collection-oriented utilities.
 
 	@author chris
*/
public class CollectionUtils 
	{
	/**
	    Answer a list of all the arguments, in order.
	*/
	public static <T> List<T> list( T ...elements) 
		{ return Arrays.asList( elements );	}

	/**
	    A set with the given element.
	*/
	public static <T> Set<T> a( T root ) 
		{
		Set<T> result = new HashSet<T>();
		result.add( root );
		return result;
		}

	}
