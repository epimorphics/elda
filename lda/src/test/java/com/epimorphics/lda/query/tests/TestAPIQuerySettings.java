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
		APIQuery q = new APIQuery( new SNS("") );
		assertTrue
			( "precondition that default page size < max page size violated."
			, QueryParameter.DEFAULT_PAGE_SIZE < QueryParameter.MAX_PAGE_SIZE 
			);
	//
		q.setPageSize( MUCH_TOO_BIG );
		assertEquals( QueryParameter.MAX_PAGE_SIZE, q.getPageSize() );
	}

}
