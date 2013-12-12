/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

/******************************************************************
    File:        ServletRouter.java
    Created by:  Dave Reynolds
    Created on:  31 Jan 2010
 * 
 * (c) Copyright 2010, Epimorphics Limited
 * $Id:  $
 *****************************************************************/

package com.epimorphics.lda.routing;

/**
 	@author <a href="mailto:der@epimorphics.com">Dave Reynolds</a>
 	@author Chris Dollin [complete revision]
 	@version $Revision: $
*/

public class RouterFactory {

    protected static Router theRouter;
    
    public static Router getDefaultRouter() {
        if (theRouter == null) theRouter = new DefaultRouter();        
        return theRouter;
    }
    
}

