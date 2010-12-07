/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) copyright Epimorphics Limited 2010
    $Id$
*/

package com.epimorphics.lda.core;

/**
    Interface to views accessible by a name; decoupling between 
    APIEndpointSpec and the rest of the world.
 
 	@author chris
*/
public interface NamedViews {

	/**
	    Answer the view with the given name (null if none).
	*/
	public View getView(String viewname);

	/**
	    Answer the default view (null if none), may be a view
	    with a magic name.
	*/
	public View getDefaultView();

}
