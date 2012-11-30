package com.epimorphics.lda.renderers.velocity;

import java.util.HashMap;
import java.util.Map;

import com.epimorphics.lda.shortnames.Transcoding;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;

public class ShortNames {

	protected final Map<String, String> map = new HashMap<String, String>();
	
	protected final PrefixMapping pm;
	
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
	
	public String getWithUpdate( Resource r ) {
		return getWithUpdate(r.getURI());
	}
	
	public String getEntry( Resource r ) {
		return getEntry(r.getURI());
	}
	
	/**
	    Return the short name for the given uri. Allocate a new
	    one according to the transcoding rule if there isn't one yet.
	*/
	public String getWithUpdate( String uri ) {
		String result = getEntry( uri );
		if (result == null) map.put(uri, result = shortForm(uri) );
		return result;
	}
	
	/**
	    Return the short name for the given uri, or null if there
	    isn't one [yet]. (This is to allow a safe test probe.)
	*/
	public String getEntry( String uri ) {
		return map.get(uri);
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
