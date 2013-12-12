/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.bindings;

import java.util.Map;

/**
    An interface to allow expandVariables to work on general maps.
*/
public interface Lookup {
	
	public String getValueString( String name );
	
	public static class Util {

		/**
		    Wraps a String-String map as a Lookup.
		*/
		public static Lookup asLookup( final Map<String, String> bindings ) {
			return new Lookup() {
				@Override public String getValueString(String name) {
					return bindings.get( name );
				}
			};
		}
	}
}