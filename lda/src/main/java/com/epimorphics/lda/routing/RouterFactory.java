/*
    See lda-top/LICENCE for the licence for this software.
    (c) copyright Epimorphics Limited 2010
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

import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.restlets.RouterRestlet;

/**
 * Simple implementation of Router based on Servlets.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: $
 */
// TODO replace by JAX-RS implementation if we can figure out a bearable mechanism

public class RouterFactory {

    protected static Router theRouter;
    
    public static void setRouter(Router router) {
        theRouter = router;
    }
    
    public static Router get() {
        if (theRouter == null) {
            theRouter = new RouterImpl();
        }
        return theRouter;
    }

    public static void set(Router router) {
        theRouter = router;
    }
    
    static class RouterImpl implements Router {
        
        @Override
        public void register(String URITemplate, APIEndpoint api) {
            RouterRestlet.register(URITemplate, api);
        }
    
        @Override
        public void unregister(String URITemplate) {
            RouterRestlet.unregister(URITemplate);
        }

        @Override
        public Match getMatch(String path) {
            return RouterRestlet.getMatch(path);
        }
    }
    
}

