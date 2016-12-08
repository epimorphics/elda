/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2015 Epimorphics Limited
*/
package com.epimorphics.lda.support.panel;

import com.epimorphics.lda.bindings.Bindings;

/**
	Switch handler; switches are for development/debugging and allow for
	alternate or new functionality to be within the code but disabled.
	Switches may inspect Bindings to allow them to be configured from
	the API[Endpoint]Spec.
*/

public class Switches {

	static protected String getStringSwitch(Bindings b, String name, String ifAbsent) {
		return b.getAsString(name, ifAbsent);
	}
	
	static protected boolean getBoolSwitch(Bindings b, String name, boolean ifAbsent) {
		String spelling = getStringSwitch(b, name, (ifAbsent ? "true" : "false"));
		return spelling.equalsIgnoreCase("true") || spelling.equalsIgnoreCase("yes");
	}
	
	static public boolean stripCacheKey(Bindings b) {
		return getBoolSwitch(b, "_stripCacheKey", true);
	}
}
