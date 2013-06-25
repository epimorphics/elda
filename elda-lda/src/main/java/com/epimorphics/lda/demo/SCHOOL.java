/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.demo;

import com.hp.hpl.jena.rdf.model.*;

public class SCHOOL
	{
	static final String ns = "http://education.data.gov.uk/def/school/";
	
	static final Model m = ModelFactory.createDefaultModel();
	
	static Property p( String local ) { return m.createProperty( ns + local ); } 
	
	static Property establishmentName = p( "establishmentName" );
	}