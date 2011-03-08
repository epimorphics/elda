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

import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.util.DOMUtils;
import com.epimorphics.util.DOMUtils.Mode;
import com.hp.hpl.jena.rdf.model.*;

public class XMLRenderer implements Renderer {

	public static final String XML_MIME = "text/xml";
	
    static Logger log = LoggerFactory.getLogger(XMLRenderer.class);
	
	final ShortnameService sns;
	final Mode as;
	final String transformFilePath;
	final String mediaType;
	
	public XMLRenderer( ShortnameService sns ) {
		this( sns, Mode.AS_IS );
	}
	
	public XMLRenderer( ShortnameService sns, Mode m ) {
		this( sns, m, XML_MIME, null );
	}
	
	public XMLRenderer( ShortnameService sns, Mode m, String mediaType, String transformFilePath ) {
		this.as = m;
		this.sns = sns;
		this.mediaType = mediaType;
		this.transformFilePath = transformFilePath;
		if (m == Mode.TRANSFORM && transformFilePath == null)
			throw new RuntimeException( "Mode.TRANSFORM requested but no transform filepath supplied." );
	}
	
	@Override public String getMediaType() {
		return mediaType;
	}

	@Override public synchronized String render( APIResultSet results ) {
		return render( results.getRoot() );
	}

	public String render( Resource root ) {
		log.debug( "render with stylesheet '" + transformFilePath + "'" );
		Document d = DOMUtils.newDocument();
		renderInto( root, d );
		return DOMUtils.nodeToIndentedString( d, as, transformFilePath );
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
