/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$

    File:        APISecurityException.java
    Created by:  Dave Reynolds
    Created on:  7 Feb 2010
*/

package com.epimorphics.lda.core;

/**
 * Used to indicated an update/delete operation on an API
 * specification was rejected due to incorrect credential of some sort.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public class APISecurityException extends Exception {

    private static final long serialVersionUID = 6044683968144319802L;

    public APISecurityException(String message) {
        super(message);
    }
}

