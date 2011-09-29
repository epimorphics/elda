/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.tests_support;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.lda.rdfq.Any;
import com.epimorphics.lda.shortnames.NameMap;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/**
    A fake base-class implementation of ShortnameService that always throws
    a NotImplementedException (except for asContext, which returns an empty
    Context for this ShortnameFake -- this is needed for XML rendering).
    
 	@author chris
*/
public class ShortnameFake implements ShortnameService
{
	final Context c;

	public ShortnameFake() {
		this.c = new Context();
	}
	
	public ShortnameFake(Model config) {
		this.c = new Context(config);
	}

	@Override public Resource asResource(String s) {
		throw new NotImplementedException();
		}

	@Override public Resource asResource(RDFNode r) {
		throw new NotImplementedException();
		}

	@Override public String expand(String s) {
		throw new NotImplementedException();
		}
	
	@Override public Context asContext() {
		return c;
		}

	@Override public Any valueAsRDFQ(String prop, String val, String language) {
		throw new NotImplementedException();
		}

	@Override public NameMap nameMap() {
		return new NameMap();
	}
}