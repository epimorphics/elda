/*
    See lda-top/LICENCE for the licence for this software.
    (c) copyright Epimorphics Limited 2010
    $Id$
*/

package com.epimorphics.lda.support;

import javax.ws.rs.core.UriInfo;

public class SharedConfig
    {
    private UriInfo u;
    
    private SharedConfig( UriInfo u ) 
        { this.u = u; }
    
    public String pathFor( String pathlet, String leaf )
        { return u.getBaseUri() + pathlet + "/" + leaf; }

    public String pathFor( String pathlet )
        { return u.getBaseUri() + pathlet; }

    public static SharedConfig create( UriInfo u )
        { return new SharedConfig( u ); } 
    }
