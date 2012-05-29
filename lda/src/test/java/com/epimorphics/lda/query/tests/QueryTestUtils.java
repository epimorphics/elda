package com.epimorphics.lda.query.tests;

import com.epimorphics.lda.query.APIQuery;
import com.epimorphics.lda.query.APIQuery.QueryBasis;
import com.epimorphics.lda.query.QueryParameter;
import com.epimorphics.lda.shortnames.ShortnameService;

public class QueryTestUtils {

	/**
	    Create an APIQuery given only a shortname service, defaulting
	    all the other parts of the query in a useful way.
	*/
	public static APIQuery queryFromSNS( ShortnameService sns ) {
		return new APIQuery( fakeQB(sns) );
	}

	private static final QueryBasis fakeQB( final ShortnameService sns ) {
		return new QueryBasis() {
			@Override public final ShortnameService sns() { return sns; }
			@Override public final String getDefaultLanguage() { return null; }
			@Override public String getItemTemplate() { return null; }
			@Override public final int getMaxPageSize() { return QueryParameter.MAX_PAGE_SIZE; }
			@Override public final int getDefaultPageSize() { return QueryParameter.DEFAULT_PAGE_SIZE; }
			@Override public boolean isItemEndpoint() {	return false; }
		};
	}

}
