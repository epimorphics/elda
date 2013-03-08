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
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.jsonrdf.ContextPropertyInfo;
import com.epimorphics.jsonrdf.Encoder;
import com.epimorphics.jsonrdf.ReadContext;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.shortnames.NameMap;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.support.Times;
import com.epimorphics.util.MediaType;
import com.epimorphics.util.StreamUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.WrappedException;
import com.hp.hpl.jena.vocabulary.RDFS;

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
    
    @Override public MediaType getMediaType( Bindings b ) {
    	String callback = b.getValueString( "callback" );
        return callback == null ? mt : MediaType.TEXT_JAVASCRIPT;
    }

    @Override public String getPreferredSuffix() {
    	return "json";
    }

    @Override public Renderer.BytesOut render( Times t, Bindings b, APIResultSet results) {
        String callback = b.getValueString( "callback" );
        final String before = (callback == null ? "" : callback + "(");
        final String after = (callback == null ? "" : ")");
        final Model model = results.getMergedModel();
        final Resource root = results.getRoot().inModel(model);
        final ReadContext context = makeReadContext( model );        
		final List<Resource> roots = new ArrayList<Resource>(1);
		roots.add( root );
		
		return new BytesOutTimed() {

			@Override public void writeAll( OutputStream os ) {
				try {
					Writer writer = StreamUtils.asUTF8( os );
					writer.write( before );
					Encoder.getForOneResult( context, false ).encodeRecursive( model, roots, writer, true );
					writer.write( after );
					writer.flush();
				} catch (Exception e) {
					log.error( "Failed to encode model: stacktrace follows:", e );
					throw new WrappedException( e );
				}				
			}

			@Override protected String getFormat() {
				return "json";
			}
			
		};
    }

	private ReadContext makeReadContext( Model m ) {
		ShortnameService sns = api.getSpec().getAPISpec().getShortnameService();
		final NameMap nm = sns.nameMap();
	//
		final Map<String, ContextPropertyInfo> infos = nm.getInfoMap();	
		for (StmtIterator statements = m.listStatements(); statements.hasNext();) {
			Statement s = statements.next();
			Property p = s.getPredicate();
			ContextPropertyInfo cpi = infos.get( p );
			if (cpi == null) {
				String uri = p.getURI(), shortName = p.getLocalName();
				infos.put(uri,  cpi = new ContextPropertyInfo( uri, shortName ) );
			}
			cpi.addType( s.getObject() );
		}		
	//
		Context given = sns.asContext();
        final Context context = given.clone();
		return new ReadContext() {
			
			@Override public boolean isSortProperties() {
				return true;
			}
			
			@Override public String getURIfromName(String code) {
				log.warn( "readContext: getURIfromName unexpectedly called." );
				return context.getURIfromName(code);
			}
			
			@Override public ContextPropertyInfo getPropertyByName(String name) {
				log.warn( "readContext: getpropertyByName unexpectedly called." );
				return context.getPropertyByName(name);
			}
			
			@Override public String getNameForURI(String uri) {
				log.warn( "readContext: getNameForURI unexpectedly called." );
				return context.getNameForURI(uri);
			}
			
			@Override public String getBase() {
				return context.getBase();
			}
			
			@Override public String forceShorten(String uri) {
				log.warn( "readContext: forceShorten unexpectedly called." );
				return context.forceShorten(uri);
			}
			
			@Override public ContextPropertyInfo findProperty(Property p) {
				ContextPropertyInfo cpi_old = context.findProperty( p );
				ContextPropertyInfo cpi_new = infos.get( p.getURI() );				
				// warnIfNotEqual( cpi_old, cpi_new );
				ContextPropertyInfo cpi = cpi_new;
				return cpi;
			}
			
			private void warnIfNotEqual(ContextPropertyInfo a, ContextPropertyInfo b) {
				if (!a.equals(b)) {
					log.warn( "findProperty: internally inconsistent property infos" );
					log.warn( "  old version: " + a );
					log.warn( "  new version: " + b );
//					throw new RuntimeException("BOOM");
				}
			}

			@Override public Set<String> allNames() {
				log.warn( "readContext: allNames unexpectedly called." );
				return context.allNames();
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

