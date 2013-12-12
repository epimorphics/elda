/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$

    File:        QueryParseException.java
    Created by:  Dave Reynolds
    Created on:  4 Feb 2010
*/

package com.epimorphics.lda.exceptions;

/**
 * Used to indicate problems with parsing request structure,
 * should result in a 404 rather than 500 error.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public class QueryParseException extends RuntimeException {
    
    private static final long serialVersionUID = -5434366450495747094L;

    public QueryParseException(String message) {
        super(message);
    }
    
    public QueryParseException(String message, Throwable t) {
        super(message, t);
    }

}

