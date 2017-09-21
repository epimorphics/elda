package com.epimorphics.lda.tests;

import org.junit.Test;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.NamedViews;
import com.epimorphics.lda.exceptions.BadRequestException;
import com.epimorphics.lda.query.APIQuery;
import com.epimorphics.lda.query.ContextQueryUpdater;
import com.epimorphics.lda.query.tests.QueryTestUtils;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.shortnames.StandardShortnameService;

public class TestCountParameter {
	
	static final String defaultQuery = "?item ?__p ?__v .";

	ShortnameService sns = new StandardShortnameService();
	APIQuery q = QueryTestUtils.queryFromSNS(sns);		
	ContextQueryUpdater x = new ContextQueryUpdater( ContextQueryUpdater.ListEndpoint, (Bindings) null, NamedViews.oneNamedView, sns, q );

	@Test public void testRejectsCountEqualsYes() {
		try {
			x.handleReservedParameters(null, null, "_count", "yes");
			fail("Should have detected illegal _count parameter");
		} catch (BadRequestException e) {
			assertEquals("this endpoint does not allow _count to be altered: yes", e.getMessage());
		}
	}
}
