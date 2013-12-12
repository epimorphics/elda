/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.tests_support;

import java.util.Collection;
import java.util.Iterator;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class Matchers 
	{

	public static class IsEmpty<T> extends BaseMatcher<T> 
		{
		@Override public boolean matches(Object x) 
			{
			return x instanceof Collection<?> && ((Collection<?>) x).isEmpty();
			}
		
		@Override public void describeTo( Description d )
			{
			d.appendText( "an empty collection" );
			}
		}

	public static class HasNone<T> extends BaseMatcher<T> 
		{
		@Override public boolean matches(Object x) 
			{
			return x instanceof Iterator<?> && !((Iterator<?>) x).hasNext();
			}
		
		@Override public void describeTo( Description d )
			{
			d.appendText( "an empty iterator" );
			}
		}
	
	public static <T> Matchers.IsEmpty<T> isEmpty() 
		{ return new Matchers.IsEmpty<T>(); }

	public static <T> Matchers.HasNone<T> hasNone() 
		{ return new HasNone<T>(); }

	}
