/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
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
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.specs.APISpec;

/**
 * Factory for creating ApiInstances from a specification and 
 * registering them.
 * 
 * @author <a href="mailto:der@epimorphics.com">Dave Reynolds</a>
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
    public static void registerApi(Router router, String context, APISpec spec) {
        for (APIEndpointSpec eps : spec.getEndpoints()) {
            APIEndpoint ep = makeApiEndpoint(eps);
            router.register(context, ep.getURITemplate(), ep);
        }
    }
    
}

