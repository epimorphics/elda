package com.epimorphics.lda.bindings;

import java.util.Set;

/**
    A tiny interface for getting the string value of a variable.
*/
public interface Lookup {

	/**
	    Answer the value of the named variable.
	*/
	public String getStringValue( String name );
	
	/**
	    Answer all the values of the named variable.
	*/
	public Set<String> getStringValues( String name );
}
