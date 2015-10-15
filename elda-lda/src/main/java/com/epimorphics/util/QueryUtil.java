package com.epimorphics.util;

import com.epimorphics.lda.exceptions.APIException;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;

public class QueryUtil {

	/**
		Return a Jena Query object. Intercept all exceptions and wrap as
		APIExceptions. This means that the specialised error page will
		be used when rendering.
	*/
	public static Query create(String queryString) {
		try {
			return QueryFactory.create(queryString);
		} catch (Throwable e) {
			String x = e.getMessage();
			throw new APIException("Internal error building query:\n\n" + x + "\nin:\n\n" + queryString, e);
		}
	}
}
