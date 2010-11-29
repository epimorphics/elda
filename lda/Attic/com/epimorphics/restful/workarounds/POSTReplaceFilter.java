/*
    See lda-top/LICENCE for the licence for this software.
    (c) copyright Epimorphics Limited 2010
    $Id$
*/

package com.epimorphics.restful.workarounds;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

public class POSTReplaceFilter implements ContainerRequestFilter
    {
    private static final String HEADER = "X-HTTP-Method-Override";
    public static final String METHOD = "_method";
       
    @Override public ContainerRequest filter(ContainerRequest request)
        {
        if (request.getMethod().equalsIgnoreCase("POST"))
            if (!override(request.getRequestHeaders().getFirst(HEADER), request))
                if (!override(request.getFormParameters().getFirst(METHOD), request))
                    override(request.getQueryParameters().getFirst(METHOD), request);
      return request;
      }
   
    private boolean override( String method, ContainerRequest request )
        {
        if (method == null || method.trim().length() == 0)
            return false;
        else
            {
            request.setMethod( method );
            return true;
            }
        }
    }
