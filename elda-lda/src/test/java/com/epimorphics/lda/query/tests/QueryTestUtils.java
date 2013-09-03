package com.epimorphics.lda.query.tests;

import com.epimorphics.lda.query.*;
import com.epimorphics.lda.shortnames.ShortnameService;

public class QueryTestUtils {


	/**
	    Create an APIQuery given only a shortname service, defaulting
	    all the other parts of the query in a useful way.
	*/
	public static APIQuery queryFromSNS( ShortnameService sns ) {
		return new APIQuery( new StubQueryBasis(sns) );
	}

}
