/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
 */

package com.epimorphics.lda.rdfq;

import com.epimorphics.lda.support.PrefixLogger;
import com.hp.hpl.jena.rdf.model.Resource;

public class URINode extends Term {
	
	final String uriString;

	public URINode(String u) {
		this.uriString = u;
	}

	public URINode(Resource r) {
		this.uriString = r.getURI();
	}

	@Override public String asSparqlTerm(PrefixLogger pl) {
		return pl.present(uriString);
	}

	@Override public URINode replaceBy(String r) {
		return new URINode(r);
	}

	@Override public String spelling() {
		return uriString;
	}

	@Override public boolean equals(Object other) {
		return other instanceof URINode && same((URINode) other);
	}

	@Override public int hashCode() {
		return uriString.hashCode();
	}

	private boolean same(URINode other) {
		return uriString.equals(other.uriString);
	}
}