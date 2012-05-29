package com.epimorphics.lda.query.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.epimorphics.lda.query.APIQuery;
import com.epimorphics.lda.query.QueryParameter;
import com.epimorphics.lda.tests.SNS;

public class TestAPIQuerySettings {
	
	static final int MUCH_TOO_BIG = 1000000;
	
	// trying to set the page size past the maximum
	// sets it to the maximum (not the default)
	@Test public void testPageSizeSetting() {
		APIQuery q = QueryTestUtils.queryFromSNS(new SNS(""));
		assertTrue
			( "precondition that default page size < max page size violated."
			, QueryParameter.DEFAULT_PAGE_SIZE < QueryParameter.MAX_PAGE_SIZE 
			);
	//
		q.setPageSize( MUCH_TOO_BIG );
		assertEquals( QueryParameter.MAX_PAGE_SIZE, q.getPageSize() );
	}

	@Test public void testAllowReservedDefaultIs_() {
		APIQuery q = QueryTestUtils.queryFromSNS(new SNS(""));
		assertTrue( "_ should always be allow-reserved.", q.allowReserved( "_" ) );
		assertFalse( "by default _x is not allowed", q.allowReserved("_x") );
		q.addAllowReserved( "_x" );
		assertTrue( "_x should now be allowed", q.allowReserved("_x") );
	}
}
