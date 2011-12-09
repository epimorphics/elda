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

import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.jsonrdf.Encoder;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.support.Times;
import com.epimorphics.util.MediaType;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.WrappedException;

public class JSONRenderer implements Renderer {

    static Logger log = LoggerFactory.getLogger(JSONRenderer.class);
    
    final APIEndpoint api;
    final MediaType mt;
    
    public JSONRenderer( APIEndpoint api ) {
        this( api, MediaType.APPLICATION_JSON );
    }
    
    public JSONRenderer( APIEndpoint api, MediaType mt ) {
        this.api = api;
        this.mt = mt;
    }
    
    @Override public MediaType getMediaType( Bindings rc ) {
    	String callback = rc.getValueString( "callback" );
        return callback == null ? mt : MediaType.TEXT_JAVASCRIPT;
    }

    @Override public Renderer.BytesOut render( Times t, Bindings b, APIResultSet results) {
        Context given = api.getSpec().getAPISpec().getShortnameService().asContext();
        
        final Resource root = results.getRoot();
        final Model model = results.getModel();
        final Context context = given.clone();
		final List<Resource> roots = new ArrayList<Resource>(1);
		roots.add( root );
		context.setSorted(true);
		
		return new Renderer.BytesOut() {

			@Override public void writeAll(Times t, OutputStream os) {
				try {
					Writer writer = StreamUtils.asUTF8( os );
					Encoder.getForOneResult( context, false ).encodeRecursive( model, roots, writer, true );
					StreamUtils.flush( os );
				} catch (Exception e) {
					log.error( "Failed to encode model: stacktrace follows:", e );
					throw new WrappedException( e );
				}				
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
        Encoder.getForOneResult( context, false ).encodeRecursive( model, roots, writer, true );
	}

//	private void paranoiaCheckForLegalJSON(String written) throws Exception {
//		try {
//			ParseWrapper.readerToJsonObject( new StringReader( written ) ); // Paranoia check that output is legal Json
//		} catch (Exception e) {
//			log.error( "Broken generated JSON:\n" + written );
//			throw e;
//		}
//	}

}

