package com.epimorphics.lda.renderers.velocity;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDFS;

public class Help {

	
	static String labelFor( Resource r ) {
		Statement s = r.getProperty( RDFS.label );
		return s == null ? r.getLocalName() : s.getString();
	}

	public static Map<String, Object> getMetadataFrom( Model m ) {
		Map<String, Object> result = new HashMap<String, Object>();
		return result;
	}

	public static Map<String, String> getShortnames( Model m ) {
		Map<String, String> uriToShortname = new HashMap<String, String>();
		return uriToShortname;
	}
}
