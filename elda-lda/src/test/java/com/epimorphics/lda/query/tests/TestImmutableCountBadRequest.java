package com.epimorphics.lda.query.tests;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.NamedViews;
import com.epimorphics.lda.exceptions.BadRequestException;
import com.epimorphics.lda.query.APIQuery;
import com.epimorphics.lda.query.ContextQueryUpdater;
import com.epimorphics.lda.support.MultiMap;
import com.epimorphics.lda.tests.FakeNamedViews;
import com.epimorphics.lda.tests.SNS;
import com.epimorphics.lda.tests_support.MakeData;
import org.junit.Test;

import static org.junit.Assert.fail;

public class TestImmutableCountBadRequest {

    SNS sns = new SNS("");
    APIQuery q = QueryTestUtils.queryFromSNS(sns);
    NamedViews nv = new FakeNamedViews();

    @Test
    public void testImmutableCountBadRequest() {

        MultiMap<String, String> qp = MakeData.parseQueryString("_count=yes");
        Bindings b = Bindings.createContext(MakeData.variables(""), qp);

        String doesNot = "this endpoint does not allow _count to be altered";
        ContextQueryUpdater c = new ContextQueryUpdater(ContextQueryUpdater.ListEndpoint, b, nv, sns, q);

        q.setTotalCountRequested(true);

        try {
            c.updateQueryAndConstructView(q.deferredFilters);
        } catch (BadRequestException br) {
            if (br.getMessage().contains(doesNot)) return;
            fail("BadRequest should have been '" + doesNot + "'");
        }
        fail("should have thrown Bad Request: " + doesNot);
    }

    @Test
    public void testNonBooleanCountBadRequest() {
        MultiMap<String, String> qp = MakeData.parseQueryString("_count=perhaps");
        Bindings b = Bindings.createContext(MakeData.variables(""), qp);

        String badbool = "illegal boolean (should be 'yes' or 'no') for _count";
        ContextQueryUpdater c = new ContextQueryUpdater(ContextQueryUpdater.ListEndpoint, b, nv, sns, q);
        try {
            c.updateQueryAndConstructView(q.deferredFilters);
        } catch (BadRequestException br) {
            if (br.getMessage().contains(badbool)) return;
            fail("BadRequest should have been '" + badbool + "'");
        }
        fail("should have thrown Bad Request: " + badbool);
    }
}
