/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
*/
package com.epimorphics.util;

import java.io.StringWriter;
import java.net.URL;
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

import com.epimorphics.lda.bindings.VarValues;
import com.epimorphics.lda.renderers.RendererContext;
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

	public static Transformer getTransformer( Mode a, URL u ) 
		throws TransformerConfigurationException, TransformerFactoryConfigurationError 
		{
		TransformerFactory tf = TransformerFactory.newInstance();
		if (a == Mode.AS_IS) 
			return tf.newTransformer();
		else 
			{
			Source s = new StreamSource( u.toExternalForm() );
			return tf.newTransformer( s );
			}
		}

	public static String nodeToIndentedString( Node d, PrefixMapping pm, Mode as ) 
		{
		if (as == Mode.TRANSFORM)
			throw new RuntimeException( "Mode.TRANSFORM requested, but no filepath given." );
		return nodeToIndentedString( d, new RendererContext(), pm, as, "SHOULD_NOT_OPEN_THIS_FILEPATH" );
		}
	
	public static String nodeToIndentedString( Node d, RendererContext rc, PrefixMapping pm, Mode as, String transformFilePath ) 
		{
		try {
			URL u = expandStylesheetName( rc, transformFilePath );
			System.err.println( ">> expanded stylesheet name " + transformFilePath + " to " + u );
			Transformer t = setPropertiesAndParams(  rc, pm, as, u );
			StringWriter sw = new StringWriter();
			StreamResult sr = new StreamResult( sw );
			t.transform( new DOMSource( d ), sr );
			String raw = sw.toString();
			return as == Mode.AS_IS ? raw : raw.replaceAll( "\"_ROOT", "\"" + rc.getContextPath() );
			} 
		catch (Throwable t) 
			{
			throw new RuntimeException( t );
			}	 
		}

	private static URL expandStylesheetName( RendererContext rc, String path ) 
		{
		String ePath = VarValues.expandVariables(rc, path);
		return rc.pathAsURL( ePath );
		}

	private static Transformer setPropertiesAndParams( RendererContext p, PrefixMapping pm, Mode as, URL u ) 
		throws TransformerConfigurationException, TransformerFactoryConfigurationError 
		{
		Transformer t = getTransformer( as, u );
		t.setOutputProperty( OutputKeys.INDENT, "yes" );
		t.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "2" );
		for (String name: p.keySet()) t.setParameter( name, p.getStringValue( name ) );
		t.setParameter( "api:namespaces", namespacesDocument( pm ) );
		return t;
		}

	private static String namespacesDocument( PrefixMapping pm ) 
		{
		StringBuilder sb = new StringBuilder();
		sb.append( "<namespaces>\n" );
		for (Map.Entry<String, String> e: pm.getNsPrefixMap().entrySet()) 
			{
			sb.append( "<namespace prefix='" );
			sb.append( e.getKey() );
			sb.append( "'>" );
			sb.append( e.getValue() );
			sb.append( "</namespace>\n" );
			}
		sb.append( "</namespaces>\n" );
		return sb.toString();
		}
	}
