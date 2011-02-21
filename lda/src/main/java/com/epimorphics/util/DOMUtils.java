/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
*/
package com.epimorphics.util;

import java.io.File;
import java.io.StringWriter;

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

import com.epimorphics.lda.routing.Loader;

public class DOMUtils 
	{
	public enum As {HTML, XML};
	
	public static DocumentBuilder getBuilder() 
		{
		try 
			{ return DocumentBuilderFactory.newInstance().newDocumentBuilder(); } 
		catch (ParserConfigurationException e) 
			{ throw new RuntimeException( e ); }
		}

	public static Document newDocument() 
		{ return getBuilder().newDocument(); }

	public static Transformer getTransformer( As a ) 
		throws TransformerConfigurationException, TransformerFactoryConfigurationError 
		{
		TransformerFactory tf = TransformerFactory.newInstance();
		if (a == As.XML) 
			return tf.newTransformer();
		else 
			{
			String bfp = Loader.getBaseFilePath();
			System.err.println( ">> bfp = " + bfp );
			Source s = new StreamSource( new File( bfp + "/xsltsheets/results.xsl" ) );
			return tf.newTransformer( s );
			}
		}

	public static String nodeToIndentedString( Node d, As as ) 
		{
		try {
			Transformer t = getTransformer( as );
			t.setOutputProperty( OutputKeys.INDENT, "yes" );
			t.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "2" );
			DOMSource ds = new DOMSource( d );
			StringWriter sw = new StringWriter();
			StreamResult sr = new StreamResult( sw );
			t.transform( ds, sr );
			String raw = sw.toString();
			return as == As.XML ? raw : cook(raw);
			} 
		catch (Throwable t) 
			{
			throw new RuntimeException( t );
			}	 
		}

	private static String cook(String raw) 
		{
		return raw
			.replaceAll( "\"/images", "\"/elda/images" )
			.replaceAll( "\"/css", "\"/elda/css" )
			.replaceAll( "\"/scripts", "\"/elda/scripts" )
			;
		}
	}
