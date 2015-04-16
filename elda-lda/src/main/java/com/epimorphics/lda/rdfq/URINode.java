/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
 */

package com.epimorphics.lda.rdfq;

import com.epimorphics.lda.support.PrefixLogger;

public class URINode extends Term {
	final String URI;

	public URINode(String URI) {
		this.URI = URI;
	}

	@Override public String asSparqlTerm(PrefixLogger pl) {
		return pl.present(URI);
	}

	@Override public URINode replaceBy(String r) {
		return new URINode(r);
	}

	@Override public String spelling() {
		return URI;
	}

	@Override public boolean equals(Object other) {
		return other instanceof URINode && same((URINode) other);
	}

	@Override public int hashCode() {
		return URI.hashCode();
	}

	private boolean same(URINode other) {
		return URI.equals(other.URI);
	}
}