/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.util;

import java.util.*;

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

	public static <T> List<T> toList(Iterator<T> it ) 
		{
		List<T> result = new ArrayList<T>();
		while (it.hasNext()) result.add(it.next());
		return result;
		}

	public static <T> Set<T> set(T ...elements) 
		{ return new HashSet<T>( list(elements) ); }
	}
