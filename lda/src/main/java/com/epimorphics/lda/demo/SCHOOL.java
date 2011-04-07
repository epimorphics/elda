package com.epimorphics.lda.demo;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;

public class SCHOOL
{
static final String ns = "http://education.data.gov.uk/def/school/";

static final Model m = ModelFactory.createDefaultModel();

static Property p( String local ) { return m.createProperty( ns + local ); } 

static Property establishmentName = p( "establishmentName" );
}