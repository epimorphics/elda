package com.epimorphics.lda.renderers.velocity;

import java.util.HashMap;
import java.util.Map;

import com.epimorphics.lda.shortnames.Transcoding;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;

public class ShortNames {

	private final Map<String, String> map = new HashMap<String, String>();
	
	private final PrefixMapping pm;
	
	public ShortNames( PrefixMapping pm, Map<Resource, String> map ) {
		this.pm = pm;
		for (Map.Entry<Resource, String> e: map.entrySet()) 
			this.map.put(e.getKey().getURI(), e.getValue() );
//		for (Map.Entry<Resource, String> e: map.entrySet()) {
//			System.err.println( ">> " + e.getKey() + " has shortname " + e.getValue() );
//		}
	}
	
	public ShortNames( PrefixMapping pm ) {
		this.pm = pm;
	}
	
	public String get( Resource r ) {
		return get(r.getURI());
	}
	
	public String get( String uri ) {
		String result = map.get(uri);
		if (result == null) map.put(uri, result = shortForm(uri) );
		return result;
	}

	// hackery to deal with blank nodes and nodes with no short form.
	private String shortForm(String uri) {
		if (uri.endsWith("#self") && uri.startsWith("http://api.talis.com/")) return "[more ...]";
		String t = Transcoding.encode(pm, uri);
		if (t.startsWith("uri_")) return uri;
		return t;
	}

	public String getMetaName(Resource p) {
		String s = map.get(p);
		return s == null ? p.getLocalName() : s;
	}
}
