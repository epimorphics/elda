/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
*/
package com.epimorphics.vocabs;

import com.epimorphics.lda.vocabularies.API;
import com.hp.hpl.jena.rdf.model.impl.Util;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class NsUtils {

	public static final String XHV_ns = "http://www.w3.org/1999/xhtml/vocab#";

	public static boolean isMagic( String ns ) {
		return 
			ns.equals(NsUtils.XHV_ns) 
			|| ns.equals(RDF.getURI()) 
			|| ns.equals(RDFS.getURI()) 
			|| ns.equals(API.NS)
			;
	}

	public static String getNameSpace( String uri ) {
		int split = Util.splitNamespace(uri);
		return uri.substring(0, split);
	}

	public static String getLocalName( String uri ) {
		int split = Util.splitNamespace(uri);
		return uri.substring(split);
	}
}
