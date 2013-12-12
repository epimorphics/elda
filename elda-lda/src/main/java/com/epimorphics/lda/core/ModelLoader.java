/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$

    File:        ModelLoader.java
    Created by:  Dave Reynolds
    Created on:  22 Feb 2010
*/

package com.epimorphics.lda.core;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * Interface for loading models from a possibly re-routed source.
 * Allows us to adapt spec execution environment to use local tdb
 * or web-app relative models.
 * 
 * @author <a href="mailto:der@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public interface ModelLoader {

    /**
     * Load a model from the given uri. Implementations may handle
     * uri prefixes in special ways.
    */
    public Model loadModel(String uri);
}

