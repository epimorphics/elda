/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.query.tests;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.NamedViews;
import com.epimorphics.lda.core.View;
import com.epimorphics.lda.query.APIQuery;
import com.epimorphics.lda.query.ContextQueryUpdater;
import com.epimorphics.lda.query.PendingParameterValue;
import com.epimorphics.lda.tests.SNS;
import com.epimorphics.lda.tests_support.MakeData;
import org.apache.jena.shared.PrefixMapping;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestDropsEmptyValueElements {

    @Test
    public void ensureEmptyPropertiesIgnored() {
        NamedViews nv = NamedViews.oneNamedView;
        SNS sns = new SNS("a=eh:/A");
        Bindings cc = Bindings.createContext(new Bindings(), MakeData.parseQueryString("_properties=,a,"));
        APIQuery aq = QueryTestUtils.queryFromSNS(sns);
        ContextQueryUpdater cu = new ContextQueryUpdater
                (ContextQueryUpdater.ListEndpoint
                        , cc
                        , nv
                        , sns
                        , aq
                );
        View v = cu.updateQueryAndConstructView(new ArrayList<PendingParameterValue>());
        assertEquals(1, v.chains().size());
        assertEquals(1, v.chains().get(0).getProperties().size());
        assertEquals("eh:/A", v.chains().get(0).getProperties().get(0).asProperty().getURI());
    }

    @Test
    public void ensureEmptySortsIgnored() {
        NamedViews nv = NamedViews.oneNamedView;
        SNS sns = new SNS("a=eh:/A;b=eh:/B");
        Bindings cc = Bindings.createContext(new Bindings(), MakeData.parseQueryString("_sort=,b,"));
        APIQuery aq = QueryTestUtils.queryFromSNS(sns);
        ContextQueryUpdater cu = new ContextQueryUpdater
                (ContextQueryUpdater.ListEndpoint
                        , cc
                        , nv
                        , sns
                        , aq
                );
        cu.updateQueryAndConstructView(new ArrayList<PendingParameterValue>());
        String q = aq.assembleSelectQuery(PrefixMapping.Standard);
        assertTrue("empty sort string element not discarded", q.matches("(?s).*item <eh:/B> \\?___0.*ORDER BY +\\?___0.*"));
    }
}
