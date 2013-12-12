/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
*/
package com.epimorphics.lda.renderers;

import java.io.*;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.core.APIResultSet.MergedModels;
import com.epimorphics.lda.shortnames.*;
import com.epimorphics.lda.shortnames.CompleteContext.Mode;
import com.epimorphics.lda.support.Times;
import com.epimorphics.util.DOMUtils;
import com.epimorphics.util.MediaType;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.WrappedException;

public class XMLRenderer implements Renderer {
	
    static Logger log = LoggerFactory.getLogger(XMLRenderer.class);
	
	final ShortnameService sns;
	final String transformFilePath;
	final MediaType mt;
	final Mode mode;
	
	public XMLRenderer( ShortnameService sns ) {
		this( CompleteContext.Mode.PreferLocalnames, sns, MediaType.TEXT_XML, null );
	}
	
	public XMLRenderer( Mode mode, ShortnameService sns, MediaType mt, String transformFilePath ) {
		this.sns = sns;
		this.mt = mt;
		this.transformFilePath = transformFilePath;
		this.mode = mode;
	}
	
	@Override public MediaType getMediaType( Bindings irrelevant ) {
		return mt;
	}

    @Override public String getPreferredSuffix() {
    	return "xml";
    }

	@Override public Mode getMode() {
		return mode;
	}

	@Override public synchronized Renderer.BytesOut render( Times t, Bindings rc, Map<String, String> termBindings, APIResultSet results ) {
		Resource root = results.getRoot();
		Document d = DOMUtils.newDocument();
		renderInto( root, results.getModels(), d, termBindings );
		return DOMUtils.renderNodeToBytesOut( t, d, rc, results.getModelPrefixes(), transformFilePath );
	}

	public void renderInto( Resource root, MergedModels mm, Document d, Map<String, String> termBindings ) {
		XMLRendering r = new XMLRendering( mm.getMergedModel(), sns.asContext(), termBindings, d );
		Element result = d.createElement( "result" );
		result.setAttribute( "format", "linked-data-api" );
		result.setAttribute( "version", "0.2" );
		r.addResourceToElement( result, root, mm );
		d.appendChild( result );            
	//
		try {	
			// save the xml for later analysis or use in gold tests.
			if (false) {		
				new File("/tmp/gold" ).mkdirs();
				System.err.println( ">> saving rendering to /tmp/gold/*" );
				
				writeModel( mm.getObjectModel(), "/tmp/gold/object_model" );
				writeModel( mm.getMetaModel(), "/tmp/gold/meta_model" );
				writeResource( root, "/tmp/gold/root.uri" );
				
				writeShortnames( sns, mm.getMergedModel(), "/tmp/gold/names.sns" );
				
				TransformerFactory tFactory = TransformerFactory.newInstance();
				Transformer transformer = tFactory.newTransformer();
//				transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
//				transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "2");
				DOMSource source = new DOMSource( d );
				OutputStream out = new FileOutputStream( "/tmp/gold/xml-rendering.xml" );
				StreamResult stream = new StreamResult( out );
				transformer.transform( source, stream );
				out.close();
			}
		} catch (Exception e) {
			throw new WrappedException( e );
		}
	}

	private void writeShortnames( ShortnameService sns, Model m, String fileName ) throws IOException {
		OutputStream os = new FileOutputStream( new File( fileName ) );
		PrintStream ps = new PrintStream( os );
		
		
		Map<String, String> uriToName = sns.constructURItoShortnameMap( m, m );
		
		for (String uri: uriToName.keySet()) {
			ps.print( uriToName.get( uri ) );
			ps.print( "=" );
			ps.print( uri );
			ps.println();
		}
		
		ps.flush();
		os.close();
	}

	private void writeBoolean( boolean suppressIPTO, String fileName ) throws IOException {
		OutputStream os = new FileOutputStream( new File( fileName ) );
		PrintStream ps = new PrintStream( os );
		ps.println( suppressIPTO ? "true" : "false" );
		ps.flush();
		os.close();
	}

	private void writeResource(Resource root, String fileName) throws IOException {
		OutputStream os = new FileOutputStream( new File( fileName ) );
		PrintStream ps = new PrintStream( os );
		ps.println( root.getURI() );
		ps.flush();
		os.close();
	}

	private void writeModel(Model objectModel, String fileName) throws IOException {
		OutputStream os = new FileOutputStream( new File( fileName + ".ttl" ) );
		objectModel.write( os, "TTL" );
		os.close();
	}
	
}
