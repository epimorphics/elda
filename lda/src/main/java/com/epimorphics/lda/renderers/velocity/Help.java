package com.epimorphics.lda.renderers.velocity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;
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

	public static Map<Resource, String> getShortnames( Model m ) {
		Map<Resource, String> uriToShortname = new HashMap<Resource, String>();
		List<Resource> pages = m.listSubjectsWithProperty( RDF.type, API.Page ).toList();
		if (pages.size() == 1) {
			List<Statement> results = pages.get(0).listProperties( API.wasResultOf ).toList();
			if (results.size() == 1) {
				List<Statement> bindings = results.get(0).getResource().listProperties( API.termBinding ).toList();
				for (Statement sb: bindings) {
					String sn = sb.getProperty( API.label ).getString();
					Resource fn = sb.getProperty( API.property ).getResource();
					uriToShortname.put( fn, sn );
				}
			}
		}
		return uriToShortname;
	}
}
