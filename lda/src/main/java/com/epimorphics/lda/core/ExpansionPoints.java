/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.core;

/**
    Interface for decoupling expansion points from APIQuery. May inherit
    code in an implementation later too.
    
 	@author chris
*/
public interface ExpansionPoints 
	{
	/**
	    Record that the property with the given URI is a template expansion
	    point.
	*/
    public void addExpansion( String uri );
	}
