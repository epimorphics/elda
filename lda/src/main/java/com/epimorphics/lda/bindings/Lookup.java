package com.epimorphics.lda.bindings;

/**
    A tiny interface for getting the string value of a variable.
*/
public interface Lookup {

	/**
	    Answer the value of the named variable.
	*/
	public String getAsString( String name );
}
