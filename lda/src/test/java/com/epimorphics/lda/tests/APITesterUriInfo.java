/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

/**
 * 
 */
package com.epimorphics.lda.tests;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
    A partial implementation of UriInfo for the API tests.
 
 	@author der
*/
public final class APITesterUriInfo implements UriInfo {

    private URI base;
    private final MultivaluedMap<String, String> queryParameters;
    
    public APITesterUriInfo(String uri, MultivaluedMap<String, String> queryParameters ) {
    	this.queryParameters = queryParameters;
        try {
            base = new URI(uri);
        } catch (URISyntaxException e) {}
    }
    
    @Override public URI getAbsolutePath() {
        return  UriBuilder.fromUri(base)
            .replaceQuery("").fragment("")
                .build(); 
    }

    @Override public UriBuilder getAbsolutePathBuilder() {
        return UriBuilder.fromUri(getAbsolutePath());
    }

    @Override public URI getBaseUri() {
        return null;
    }

    @Override public UriBuilder getBaseUriBuilder() {
        return null;
    }

    @Override public List<Object> getMatchedResources() {
        return null;
    }

    @Override public List<String> getMatchedURIs() {
        return null;
    }

    @Override public List<String> getMatchedURIs(boolean decode) {
        return null;
    }

    @Override public String getPath() {
        return base.getPath();
    }

    @Override public String getPath(boolean decode) {
        return null;
    }

    @Override public MultivaluedMap<String, String> getPathParameters() {
        return null;
    }

    @Override public MultivaluedMap<String, String> getPathParameters(boolean decode) {
        return null;
    }

    @Override public List<PathSegment> getPathSegments() {
        return null;
    }

    @Override public List<PathSegment> getPathSegments(boolean decode) {
        return null;
    }

    @Override public MultivaluedMap<String, String> getQueryParameters() {
        return getQueryParameters( true );
    }

    @Override public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
        return queryParameters;
    }

    @Override public URI getRequestUri() {
        return base;
    }

    @Override public UriBuilder getRequestUriBuilder() {
        return UriBuilder.fromUri(base);
    }
    
}