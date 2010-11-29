/*
    See lda-top/LICENCE for the licence for this software.
    (c) copyright Epimorphics Limited 2010
    $Id$

    File:        EncodingException.java
    Created by:  Dave Reynolds
    Created on:  21 Dec 2009
*/

package com.epimorphics.jsonrdf;

/** Return problems found during JSON encode/decode */

public class EncodingException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    public EncodingException(String message) {
        super(message);
    }
    
    public EncodingException(String message, Throwable t) {
        super(message, t);
    }

}

