package com.epimorphics.lda.vocabularies;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
    A tiny stub of SKOS properties used explicitly in Elda.

	@author chris
*/
public class SKOSstub {

	public static final String NS = "http://www.w3.org/2004/02/skos/core#";
	
	public static final Property prefLabel = ResourceFactory.createProperty( NS + "prefLabel" );

}
