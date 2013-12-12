/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.apispec.tests;

import com.epimorphics.lda.sources.AuthMap;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.support.EldaFileManager;
import com.hp.hpl.jena.rdf.model.Resource;

public class SpecUtil {

	/**
	    Answer an APISPec derived from the specification at 
	    <code>root</code>, loading no other models, and using the
	    default FileManager to resolve files to load.
	*/
	public static APISpec specFrom( Resource root ) {
		return new APISpec( "", new AuthMap(), EldaFileManager.get(), root, null );
	}

}
