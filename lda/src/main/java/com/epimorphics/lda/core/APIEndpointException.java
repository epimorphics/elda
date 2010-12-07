/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) copyright Epimorphics Limited 2010
    $Id$
*/

/******************************************************************
    File:        APIEndpointException.java
    Created by:  Dave Reynolds
    Created on:  8 Feb 2010
 * 
 * (c) Copyright 2010, Epimorphics Limited
 * $Id:  $
 *****************************************************************/

package com.epimorphics.lda.core;

/**
 * Exception used when the API can't access the configured sparql endpoint
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public class APIEndpointException extends APIException {

    private static final long serialVersionUID = 2375551377281406964L;

    public APIEndpointException(String message) {
        super(message);
    }

    public APIEndpointException(String message, Throwable t) {
        super(message, t);
    }
}

