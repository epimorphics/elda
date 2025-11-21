/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.query.tests;

import com.epimorphics.lda.core.View;
import com.epimorphics.lda.core.View.State;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.fail;

public class TestDescribeQueries {

    PrefixMapping pm = PrefixMapping.Factory.create()
            .setNsPrefix("rdf", RDF.getURI());

    Model m = ModelFactory.createDefaultModel();

    static final String expectA =
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                    + "\nDESCRIBE "
                    + "\n  rdf:intruder"
                    + "\n  <http://www.w3.org/2000/01/rdf-schema#stranger>";

    static final String expectB =
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                    + "\nDESCRIBE "
                    + "\n  <http://www.w3.org/2000/01/rdf-schema#stranger>"
                    + "\n  rdf:intruder";

    @Test
    public void testX() {
        m.withDefaultMappings(pm);
        Resource a = m.createResource(RDF.getURI() + "intruder");
        Resource b = m.createResource(RDFS.getURI() + "stranger");
        List<Resource> both = Arrays.asList(a, b);
        State s = new State(both, m, null, null, null);
        String q = View.createDescribeQueryForItems(s, both);
        if (!q.equals(expectA) && !q.equals(expectB))
            fail("wrong describe query created:\n" + q);
    }

}
