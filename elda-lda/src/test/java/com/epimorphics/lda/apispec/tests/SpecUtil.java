/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.apispec.tests;

import com.epimorphics.lda.sources.AuthMap;
import com.epimorphics.lda.specs.APISpec;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

public class SpecUtil {

	/**
	    Answer an APISPec derived from the specification at 
	    <code>root</code>, loading no other models, and using the
	    default FileManager to resolve files to load.
	*/
	public static APISpec specFrom( Resource root ) {
		return new APISpec( new AuthMap(), FileManager.get(), root, null );
	}

}
