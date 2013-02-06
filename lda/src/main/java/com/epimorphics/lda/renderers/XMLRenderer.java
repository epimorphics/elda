/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
*/
package com.epimorphics.lda.renderers;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

//import sun.org.mozilla.javascript.internal.WrappedException;

import com.hp.hpl.jena.rdf.model.*;
import com.epimorphics.util.DOMUtils;
import com.epimorphics.util.MediaType;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.WrappedException;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.core.APIResultSet.MergedModels;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.support.Times;

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
	
	@Override public MediaType getMediaType( Bindings irrelevant ) {
		return mt;
	}

    @Override public String getPreferredSuffix() {
    	return "xml";
    }

	@Override public synchronized Renderer.BytesOut render( Times t, Bindings rc, APIResultSet results ) {
		Resource root = results.getRoot();
		boolean suppressIPTO = rc.getAsString( "_suppress_ipto", "no" ).equals( "yes" );
		Document d = DOMUtils.newDocument();
		renderInto( root, results.getModels(), d, suppressIPTO );
		return DOMUtils.renderNodeToBytesOut( t, d, rc, results.getModelPrefixes(), transformFilePath );
	}

	public void renderInto( Resource root, MergedModels mm, Document d, boolean suppressIPTO ) {
		XMLRendering r = new XMLRendering( root.getModel(), sns, suppressIPTO, d );
		Element result = d.createElement( "result" );
		result.setAttribute( "format", "linked-data-api" );
		result.setAttribute( "version", "0.2" );
		r.addResourceToElement( result, root, mm );
		d.appendChild( result );            
	//
//		OutputFormat format = new OutputFormat(d);
//        format.setLineWidth(65);
//        format.setIndenting(true);
//        format.setIndent(2);
//        Writer out = new StringWriter();
//        XMLSerializer serializer = new XMLSerializer(out, format);
//        try {
//			serializer.serialize(d);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			throw new WrappedException(e);
//		}
//        System.err.println( ">>>>>>>>>>>>>>>>>>>>>>>>>>>\n" + out.toString() );
	}
	
}
