/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.sources;

import java.util.*;

// EXPLORATORY.
public class AuthInfo {
	
	final Map<String, String> map = new HashMap<String, String>();
	
	public AuthInfo(Properties p) {
		for (Map.Entry<Object, Object> kv: p.entrySet()) {
			if (kv.getKey() instanceof String) {
				if (kv.getValue() instanceof String) {
					map.put((String) kv.getKey(), (String) kv.getValue());
				}
			}
		}
	}	
	
	public void put(String k, String v) {
		map.put(k, v);
	}

	public String get(String key) {
		return map.get(key);
	}
	
	@Override public String toString() {
		return map.toString();
	}
}