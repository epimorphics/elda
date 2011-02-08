/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
*/
package com.epimorphics.lda.renderers.tests;

import static org.junit.Assert.*;

import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Test;

import com.epimorphics.lda.renderers.XMLRenderer;
import com.epimorphics.lda.renderers.XMLRenderer.As;
import com.epimorphics.lda.renderers.tests.TestXMLRenderer.Tokens.Type;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.tests_support.ShortnameFake;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class TestXMLRenderer 
	{
	static class Tokens 
		{
		String spelling;
		Type type;
		String source;
		Matcher m;
		
		static final Pattern p = Pattern.compile( "\\(|\\)|'[^']*'|[^()=' ]+=[^()=' ]+|[^()=' ]+|." );

		enum Type {WRONG, EOF, LPAR, RPAR, LIT, WORD, ATTR}
		
		public Tokens( String source ) 
			{
			this.source = source;
			this.m = p.matcher( source );
			advance();
			}
		
		public void advance()
			{
			if (m.find()) 
				{
				spelling = m.group();
//				System.err.println( ">> SPELLING: " + spelling );
				if (spelling.equals( " "))
					advance();
				else
					type =
						spelling.equals( "(" ) ? Type.LPAR
						: spelling.equals( ")" ) ? Type.RPAR
						: spelling.startsWith( "'" ) ? Type.LIT
						: spelling.contains( "=" ) ? Type.ATTR
						: Type.WORD
						;
				}
			else
				{
				spelling = "";
				type = Type.EOF;
				}
			// System.err.println( ">> advanced to " + type + " \"" + spelling + "\"" );
			}

		public void demand( Type t ) 
			{
			if (type == t) advance();
			else throw new RuntimeException( "expected a " + t + " but got: " + type + " " + spelling );
			}
		}
	
	protected Node parse( String source ) 
		{
		Document d = getBuilder().newDocument();
		Node result = parse( d, new Tokens( source ) );
//		System.err.println( "-->> " + docToString( result ) );
		return result;		
		}

	public String nodeToString( Node d ) {
		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer t = tf.newTransformer();
			t.setOutputProperty( OutputKeys.INDENT, "yes" );
			t.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "2" );
			DOMSource ds = new DOMSource( d );
			StringWriter sw = new StringWriter();
			StreamResult sr = new StreamResult( sw );
			t.transform( ds, sr );
			return sw.toString();
		} catch (Throwable t) {
			throw new RuntimeException( t );
		} 
	}
	
	private Node parse( Document d, Tokens t ) 
		{
		if (t.type == Tokens.Type.LPAR)
			{
			t.advance();
			String tag = parseWord( d, t );
			Element e = d.createElement( tag );
			while (true)
				{
				switch (t.type)
					{
					case ATTR:
						int eq = t.spelling.indexOf( '=' );
						String name = t.spelling.substring( 0, eq );
						String value = t.spelling.substring( eq + 1 );
						e.setAttribute( name, value );
						t.advance();
						break;
						
					case LPAR:
					case LIT:
						e.appendChild( parse( d, t ) );
						break;
						
					case RPAR:
						t.advance();
						return e;
						
					default: 
						throw new RuntimeException( "Not allowed in element: " + t.type + " " + t.spelling );
					}
				}
			}
		else if (t.type == Tokens.Type.LIT)
			{
			try { return d.createTextNode( t.spelling ); } finally { t.advance(); }
			}
		else
			throw new RuntimeException( "OOPS -- bad token for parse: " + t.type + " " + t.spelling );
		}

	private String parseWord(Document d, Tokens t)
		{
		if (t.type == Tokens.Type.WORD)
			{
			try { return t.spelling; } finally { t.advance(); }
			}
		else
			throw new RuntimeException( "tag following LPAR must be WORD: " + t.type + " " + t.spelling );
		}

	@Test public void testParser() 
		{
//		System.err.println( ">> " + parse( "'hello'" ) );
//		System.err.println( ">> " + parse( "(hello)" ) );
//		System.err.println( ">> " + parse( "(hello 'there')" ) );
//		System.err.println( ">> " + parse( "(hello 'there' 'lovelies')" ) );
//		System.err.println( ">> " + parse( "(hello (there))" ) );
//		System.err.println( ">> " + parse( "(hello (there) (lovelies))" ) );
//		System.err.println( ">> " + parse( "(hello (there) 'my' (e (lovelies)))" ) );
//		System.err.println( ">> " + parse( "(hello href=spoo)" ) );
		}
	
	@Test public void testSketch() 
		{
		Model m = ModelTestBase.modelWithStatements( "a P b" );
		Resource root = m.createResource( "eh:/a" );
		ShortnameService sns = new ShortnameFake() 
			{
			@Override public String shorten( String uri ) 
				{ return uri.replaceAll( "^.*/", "" ); }
			};
		XMLRenderer xr = new XMLRenderer( sns, As.XML );
		Document d = getBuilder().newDocument();
		xr.renderInto( root, d );
		Node de = d.getDocumentElement();
		Node expected = parse( "(result format=linked-data-api version=0.2 href=eh:/a (P href=eh:/b))" );
		if (!de.isEqualNode( expected )) 
			{
			System.err.println( "expected: " + nodeToString( expected ) );
			System.err.println( "obtained: " + nodeToString( de ) );
			fail( "ALAS" );
			}
		}
	
	private static DocumentBuilder getBuilder() 
		{
		try 
			{
			return DocumentBuilderFactory.newInstance().newDocumentBuilder();
			} 
		catch (ParserConfigurationException e) 
			{
			throw new RuntimeException( e );
			}
		}
	
	}
