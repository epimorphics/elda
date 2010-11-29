/*
    See lda-top/LICENCE for the licence for this software.
    (c) copyright Epimorphics Limited 2010
    $Id$
*/

/******************************************************************
    File:        SpecManagerFactory.java
    Created by:  Dave Reynolds
    Created on:  7 Feb 2010
 * 
 * (c) Copyright 2010, Epimorphics Limited
 * $Id:  $
 *****************************************************************/

package com.epimorphics.lda.specmanager;


/**
 * Supply an environment-specific implementation of the
 * SpecManager.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public class SpecManagerFactory {

    protected static SpecManager instance;
    
    public static SpecManager get() {
        return instance;
    }
    
    public static void set(SpecManager sm) {
        instance = sm;
    }
    
}

