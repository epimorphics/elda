/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.apispec.tests;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.specs.APISpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestAPISpecExtractsProperties {
    @Test
    public void testExtractsBase() {
        Model spec = ModelIOUtils.modelFromTurtle(new StringBuffer()
                .append(":s a api:API")
                .append("; api:sparqlEndpoint <http://example.com/none>")
                .append("; api:base 'to/be/expunged'")
                .append(".")
                .toString()
        );

        Resource s = spec.getResource(spec.expandPrefix(":s"));
        APISpec a = SpecUtil.specFrom(s);
        assertEquals("to/be/expunged", a.getBase());
    }

    @Test
    public void testExtractsEnableForwardHeaders() {
        Model spec = ModelIOUtils.modelFromTurtle(new StringBuffer()
                .append(":s a api:API")
                .append("; api:sparqlEndpoint <http://example.com/none>")
                .append("; elda:enableForwardHeaders false")
                .append(".")
                .toString()
        );

        Resource s = spec.getResource(spec.expandPrefix(":s"));
        APISpec a = SpecUtil.specFrom(s);
        assertEquals(false, a.getEnableForwardHeaders());
    }

    @Test
    public void testExtractsEnableForwardHeaders_DefaultsToTrue() {
        Model spec = ModelIOUtils.modelFromTurtle(new StringBuffer()
                .append(":s a api:API")
                .append("; api:sparqlEndpoint <http://example.com/none>")
                .append(".")
                .toString()
        );

        Resource s = spec.getResource(spec.expandPrefix(":s"));
        APISpec a = SpecUtil.specFrom(s);
        assertEquals(true, a.getEnableForwardHeaders());
    }
}
