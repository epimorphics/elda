/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
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

import java.io.*;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.jsonrdf.*;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.log.ELog;
import com.epimorphics.lda.shortnames.*;
import com.epimorphics.lda.shortnames.CompleteContext.Mode;
import com.epimorphics.lda.support.Times;
import com.epimorphics.util.MediaType;
import com.epimorphics.util.StreamUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.WrappedException;

public class JSONRenderer implements Renderer {

	static final String JSON_POISON = "\n[:]'[:]\"[:]\nPROBLEM:\n";
    static Logger log = LoggerFactory.getLogger(JSONRenderer.class);
    
    final APIEndpoint api;
    final MediaType mt;
    final CompleteContext.Mode mode;
    final Boolean jsonUsesISOdate;
    
    public JSONRenderer( APIEndpoint api ) {
        this( Mode.PreferLocalnames, api, MediaType.APPLICATION_JSON, false );
    }
    
    public JSONRenderer( CompleteContext.Mode mode, APIEndpoint api, MediaType mt, Boolean jsonUsesISOdate ) {
        this.mode = mode;
        this.api = api;
        this.mt = mt;
        this.jsonUsesISOdate = jsonUsesISOdate;
    }
    
    @Override public MediaType getMediaType( Bindings b ) {
    	String callback = b.getValueString( "callback" );
        return callback == null ? mt : MediaType.TEXT_JAVASCRIPT;
    }

    @Override public String getPreferredSuffix() {
    	return "json";
    }
    
    @Override public Mode getMode() {
    	return mode;
    }

    @Override public Renderer.BytesOut render( Times t, Bindings b, final Map<String, String> termBindings, APIResultSet results) {
        String callback = b.getValueString( "callback" );
        final String before = (callback == null ? "" : callback + "(");
        final String after = (callback == null ? "" : ")");
        final Model model = results.getMergedModel();
        final Resource root = results.getRoot().inModel(model);
		ShortnameService sns = api.getSpec().getAPISpec().getShortnameService();
        final ReadContext context = CompleteReadContext.create(sns.asContext(), termBindings );        
		final List<Resource> roots = new ArrayList<Resource>(1);
		roots.add( root );
	//
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			Writer writer = StreamUtils.asUTF8( os );
			writer.write( before );
			Encoder.getForOneResult( context, jsonUsesISOdate ).encodeRecursive( model, roots, writer, true );
			writer.write( after );
			writer.flush();
		} catch (Exception e) {
			log.error(ELog.message("failed to encode model: stacktrace follows: %s", e));
			throw new WrappedException( e );
		}				
		final String content = UTF8.toString( os );
	//
		return new BytesOutTimed() {

			@Override public void writeAll( OutputStream os ) {
				StreamUtils.writeAsUTF8(content, os);			
			}

			@Override protected String getFormat() {
				return "json";
			}

			@Override public String getPoison() {
				return JSON_POISON;
			}
			
		};
    }

    // testing only.
	public void renderAndDiscard( Bindings b, Model model, Resource root, Context given ) {
		List<Resource> roots = new ArrayList<Resource>(1);
		roots.add( root );
		StringWriter writer = new StringWriter();
		Context context = given.clone();
		context.setSorted(true);
        Encoder.getForOneResult( context ).encodeRecursive( model, roots, writer, true );
	}

}

