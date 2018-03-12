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

public class TestPageSize {

	static final String defaultQuery = "?item ?__p ?__v .";

	ShortnameService sns = new StandardShortnameService();
	APIQuery q = QueryTestUtils.queryFromSNS(sns);		
	ContextQueryUpdater x = new ContextQueryUpdater( ContextQueryUpdater.ListEndpoint, (Bindings) null, NamedViews.oneNamedView, sns, q );
	
	@Test public void testOKPageSizes() {    
	    x.handleReservedParameters( null, null, "_pageSize", "10");
	    x.handleReservedParameters( null, null, "_pageSize", "1");
	}
	
	@Test public void testBadPageSizes() {
		testBadPageSize("0");
		testBadPageSize("-1");
	}
	
	private void testBadPageSize(String val) {
		try {
			x.handleReservedParameters(null,  null,  "_pageSize", val);
			fail("should trap bad page size");
		} catch (BadRequestException e) {
			assertEquals("_pageSize=" + val + ": value must be at least 1.", e.getMessage());
			return;
		}
	}

	@Test public void testIllegalPageSizes() {
		testIllegalPageSize("notinteger");
		testIllegalPageSize("1066AD");
	}
	
	private void testIllegalPageSize(String val) {
		try {
			x.handleReservedParameters( null, null, "_pageSize", val);
			fail("should trap bad page size");
		} catch (BadRequestException e) {
			assertEquals("_pageSize=" + val + ": value must be an integer.", e.getMessage());
			return;
		}
	}
}
