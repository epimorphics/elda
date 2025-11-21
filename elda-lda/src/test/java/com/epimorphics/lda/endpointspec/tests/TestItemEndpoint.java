/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.endpointspec.tests;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.apispec.tests.SpecUtil;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.specs.APISpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestItemEndpoint {
    Model spec = ModelIOUtils.modelFromTurtle
            (
                    ":s a api:API; api:endpoint :e; api:sparqlEndpoint <http://example.com/none>."
                            + "\n:e a api:ItemEndpoint; api:uriTemplate '/absent/friends'; api:itemTemplate 'http://fake.domain.org/spoo/{what}'."
            );

    Resource s = spec.getResource(spec.expandPrefix(":s"));
    Resource e = spec.getResource(spec.expandPrefix(":e"));

    @Test
    public void ensureSpecExtractsItemTemplate() {
        APISpec a = SpecUtil.specFrom(s);
        APIEndpointSpec eps = new APIEndpointSpec(a, null, e);
        assertEquals("http://fake.domain.org/spoo/{what}", eps.getItemTemplate());
    }
}
