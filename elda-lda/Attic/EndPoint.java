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

import java.io.*;
import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.epimorphics.util.Util;
import com.epimorphics.vocabs.API;
import com.epimorphics.lda.support.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;

@Path("{e: endpoint}/{spec}") public class EndPoint
    {
    final SharedConfig config;

    public EndPoint( @Context UriInfo u ) 
        { config = SharedConfig.create( u ); }
    
    static final Map<String, ResultSetResource> resultSetResourceMap = new HashMap<String, ResultSetResource>();

    protected final static Model prefixes = loadModel( "prefixes.ttl" );
    
    static Property property( String shortForm )
        { return ResourceFactory.createProperty( prefixes.expandPrefix( shortForm ) ); }
    
    static Resource resource( String shortForm )
        { return ResourceFactory.createResource( prefixes.expandPrefix( shortForm ) ); }
    
    @GET @Produces("text/plain") public String helloWorld( @PathParam("spec") PathSegment k )
        {
        Resource spec = specNamed( new File( k.getPath() ).getName() );
        System.err.println( ">> " + spec );
        spec.getModel().write(System.err, "TTL");
        String resourceVar = getString( spec, API.resourceVar );
        String paramVar = getString( spec, API.paramVar );
        String queryString = getString( spec, API.viewer );
        MultivaluedMap<String, String> mp = k.getMatrixParameters();
        String param = mp.getFirst( paramVar );
        String pathlet = makeResultSetPathlet();
        String atURI = config.pathFor( "endpoint/" + k.getPath() + "/" + pathlet );
        ResultSetResource rsr = new ResultSetResource( config, resourceVar, paramVar, param, queryString, atURI );
        resultSetResourceMap.put( pathlet, rsr );
        return 
            magic 
            + "parameters: " + mp + "\n"
            + "result set URI: " + atURI + "\n"
            + "all results:\n"
            + rsr.pullResults( 0, 10 )
            ;
        }
    
    private String getString( Resource spec, Property p )
        {
        return spec.getRequiredProperty( p ).getString();
        }

    private Resource specNamed( String name )
        { // TODO fix this hack
        Model desc = loaded.get( name );
        if (desc == null)
            return resource( "ex:" + name ).inModel( specs );
        else
            return resource( "ex:" + name ).inModel( desc );
        }

    public static synchronized void registerLeaf( String leaf, Model desc )
        { // TODO fix this horrible hack 
        Resource full = resource( "ex:" + leaf ).inModel( desc );
        Resource S = desc.listStatements( null, RDF.type, (RDFNode) null ).next().getSubject();
        for (Statement s: S.listProperties().toList())
            full.addProperty( s.getPredicate(), s.getObject() );
        loaded.put( leaf, desc ); 
        }

    @GET @Path("{rsID}")@Produces("text/plain") public String serveFromResultSetURI( @PathParam("rsID") PathSegment rsID )
        {
        String path = rsID.getPath();
        ResultSetResource rsr = resultSetResourceMap.get( path );
        MultivaluedMap<String, String> m = rsID.getMatrixParameters();
        int offset = integer( m.getFirst( "offset" ), 0 );
        int limit = integer( m.getFirst( "limit" ), 10 );
        return rsr.pullResults( offset, limit );
        }
    
    private int integer( String s, int ifAbsent )
        {
        return s == null ? ifAbsent : Integer.parseInt( s );
        }

    static int counter = 1000;
    
    static synchronized String makeResultSetPathlet()
        {
        int count = ++counter;
        return "rs" + count;
        }
    
    protected final static String magic = loadString( "magic.text" );
    
    public final static Model model = loadModel( "model.ttl" );
    
    protected final static Model specs = loadModel( "spec-sample.ttl" );
    
    protected final static Map<String, Model> loaded = new HashMap<String, Model>();

    private static String loadString( String name )
        { return Util.readResource( "samplelike/" + name ); }

    private static Model loadModel( String name )
        { return Util.readModel( "samplelike/" + name ); }
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
