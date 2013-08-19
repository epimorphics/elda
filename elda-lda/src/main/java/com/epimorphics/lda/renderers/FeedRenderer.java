package com.epimorphics.lda.renderers;

import java.io.*;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.shortnames.CompleteContext.Mode;
import com.epimorphics.lda.support.Times;
import com.epimorphics.util.*;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.WrappedException;

public class FeedRenderer implements Renderer {

	private final MediaType mt;
	
	public FeedRenderer(MediaType mt, Bindings bindings, Resource config) {
		this.mt = mt;
	}

	@Override public MediaType getMediaType(Bindings rc) {
		return mt;
	}

	@Override public Mode getMode() {
		return Mode.PreferPrefixes;
	}

	@Override public BytesOut render
		( final Times t
		, final Bindings b
		, Map<String, String> termBindings
		, final APIResultSet results
		) {
		return new BytesOutTimed() {

			@Override protected void writeAll(OutputStream os) {
				renderFeed( os, results, t, b );
				flush( os );
			}

			@Override protected String getFormat() {
				return FeedRendererFactory.format;
			}
			
		};
	}

	@Override public String getPreferredSuffix() {
		return FeedRendererFactory.format;
	}
	
	private static void flush( OutputStream os ) {
		try { os.flush(); } 
		catch (IOException e) { throw new WrappedException( e ); } 
		
	}
	
	private void renderFeed( OutputStream os, APIResultSet results, Times t, Bindings b ) {
		System.err.println( ">> working on RenderFeed." );
	//
		final PrefixMapping pm = results.getModelPrefixes();
		Document d = DOMUtils.newDocument();
		
		renderFeedIntoDocument( d );
		
		Transformer tr = DOMUtils.setPropertiesAndParams( t, b, pm, null );
		OutputStreamWriter u = StreamUtils.asUTF8( os );
		StreamResult sr = new StreamResult( u );
		try { tr.transform( new DOMSource( d ), sr ); } 
		catch (TransformerException e) { throw new WrappedException( e ); }
					
	}

	private void renderFeedIntoDocument(Document d) {
		Element result = d.createElement( "result" );
		d.appendChild( result );
	}

}
