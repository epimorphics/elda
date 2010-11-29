/*
    See lda-top/LICENCE for the licence for this software.
    (c) copyright Epimorphics Limited 2010
    $Id$
*/

/******************************************************************
    File:        APIFactory.java
    Created by:  Dave Reynolds
    Created on:  31 Jan 2010
 * 
 * (c) Copyright 2010, Epimorphics Limited
 * $Id:  $
 *****************************************************************/

package com.epimorphics.lda.core;

import com.epimorphics.lda.routing.Router;

/**
 * Factory for creating ApiInstances form a specification and 
 * registering them.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public class APIFactory {

    public static APIEndpoint makeApiEndpoint(APIEndpointSpec spec) {
        return new APIEndpointImpl(spec);
    }
    
    /**
     * Manufacture ApiInstances for each instance defined in the
     * given API and register them with the router.
     */
    public static void registerApi(Router router, APISpec spec) {
        for (APIEndpointSpec eps : spec.getEndpoints()) {
            APIEndpoint ep = makeApiEndpoint(eps);
            router.register(ep.getURITemplate(), ep);
        }
    }
    
}

