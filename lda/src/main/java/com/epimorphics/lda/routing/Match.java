/*
    See lda-top/LICENCE for the licence for this software.
    (c) copyright Epimorphics Limited 2010
    $Id$
*/

/******************************************************************
    File:        Match.java
    Created by:  Dave Reynolds
    Created on:  8 Feb 2010
 * 
 * (c) Copyright 2010, Epimorphics Limited
 * $Id:  $
 *****************************************************************/

package com.epimorphics.lda.routing;

import java.util.Map;

import com.epimorphics.lda.core.APIEndpoint;

/**
 * Represents the result of a router maching an incoming URL
 * against its table of known endpoints.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public class Match {
    final APIEndpoint endpoint;
    final Map<String, String> bindings;

    public Match( APIEndpoint endpoint, Map<String, String> bindings )
        { this.endpoint = endpoint; this.bindings = bindings; }

    public APIEndpoint getEndpoint() {
        return endpoint;
    }
    
    public Map<String, String> getBindings() {
        return bindings;
    }
}

