/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.rdfq;

import java.util.List;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;

/**
    Support code for constructing SPARQL queries.
*/
public class SparqlSupport {

	/**
	    Add SPARQL prefix declarations for all the prefixes in
	    <code>pm</code> to the StringBuilder <code>q</code>.
	*/
	public static void appendPrefixes( StringBuilder q, PrefixMapping pm ) {
		for (String prefix: pm.getNsPrefixMap().keySet()) {
			q
				.append( "PREFIX " )
				.append( prefix )
				.append( ": <" )
				.append( pm.getNsPrefixURI(prefix) )
				.append( ">\n" );
		}
	}

	/**
		Return a String[Builder] containing the SPARQL fragment
		FILTER(?item = R1 || ?item = R2 ...) where the Ri are the
		resources in <code>roots</code>.
	*/
	public static StringBuilder itemsAsFilter( List<Resource> roots ) {
		StringBuilder result = new StringBuilder();
		String OR = "";
		result.append( "FILTER(" );
		for (Resource r: roots) {
			result.append( OR );
			result.append( "?item = <" ).append( r.getURI() ).append( ">" );
			OR = " || ";
		}
		result.append( ")" );
		return result;
	}

}
