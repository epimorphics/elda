/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
*/
package com.epimorphics.lda.renderers;



import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.util.DOMUtils;
import com.epimorphics.util.DOMUtils.As;
import com.hp.hpl.jena.rdf.model.*;

public class XMLRenderer implements Renderer {

	public static final String XML_MIME = "text/xml";
	
	final ShortnameService sns;
	final As as;
	
	public XMLRenderer( ShortnameService sns ) {
		this( sns, As.XML );
	}
	
	public XMLRenderer( ShortnameService sns, As as ) {
		this.as = as;
		this.sns = sns;
	}
	
	@Override public String getMimeType() {
		return XML_MIME;
	}

	@Override public synchronized String render( APIResultSet results ) {
		return render( results.getRoot() );
	}

	public String render( Resource root ) {
		Document d = DOMUtils.newDocument();
		renderInto( root, d );
		return DOMUtils.docToIndentedString( d, as );
	}

	public void renderInto( Resource root, Document d ) {
		XMLRendering r = new XMLRendering( sns, d );
		Element result = d.createElement( "result" );
		result.setAttribute( "format", "linked-data-api" );
		result.setAttribute( "version", "0.2" );
		r.addResourceToElement( result, root );
		d.appendChild( result );
	}
}
