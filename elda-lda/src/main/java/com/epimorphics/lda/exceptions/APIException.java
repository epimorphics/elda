/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

/******************************************************************
    File:        APIException.java
    Created by:  Dave Reynolds
    Created on:  31 Jan 2010
 * 
 * (c) Copyright 2010, Epimorphics Limited
 * $Id:  $
 *****************************************************************/

package com.epimorphics.lda.exceptions;

public class APIException extends RuntimeException {

    private static final long serialVersionUID = 6906164670791620575L;

    public APIException(String message) {
        super(message);
    }
    
    public APIException(String message, Throwable t) {
        super(message, t);
    }
}

