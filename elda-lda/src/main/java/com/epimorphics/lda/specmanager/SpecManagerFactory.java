/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
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

import java.util.ArrayList;
import java.util.List;

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
    
    protected static List<SpecManager> managers = new ArrayList<SpecManager>();
    
    public static SpecManager get() {
        return managers.isEmpty() ? null : managers.get(0);
    }
    
    public static void set(SpecManager sm) {
        managers.add(sm);
    }

	public static List<SpecEntry> allSpecs() {
		List<SpecEntry> specs = new ArrayList<SpecEntry>();
		for (SpecManager sm: managers) specs.addAll( sm.allSpecs() );
		return specs;
	}
    
}

