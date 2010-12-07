/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) copyright Epimorphics Limited 2010
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

package com.epimorphics.lda.core;

public class APIException extends RuntimeException {

    private static final long serialVersionUID = 6906164670791620575L;

    public APIException(String message) {
        super(message);
    }
    
    public APIException(String message, Throwable t) {
        super(message, t);
    }
}

