/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.bindings;

/**
    An interface to allow expandVariables to work on general maps.
*/
public interface Lookup {
	public String getValueString( String name );
}