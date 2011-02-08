/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
*/
package com.epimorphics.lda.renderers.tests;

import static org.junit.Assert.*;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Test;

import com.epimorphics.lda.renderers.XMLRenderer;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.tests_support.ShortnameFake;
import com.epimorphics.util.DOMUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class TestXMLRenderer 
	{
	private static class ForceShorten extends ShortnameFake 
		{
		@Override public String shorten( String uri ) 
			{ return uri.replaceAll( "^.*/", "" ); }
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
		ensureRendering( "(P href=eh:/b)", resourceInModel( "a P b" ) );
		}

    protected Resource resourceInModel( String string )
        {
        Model m = ModelTestBase.modelWithStatements( string );
        Resource r = ModelTestBase.resource( m, string.substring( 0, string.indexOf( ' ' ) ) );
        return r.inModel( m );        
        }
    
	private void ensureRendering( String desired, Resource root ) 
		{
		ShortnameService sns = new ForceShorten();
		XMLRenderer xr = new XMLRenderer( sns, DOMUtils.As.XML );
		Document d = DOMUtils.newDocument();
		xr.renderInto( root, d );
		Node de = d.getDocumentElement().getFirstChild();
		Node expected = new TinyParser().parse( desired );
		if (!de.isEqualNode( expected )) 
			{
			System.err.println( "expected: " + nodeToString( expected ) );
			System.err.println( "obtained: " + nodeToString( de ) );
			fail( "ALAS -- rendering not as expected." );
			}
		}
	
	}
