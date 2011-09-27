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
import com.epimorphics.lda.bindings.VarValues;
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
	
	@Override public MediaType getMediaType( VarValues irrelevant ) {
		return mt;
	}

	@Override public synchronized String render( VarValues rc, APIResultSet results ) {
		return render( rc, results.getRoot() );
	}

	public String render( VarValues rc, Resource root ) {
		PrefixMapping pm = root.getModel();
		boolean stripHas = rc.getAsString( "_strip_has", "no" ).equals( "yes" );
		boolean suppressIPTO = rc.getAsString( "_suppress_ipto", "no" ).equals( "yes" );
		Document d = DOMUtils.newDocument();
		long origin = System.currentTimeMillis();
		renderInto( root, d, stripHas, suppressIPTO );
		// System.err.println( DOMUtils.renderNodeToString( d, rc, pm, null ) );
		long afterRenderToDOM = System.currentTimeMillis();
		String rendered = DOMUtils.renderNodeToString( d, rc, pm, transformFilePath );
		long afterRenderedToString = System.currentTimeMillis();
		log.debug( "TIMING: render to DOM: " + (afterRenderToDOM - origin)/1000.0 + "s" );
		log.debug( "TIMING: DOM to string: " + (afterRenderedToString - afterRenderToDOM)/1000.0 + "s" );
		return rendered;
	}

	public void renderInto( Resource root, Document d, boolean stripHas, boolean suppressIPTO ) {
		XMLRendering r = new XMLRendering( root.getModel(), sns, stripHas, suppressIPTO, d );
		Element result = d.createElement( "result" );
		result.setAttribute( "format", "linked-data-api" );
		result.setAttribute( "version", "0.2" );
		r.addResourceToElement( result, root );
		d.appendChild( result );
	}
	
}
