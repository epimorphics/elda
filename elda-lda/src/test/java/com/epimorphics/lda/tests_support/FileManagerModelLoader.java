/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.tests_support;

import com.epimorphics.lda.core.ModelLoader;
import com.epimorphics.lda.support.EldaFileManager;
import com.hp.hpl.jena.rdf.model.Model;

/**
    A ModelLoaderI that loads the model using the FileManager.
    
 	@author chris
*/
public final class FileManagerModelLoader implements ModelLoader {
	/**
	    Load the model named by the uri using the FileManager's global
	    instance.
	*/
	@Override public Model loadModel( String uri ) {
	    return EldaFileManager.get().loadModel(uri);
	}
}