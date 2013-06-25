package com.epimorphics.lda.renderers.json;

import java.util.*;

import com.epimorphics.lda.shortnames.Transcoding;
import com.epimorphics.lda.support.MultiMap;
import com.epimorphics.util.NameUtils;
import com.hp.hpl.jena.shared.PrefixMapping;

public class JSONPropertyNaming {
	
	final PrefixMapping pm;
	
	public JSONPropertyNaming(PrefixMapping given) {
		this.pm = PrefixMapping.Factory.create()
			.setNsPrefixes( given )
//			.withDefaultMappings( Prefixes.various )
			;
	}	
	
	public Map<String, String> complete( Map<String, String> given, Set<String> given_uris ) {
		Map<String, String> result = new HashMap<String, String>();
		MultiMap<String, String> shortnamesToURIs = new MultiMap<String, String>();
		Set<String> deferred = new HashSet<String>();
		Set<String> uris = removeExisting( given, given_uris );
	//
		for (String u: uris) {
			String ln = NameUtils.localName(u);
			if (NameUtils.isLegalShortname(ln)) {
				shortnamesToURIs.add( ln, u );
			} else {
				deferred.add( u );
			}
		}
	//
		for (String s: shortnamesToURIs.keySet()) {
			Set<String> candidates = shortnamesToURIs.getAll(s);
			if (candidates.size() == 1) {
				String uri = candidates.iterator().next();
				result.put( uri, s );
			}
		}
	//
		for (String u: uris) {
			if (result.containsKey(u) == false) {
				String ln = NameUtils.localName(u);
				String ns = NameUtils.nameSpace(u);
				String prefix = pm.getNsURIPrefix(ns);
				if (prefix == null) {
					result.put( u, Transcoding.encode( pm, u ) );
				} else {
					result.put( u, prefix + "_" + ln );
				}
			}
		}
	//
		return result;
	}
	
	private Set<String> removeExisting(Map<String, String> given, Set<String> given_uris) {
		Set<String> result = new HashSet<String>( given_uris.size() );
		for (String u: given_uris) if (!given.containsKey(u)) result.add(u);
		return result;
	}

}
