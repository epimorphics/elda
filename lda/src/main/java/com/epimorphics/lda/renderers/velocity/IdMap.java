package com.epimorphics.lda.renderers.velocity;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Resource;

/**
    An IdMap associates a string id with a resource.
*/
public class IdMap {
	
	final Map<Resource, String> map = new HashMap<Resource, String>();
	
	public IdMap() {
	}

	/**
	    Return the ID string associated with this resource. The ID has
	    syntax "ID-digits".
	*/
	public String get( Resource r ) {
		String id = map.get( r );
		if (id == null) map.put( r,  id = "ID-" + (map.size() + 10000) );
		return id;
	}
}
