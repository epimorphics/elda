/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
*/
package com.epimorphics.lda.renderers.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.epimorphics.lda.renderers.XMLRenderer;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.tests_support.ShortnameFake;
import com.epimorphics.util.DOMUtils;
import com.epimorphics.util.DOMUtils.As;
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
			{ return uri.replaceAll( "^.*[/#]", "" ); }
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
	
	@Test public void testSingleStatement() 
		{
		ensureRendering( "(P href=eh:/b)", resourceInModel( "a P b" ) );
		}
	
	@Test public void testSimpleChain() 
		{
		ensureRendering( "(P (item href=eh:/b (Q href=eh:/c)))", resourceInModel( "a P b; b Q c" ) );
		}
	
	@Test public void testSingleDataStatement()
		{
		ensureRendering( "(P 'b')", resourceInModel( "a P 'b'" ) );
		}
	
	@Test public void testSingleDataStatementWithLanguage()
		{
		ensureRendering( "(P lang=en-uk 'b')", resourceInModel( "a P 'b'en-uk" ) );
		}
	
	@Test public void testSingleDataStatementWithType()
		{
		ensureRendering( "(P datatype=string 'b')", resourceInModel( "a P 'b'xsd:string" ) );
		}
	
	@Test public void testRootWithSingletonList()
		{
		ensureRendering
			( "(P (item href=eh:/A))", 
			resourceInModel( "a P _b; _b rdf:first A; _b rdf:rest rdf:nil" ) 
			);
		}
	
	@Test public void testRootWithDoubletonList()
		{
		ensureRendering
			( "(P (item href=eh:/A) (item href=eh:/B))", 
			resourceInModel( "a P _b; _b rdf:first A; _b rdf:rest _c; _c rdf:first B; _c rdf:rest rdf:nil" ) 
			);
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
			System.err.println( "expected: " + DOMUtils.nodeToIndentedString( expected, As.XML ) );
			System.err.println( "obtained: " + DOMUtils.nodeToIndentedString( de, As.XML ) );
			fail( "ALAS -- rendering not as expected." );
			}
		}
	
	}
