/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) copyright Epimorphics Limited 2010
    $Id$
*/

package com.epimorphics.lda.tests_support;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.lda.rdfq.Any;
import com.epimorphics.lda.rdfq.Term;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/**
    A fake base-class implementation of ShortnameService that always throws
    a NotImplementedException.
    
 	@author chris
*/
public class ShortnameFake implements ShortnameService
{
	@Override public String shorten(String u) {
		throw new NotImplementedException();
	}

	@Override public String normalizeValue(String val) {
		throw new NotImplementedException();
		}

	@Override public Resource normalizeResource(String s) {
		throw new NotImplementedException();
		}

	@Override public Resource normalizeResource(RDFNode r) {
		throw new NotImplementedException();
		}

	@Override public Resource normalizeResource( Term r ) {
		throw new NotImplementedException();
		}

	@Override public String normalizeNodeToString(String prop, String val) {
		throw new NotImplementedException();
		}

	@Override public String expand(String s) {
		throw new NotImplementedException();
		}

	@Override public Context asContext() {
		throw new NotImplementedException();
		}

	@Override public String normalizeNodeToString(String prop, String val, String language) {
		throw new NotImplementedException();
		}

	@Override public String normalizeValue(String val, String language) {
		throw new NotImplementedException();
		}

	@Override public Any normalizeNodeToRDFQ(String prop, String val, String language) {
		throw new NotImplementedException();
		}
}