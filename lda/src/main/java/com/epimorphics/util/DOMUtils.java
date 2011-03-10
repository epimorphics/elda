/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
*/
package com.epimorphics.util;

import java.io.File;
import java.io.StringWriter;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.epimorphics.lda.renderers.Renderer.Params;
import com.epimorphics.lda.routing.Loader;
import com.hp.hpl.jena.shared.PrefixMapping;

/**
    Handles XSLT rewrites for HTML and indented-string display
    of XML.
    
 	@author chris
*/
public class DOMUtils 
	{
	public enum Mode {TRANSFORM, AS_IS};
	
	public static DocumentBuilder getBuilder() 
		{
		try 
			{ return DocumentBuilderFactory.newInstance().newDocumentBuilder(); } 
		catch (ParserConfigurationException e) 
			{ throw new RuntimeException( e ); }
		}

	public static Document newDocument() 
		{ return getBuilder().newDocument(); }

	public static Transformer getTransformer( Mode a, String transformFilePath ) 
		throws TransformerConfigurationException, TransformerFactoryConfigurationError 
		{
		TransformerFactory tf = TransformerFactory.newInstance();
		if (a == Mode.AS_IS) 
			return tf.newTransformer();
		else 
			{
			Source s = new StreamSource( new File( transformFilePath ) );
			return tf.newTransformer( s );
			}
		}

	public static String nodeToIndentedString( Node d, PrefixMapping pm, Mode as ) 
		{
		if (as == Mode.TRANSFORM)
			throw new RuntimeException( "Mode.TRANSFORM requested, but no filepath given." );
		return nodeToIndentedString( d, new Params(), pm, as, "SHOULD_NOT_OPEN_THIS_FILEPATH" );
		}
	
	public static String nodeToIndentedString( Node d, Params p, PrefixMapping pm, Mode as, String transformFilePath ) 
		{
		try {
			String fullPath = Loader.getBaseFilePath() + transformFilePath;
			Transformer t = setPropertiesAndParams(p, pm, as, fullPath);
			DOMSource ds = new DOMSource( d );
			StringWriter sw = new StringWriter();
			StreamResult sr = new StreamResult( sw );
			t.transform( ds, sr );
			String raw = sw.toString();
			return as == Mode.AS_IS ? raw : cook( raw );
			} 
		catch (Throwable t) 
			{
			throw new RuntimeException( t );
			}	 
		}
	
	private static Transformer setPropertiesAndParams(Params p, PrefixMapping pm, Mode as,	String fullPath) 
		throws TransformerConfigurationException, TransformerFactoryConfigurationError {
		Transformer t = getTransformer( as, fullPath );
		t.setOutputProperty( OutputKeys.INDENT, "yes" );
		t.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "2" );
		for (String name: p.keySet()) t.setParameter( name, p.get( name ) );
		t.setParameter( "SPOO", "Spoo SPOO oops." );
		t.setParameter( "namespaces", namespacesDocument( pm ) );
		return t;
	}

	private static String namespacesDocument( PrefixMapping pm ) {
		StringBuilder sb = new StringBuilder();
		sb.append( "<namespaces>\n" );
		for (Map.Entry<String, String> e: pm.getNsPrefixMap().entrySet()) {
			sb.append( "<namespace prefix='" );
			sb.append( e.getKey() );
			sb.append( "'>" );
			sb.append( e.getValue() );
			sb.append( "</namespace>\n" );
		}
		sb.append( "</namespaces>\n" );
		return sb.toString();
	}

	private static String cook( String raw ) 
		{
		return raw
			.replaceAll( "\"/images", "\"../../images" )
			.replaceAll( "\"/css", "\"../../css" )
			.replaceAll( "\"/scripts", "\"../../scripts" )
			;
		}
	}
