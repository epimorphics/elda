/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
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
import com.epimorphics.util.MediaType;
import com.hp.hpl.jena.rdf.model.Resource;

public class JSONRenderer implements Renderer {

    static Logger log = LoggerFactory.getLogger(JSONRenderer.class);
    
    final APIEndpoint api;
    final MediaType mt;
    final boolean wantContext;
    
    public JSONRenderer( APIEndpoint api ) {
        this( api, MediaType.APPLICATION_JSON );
    }
    
    public JSONRenderer( APIEndpoint api, MediaType mt ) {
        this.api = api;
        this.mt = mt;
        this.wantContext = api.wantContext();
    }
    
    @Override public MediaType getMediaType() {
        return MediaType.APPLICATION_JSON;
    }

    @Override public String render( RendererContext ignored, APIResultSet results) {
        StringWriter writer = new StringWriter();
        List<Resource> roots = new ArrayList<Resource>(1);
        roots.add( results.getRoot() );
        Context context = api.getSpec().getAPISpec().getShortnameService().asContext();
        context.setSorted(true);
        try {
            Encoder.getForOneResult( context, api.wantContext() ).encodeRecursive(results.getModel(), roots, writer, true);
            String written = writer.toString();
            ParseWrapper.readerToJsonObject( new StringReader( written ) ); // Paranoia check that output is legal Json
            return written;
        } catch (Exception e) {
        	log.error( "Failed to encode model: stacktrace follows:", e );
            return "'ERROR: " + e.getMessage() + "'";
        }
    }

}

