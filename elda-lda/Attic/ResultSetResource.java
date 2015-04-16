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

package com.epimorphics.lda.support;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.epimorphics.vocabs.API;
import com.epimorphics.lda.restlets.EndPoint;
import com.epimorphics.lda.vocabularies.XHV;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class ResultSetResource
    {
    final String resourceVar;
    final String parameterName;
    final String parameterValue;
    final String queryString;
    final String myURI;
    
    public ResultSetResource( SharedConfig config, String var, String name, String value, String query, String myURI )
        {
        this.resourceVar = var;
        this.parameterName = name;
        this.parameterValue = value;
        this.queryString = query;
        this.myURI = myURI;
        }
    
    public String getURI()
        { return myURI; }
    
    public String pullResults( int from, int limit )
        {
        List<Resource> inOrder = new ArrayList<Resource>();
        Model resultModel = ModelFactory.createDefaultModel();
        resultModel.setNsPrefixes( EndPoint.model );
        Resource me = resultModel.createResource( myURI );
        Resource firstPage = resultModel.createResource( myURI + ";offset=0" );
        Resource nextPage = resultModel.createResource( myURI + ";offset=" + (from + limit) + ";limit=" + limit );
        Resource prevPage = resultModel.createResource( myURI + ";offset=" + notNegative(from - limit) + ";limit=" + limit );
        me
            .addProperty( RDF.type, API.ListEndpoint)
            .addProperty( RDFS.comment, "some comment here" )
            .addProperty( XHV.first, firstPage )
            ;
        ResultSet rs = rq( from, limit );
        StringBuilder b = new StringBuilder();
        while (rs.hasNext()) 
            {
            QuerySolution row = rs.next();
            Resource x = row.getResource( resourceVar );
            inOrder.add( x );
            System.err.println( "|> " + resourceVar + " = " + x );
            resultModel.add( x.inModel( EndPoint.model ).listProperties() );
            b.append( row );
            b.append( "\n" );
            }
        RDFList list = resultModel.createList( inOrder.iterator() );
        firstPage
            .addProperty( API.contains, list )
            .addProperty( XHV.first, firstPage )
            .addProperty( XHV.prev, prevPage )
            .addProperty( XHV.next, nextPage )
            ;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        resultModel.write( bos, "TTL" );
        String resultModelString = bos.toString();
        return b.toString() + "\n" + resultModelString;
        }
    
    private ResultSet rq( int from, int limit )
        {
        QuerySolution initial = QuerySupport.mapToStringLiteral( parameterName, parameterValue );
        return QuerySupport.runQuery( EndPoint.model, queryString, from, limit, initial );
        }
    
    private int notNegative( int i )
        { return i < 0 ? 0 : i; }
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
