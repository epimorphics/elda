/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.core;

import java.util.Map;

import com.epimorphics.lda.bindings.Bindings;

/**
    The result from an endpoint call.
*/
public class ResponseResult {

	public final APIResultSet resultSet;
	public final Map<String, String> uriToShortnameMap;
	public final Bindings bindings;
	
	public ResponseResult(APIResultSet rs, Map<String, String> uriToShortnameMap, Bindings bindings) {
		this.resultSet = rs;
		this.uriToShortnameMap = uriToShortnameMap;
		this.bindings = bindings;
	}
	
}
