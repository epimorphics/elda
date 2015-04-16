/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.support;

import com.epimorphics.lda.exceptions.EldaException;
import com.hp.hpl.jena.shared.WrappedException;

/**
    Versions of Class.forName and Class.newInstance that turn checked
    exceptions into unchecked ones.

	@author eh
*/
public class ReflectionSupport {

	/**
	 	Answer the class with the given name, or throw a NotFoundException
	 	if there's no such class.
	*/
	public static Class<?> classForName( String className ) {
		try 
			{ return Class.forName( className ); } 
		catch (ClassNotFoundException e) 
			{ EldaException.NotFound( "class", className ); return null; }
	}

	/**
	    Answer a new instance of the given class.
	*/
	public static <T> T newInstanceOf( Class<T> c ) {
		try { return c.newInstance(); } 
		catch (Exception e) { throw new WrappedException( e ); }
	}

}
