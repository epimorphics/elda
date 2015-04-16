/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

/*
	(c) Copyright 2010 Epimorphics Limited
	[see end of file]
	$Id$
*/

package com.epimorphics.lda.restlets;

import java.io.StringReader;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.epimorphics.lda.support.SharedConfig;
import com.epimorphics.util.Util;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

@Path("{sc : create}") public class ServiceCreate
    {
    final SharedConfig config;

    public ServiceCreate( @Context UriInfo u ) 
        { config = SharedConfig.create( u ); }

    @POST @Produces("text/html") public String createService( @FormParam("desc") String ttl )
        {
        try
            {
            Model desc = ModelFactory.createDefaultModel();
            desc.read( new StringReader( ttl ), "", "TTL" );
            String ln = leafName();
            String full = config.pathFor( "endpoint", ln );
            EndPoint.registerLeaf( ln, desc );
            String body = "<a href='" + full + ">" + full + "</a>";
            return Util.withBody( "here is your endpoint URI", body );
            }
        catch (Exception e)
            {
            return Util.withBody( "oops!", e.toString() );
            }
        }
    
    static int label = 1000;
    
    static synchronized String leafName() 
        { return "service-" + ++label; }
    
    @GET @Produces("text/html") public String supplyRequestForm()
        {
        String form = Util.readResource( "textlike/create-endpoint-form.html" );
        return Util.withBody( "request a new service endpoint", form );
        }
    }

    
/*
    (c) Copyright 2010 Epimorphics Limited
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
