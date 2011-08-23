/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
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

import com.epimorphics.lda.bindings.VarValues;
import com.epimorphics.lda.core.APIEndpoint;

/**
 * Represents the result of a router matching an incoming URL
 * against its table of known endpoints.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public class Match {
    final APIEndpoint endpoint;
    final VarValues bindings;

    public Match( APIEndpoint endpoint, VarValues bindings )
        { this.endpoint = endpoint; this.bindings = bindings; }

    public APIEndpoint getEndpoint() {
        return endpoint;
    }
    
    public VarValues getBindings() {
        return bindings;
    }
}

