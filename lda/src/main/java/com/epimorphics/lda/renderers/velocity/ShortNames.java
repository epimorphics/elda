package com.epimorphics.lda.renderers.velocity;

import java.util.HashMap;
import java.util.Map;

import com.epimorphics.lda.shortnames.Transcoding;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;

public class ShortNames {

	private final Map<Resource, String> map = new HashMap<Resource, String>();
	
	private final PrefixMapping pm;
	
	public ShortNames( PrefixMapping pm, Map<Resource, String> map ) {
		this.pm = pm;
		this.map.putAll( map );
//		for (Map.Entry<Resource, String> e: map.entrySet()) {
//			System.err.println( ">> " + e.getKey() + " has shortname " + e.getValue() );
//		}
	}
	
	public ShortNames( PrefixMapping pm ) {
		this.pm = pm;
	}
	
	public String get( Resource r ) {
		String result = map.get(r);
		if (result == null) map.put(r, result = shortForm(r) );
		return result;
	}

	private String shortForm(Resource r) {
		return Transcoding.encode(pm, r.getURI());
	}

	public String getMetaName(Resource p) {
		String s = map.get(p);
		return s == null ? p.getLocalName() : s;
	}
}
