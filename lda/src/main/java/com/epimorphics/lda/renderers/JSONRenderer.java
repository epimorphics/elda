/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) copyright Epimorphics Limited 2010
    $Id$
*/

/******************************************************************
    File:        JSONRenderer.java
    Created by:  Dave Reynolds
    Created on:  4 Feb 2010
 * 
 * (c) Copyright 2010, Epimorphics Limited
 * $Id:  $
 *****************************************************************/

package com.epimorphics.lda.renderers;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.jsonrdf.Encoder;
import com.epimorphics.jsonrdf.ParseWrapper;
import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.core.APIResultSet;
import com.hp.hpl.jena.rdf.model.Resource;

public class JSONRenderer implements Renderer {

    public static final String JSON_MIME = "application/json";

    static Logger log = LoggerFactory.getLogger(JSONRenderer.class);
    
    APIEndpoint api;
    String mime = JSON_MIME;
    
    public JSONRenderer( APIEndpoint api ) {
        this.api = api;
    }
    
    public JSONRenderer( APIEndpoint api, String overrideMime ) {
        this.api = api;
        mime = overrideMime;
    }
    
    @Override public String getMimeType() {
        return mime;
    }

    @Override public String render(APIResultSet results) {
        StringWriter writer = new StringWriter();
        List<Resource> roots = new ArrayList<Resource>(1);
        roots.add( results.getRoot() );
        Context context = api.getSpec().getAPISpec().getShortnameService().asContext();
        context.setSorted(true);
        try {
            Encoder.getForOneResult( context ).encodeRecursive(results, roots, writer, true);
            String written = writer.toString();
            ParseWrapper.readerToJsonObject( new StringReader( written ) ); // Paranoia check that output is legal Json
            return written;
        } catch (Exception e) {
        	log.error( "Failed to encode model: stacktrace follows:", e );
            return "'ERROR: " + e.getMessage() + "'";
        }
    }

}

