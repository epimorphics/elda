/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.vocabularies;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
    Stub vocabulary for GEO properties
 	@author chris
*/
public class GEOStub
	{
	static final public Resource LONG = ResourceFactory.createResource( "http://www.w3.org/2003/01/geo/wgs84_pos#long" );
	
	static final public Resource LAT = ResourceFactory.createResource( "http://www.w3.org/2003/01/geo/wgs84_pos#lat" );
	}
