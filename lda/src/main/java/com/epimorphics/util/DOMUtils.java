/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
*/
package com.epimorphics.util;

import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.epimorphics.lda.bindings.VarValues;
import com.epimorphics.lda.renderers.RendererContext;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.WrappedException;

/**
    Handles XSLT rewrites for HTML and indented-string display
    of XML.
    
 	@author chris
*/
public class DOMUtils 
	{	
	public static Document newDocument() 
		{ return getBuilder().newDocument(); }
	
	public static String renderNodeToString( Node d, PrefixMapping pm ) 
		{ return renderNodeToString( d, new RendererContext(), pm, null ); }
	
	public static String renderNodeToString( Node d, RendererContext rc, PrefixMapping pm, String transformFilePath ) 
		{
		Transformer t = setPropertiesAndParams(  rc, pm, transformFilePath );
		StringWriter sw = new StringWriter();
		StreamResult sr = new StreamResult( sw );
		try { t.transform( new DOMSource( d ), sr ); } 
		catch (TransformerException e) { throw new WrappedException( e ); }
		return sw.toString();
		}
    
    static Logger log = LoggerFactory.getLogger(DOMUtils.class);

	private static Transformer setPropertiesAndParams( RendererContext rc, PrefixMapping pm, String transformFilePath ) 
		{
		Transformer t = getTransformer( rc, transformFilePath );
		t.setOutputProperty( OutputKeys.INDENT, "yes" );
		t.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "2" );
		for (String name: rc.keySet()) 
			{
			String value = rc.getStringValue( name );
			t.setParameter( name, value );
			log.debug( "set xslt parameter " + name + " = " + value );
			}
		String nsd = namespacesDocument( pm );
		t.setParameter( "api:namespaces", nsd );
		// log.debug( "set xslt parameter api:namespaces = " + nsd );
		return t;
		}
	
	private static DocumentBuilder getBuilder() 
		{
		try  { return DocumentBuilderFactory.newInstance().newDocumentBuilder(); } 
		catch (ParserConfigurationException e) { throw new WrappedException( e ); }
		}
	
	protected static HashMap<URL, Templates> cache = new HashMap<URL, Templates>();
	
	private static Transformer getTransformer( RendererContext rc, String transformFilePath ) 
		{
		try
			{
			TransformerFactory tf = TransformerFactory.newInstance();
			if (transformFilePath == null) 
				return tf.newTransformer();
			else 
				{
				URL u = rc.pathAsURL( VarValues.expandVariables( rc, transformFilePath ) );
				Templates t = cache.get( u );
				if (t == null) {
					long origin = System.currentTimeMillis();
					t = tf.newTemplates( new StreamSource( u.toExternalForm() ) );
					long after = System.currentTimeMillis();
					log.debug( "TIMING: compile stylesheet " + transformFilePath + " " + (after - origin)/1000.0 + "s" );
					cache.put( u, t );
				}
				return t.newTransformer();
				}
			}
		catch (TransformerConfigurationException e) 
			{ throw new WrappedException( e ); }
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
