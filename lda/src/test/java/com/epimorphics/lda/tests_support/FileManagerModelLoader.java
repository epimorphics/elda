/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.tests_support;

import com.epimorphics.lda.core.ModelLoaderI;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;

/**
    A ModelLoaderI that loads the model using the FileManager.
    
 	@author chris
*/
public final class FileManagerModelLoader implements ModelLoaderI {
	/**
	    Load the model named by the uri using the FileManager's global
	    instance.
	*/
	public Model loadModel( String uri ) {
	    return FileManager.get().loadModel(uri);
	}
}