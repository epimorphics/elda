/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
*/
package com.epimorphics.lda.renderers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.hp.hpl.jena.rdf.model.*;
import com.epimorphics.util.DOMUtils;
import com.epimorphics.util.MediaType;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.shortnames.ShortnameService;

public class XMLRenderer implements Renderer {
	
    static Logger log = LoggerFactory.getLogger(XMLRenderer.class);
	
	final ShortnameService sns;
	final String transformFilePath;
	final MediaType mt;
	
	public XMLRenderer( ShortnameService sns ) {
		this( sns, MediaType.TEXT_XML, null );
	}
	
	public XMLRenderer( ShortnameService sns, MediaType mt, String transformFilePath ) {
		this.sns = sns;
		this.mt = mt;
		this.transformFilePath = transformFilePath;
	}
	
	@Override public MediaType getMediaType() {
		return mt;
	}

	@Override public synchronized String render( RendererContext rc, APIResultSet results ) {
		return render( rc, results.getRoot() );
	}

	public String render( RendererContext rc, Resource root ) {
		PrefixMapping pm = root.getModel();
		Document d = DOMUtils.newDocument();
		renderInto( root, d );
		return DOMUtils.renderNodeToString( d, rc, pm, transformFilePath );
	}

	public void renderInto( Resource root, Document d ) {
		XMLRendering r = new XMLRendering( root.getModel(), sns, d );
		Element result = d.createElement( "result" );
		result.setAttribute( "format", "linked-data-api" );
		result.setAttribute( "version", "0.2" );
		r.addResourceToElement( result, root );
		d.appendChild( result );
	}
	
}
