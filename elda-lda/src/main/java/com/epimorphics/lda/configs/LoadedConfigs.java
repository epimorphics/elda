/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.

    (c) Copyright 2017 Epimorphics Limited
*/

package com.epimorphics.lda.configs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.epimorphics.lda.specs.APISpec;
import com.hp.hpl.jena.rdf.model.Resource;

public class LoadedConfigs {

	public static final LoadedConfigs instance = new LoadedConfigs();
	
	// filename -> (URI -> APISpec)
	final Map<String, Map<Resource, APISpec>> stashed = new HashMap<String, Map<Resource, APISpec>>();
	
	/**
		Add an API spec from the file thisPathSpec and with URI api. 
	*/
	public void stash(String thisSpecPath, Resource api, APISpec spec) {
	
		Map<Resource, APISpec> specsForPath = stashed.get(thisSpecPath);
		
		if (specsForPath == null) {
			specsForPath = new HashMap<Resource, APISpec>();
			stashed.put(thisSpecPath, specsForPath);
		}
		
		specsForPath.put(api, spec);
	}
	
	/**
		Remove all entries for this spec path
	*/
	public void unstash(String thisSpecPath) {
		stashed.remove(thisSpecPath);
	}

	public int size() {
		int n = 0;
		for (Map.Entry<String, Map<Resource, APISpec>> e: stashed.entrySet()) {
			n += e.getValue().size();
		}
		return n;
	}
	
	/**
		entries() delivers a collection containing each specPath/URI/apiSpec
		configured in.
	*/
	public List<StashEntry> entries() {
		List<StashEntry> result = new ArrayList<StashEntry>();
		for (Map.Entry<String, Map<Resource, APISpec>> e: stashed.entrySet()) {
			String specPath = e.getKey();
			for (Map.Entry<Resource, APISpec> f: e.getValue().entrySet()) {	
				Resource URI = f.getKey();
				APISpec a = f.getValue();
				result.add(new StashEntry(specPath, URI, a));
			}
		}
		return result;
	}
}