package com.epimorphics.lda.renderers;

import java.io.*;
import java.util.*;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.core.APIResultSet.MergedModels;
import com.epimorphics.lda.renderers.XMLRendering.Trail;
import com.epimorphics.lda.shortnames.CompleteContext.Mode;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.support.Times;
import com.epimorphics.lda.vocabularies.EXTRAS;
import com.epimorphics.util.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.WrappedException;
import com.hp.hpl.jena.vocabulary.RDFS;

public class FeedRenderer implements Renderer {

	private final MediaType mt;
	private final Resource config;
	private final ShortnameService sns;
	
	public FeedRenderer
		( MediaType mt
		, Bindings bindings
		, Resource config
		, ShortnameService sns 
		) {
		this.mt = mt;
		this.config = config;
		this.sns = sns;
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
		, final Map<String, String> termBindings
		, final APIResultSet results
		) {
		return new BytesOutTimed() {

			@Override protected void writeAll(OutputStream os) {
				renderFeed( os, results, t, termBindings, b );
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
	
	private void renderFeed( OutputStream os, APIResultSet results, Times t, Map<String, String> termBindings, Bindings b ) {
		final PrefixMapping pm = results.getModelPrefixes();
		Document d = DOMUtils.newDocument();
		renderFeedIntoDocument( d, termBindings, results );
	//
		Transformer tr = DOMUtils.setPropertiesAndParams( t, b, pm, null );
		OutputStreamWriter u = StreamUtils.asUTF8( os );
		StreamResult sr = new StreamResult( u );
		try { tr.transform( new DOMSource( d ), sr ); } 
		catch (TransformerException e) { throw new WrappedException( e ); }
					
	}

	private void renderFeedIntoDocument
		( Document d
		, Map<String, String> termBindings
		, APIResultSet results 
		) {
		Element feed = d.createElement( "feed" );
		feed.setAttribute( "xmlns", "http://www.w3.org/2005/Atom" );
	//
		addChild( feed, "title", getFeedTitle() );
		addLinkChild( feed, results.getRoot().getURI() );
		addChild( feed, "updated", "SOME-UPDATE-TIME" );
		addChild( feed, "author", "<name>Nemo</name>" );
		addChild( feed, "id", results.getRoot().getURI() );
	//		
		MergedModels mm = results.getModels();
		XMLRendering xr = new XMLRendering
			( mm.getMergedModel()
			, sns.asContext()
			, termBindings
			, false
			, d 
			);
	//
		for (Resource r: results.getResultList()) {
			Element entry = d.createElement( "entry" );
			addChild( entry, "title", getEntryTitle( r ) );
			addChild( entry, "updated", "THE TIME" );
			addChild( entry, "author", "<name>Nemo</name>" );
			addChild( entry, "id", r.getURI() );
			
			Element content = d.createElement( "content" );
			content.setAttribute( "type", "application/xml" );
			
			Set<Resource> cyclic = new HashSet<Resource>();
			Set<Resource> seen = new HashSet<Resource>();
			Set<Resource> blocked = new HashSet<Resource>();
			
			Trail t = new Trail( cyclic, seen, blocked );
			xr.expandProperties( t, content, r );

			entry.appendChild( content );

			feed.appendChild( entry );
		}
	//
		d.appendChild( feed );
	}

	private String getEntryTitle(Resource r) {
		Statement s = r.getProperty( RDFS.label );
		return s == null ? r.getURI() : s.getString();
	}

	private void addLinkChild( Element feed, String root ) {
		Document d = feed.getOwnerDocument();
		Element child = d.createElement( "link" );
		feed.setAttribute( "rel", "self" );
		feed.setAttribute( "type", "application/atom+xml" );
		feed.setAttribute( "href", root );
		feed.appendChild( child );
		
	}

	static final Property TITLE = ResourceFactory.createProperty( EXTRAS.EXTRA, "feedTitle" );
	
	private String getFeedTitle() {
		return RDFUtils.getStringValue( config, TITLE, "Elda feed" );
	}

	private void addChild( Element e, String tag, String body ) {
		Document d = e.getOwnerDocument();
		Element child = d.createElement( tag );
		child.appendChild( d.createTextNode( body ) );
		e.appendChild( child );
	}

}
