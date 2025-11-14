/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011, 2014 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.shortnames;

import com.epimorphics.lda.vocabularies.API;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;

import java.util.ArrayList;
import java.util.List;

/**
 * Mode containing shortname definitions for the
 * syntactically reserved properties.
 */
public class Reserved {

    public static List<Resource> allReservedResouces() {
        List<Resource> result = new ArrayList<Resource>();
        for (String r : "result item items".split(" ")) result.add(ResourceFactory.createResource(API.NS + r));
        return result;
    }

    public static Model createReservedModel() {
        Model result = ModelFactory.createDefaultModel();
        for (Resource reserved : allReservedResouces()) {
            result.add(reserved, API.label, reserved.getLocalName());
            result.add(reserved, RDF.type, RDF.Property);
        }
        return result;
    }

    static final Model reservedProperties = createReservedModel();

}
