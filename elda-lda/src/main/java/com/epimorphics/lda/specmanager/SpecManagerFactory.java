/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Supply an environment-specific implementation of the
 * SpecManager.
 * 
 * @author <a href="mailto:der@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public class SpecManagerFactory {

    static Logger log = LoggerFactory.getLogger(SpecManagerFactory.class);
    
    protected static SpecManager instance;
    
    public static SpecManager get() {
        return instance;
    }
    
    public static void set(SpecManager sm) {
    	log.info( "Setting SpecManager " + sm );
        instance = sm;
    }
    
}

