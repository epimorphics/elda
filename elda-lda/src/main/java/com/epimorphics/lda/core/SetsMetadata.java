/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.core;

import com.hp.hpl.jena.rdf.model.Model;

/**
    Interface for classes that accept named metadata.
*/
public interface SetsMetadata {

	public void setMetadata( String type, Model meta );

}
