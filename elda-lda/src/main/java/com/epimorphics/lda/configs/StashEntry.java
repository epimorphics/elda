/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.

    (c) Copyright 2017 Epimorphics Limited
*/

package com.epimorphics.lda.configs;

import com.epimorphics.lda.specs.APISpec;
import com.hp.hpl.jena.rdf.model.Resource;

/**
	A StashEntry records a single loaded config with its
	file name (specPath), its root URI, and its compiled
	APISpec (spec).
*/
public class StashEntry {
	
	final String specPath;
	final Resource URI;
	final APISpec spec;
	
	public StashEntry(String specPath, Resource URI, APISpec a) {
		this.specPath = specPath;
		this.URI = URI;
		this.spec = a;
	}

	public APISpec getSpec() {
		return spec;
	}

	public Resource getRoot() {
		return URI;
	}
}