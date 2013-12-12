/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
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
	
	/**
	    A NamedViews with only a trivial default view, for testing
	    purposes.
	*/
	static final NamedViews noNamedViews = new NamedViews() {
		
		@Override public View getView(String viewname) {
			return null;
		}
		
		@Override public View getDefaultView() {
			return new View();
		}
	};

}
