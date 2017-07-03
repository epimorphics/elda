/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.vocabularies;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

/**
    A tiny stub of SKOS properties used explicitly in Elda.

	@author chris
*/
public class SKOSstub {

	public static final String NS = "http://www.w3.org/2004/02/skos/core#";
	
	public static final Property prefLabel = ResourceFactory.createProperty( NS + "prefLabel" );

}
