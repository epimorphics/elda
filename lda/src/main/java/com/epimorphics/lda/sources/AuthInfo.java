package com.epimorphics.lda.sources;

import java.util.HashMap;
import java.util.Map;

// EXPLORATORY.
public class AuthInfo {
	
	Map<String, String> map = new HashMap<String, String>();

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