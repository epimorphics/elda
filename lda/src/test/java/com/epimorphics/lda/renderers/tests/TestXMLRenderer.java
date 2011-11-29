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
import com.epimorphics.lda.shortnames.StandardShortnameService;
import com.epimorphics.lda.support.Times;
import com.epimorphics.util.DOMUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.shared.PrefixMapping;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class TestXMLRenderer 
	{	
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
		ensureRendering( "(P href=eh:/b (Q href=eh:/c))", resourceInModel( "a P b; b Q c" ) );
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
	
	@Test public void testSortingByPredicate()
		{
		// FRAGILE. The test may succeed even if value-sorting doesn't work, if the
		// order that statements come out of the model in has magically sorted. Hence
		// the choice of 'b' and 'aa' and their order of appearance in the model string.
		// not sure how to improve this without arranging a pipeline through to the
		// renderer.
		ensureRendering( "(R href=eh:/a (P (item 'aa') (item 'b')))", resourceInModel( "root R a; a P 'b'; a P 'aa'" ) );
//		ensureRendering( "(P datatype=string 'b')", resourceInModel( "a P 'b'xsd:string" ) );
		}
	
	/*
	    Test that a shared graph node is unpacked at all (here, both) of its occurrences,
	    rather than at only the first.
	*/
	@Test public void testUnpackingRepeatedResources()
		{
		ensureRendering
			( "(R href=eh:/a (P (item href=eh:/b (HAS href=eh:/value)) (item href=eh:/c (Q href=eh:/b (HAS href=eh:/value)))))"
			, resourceInModel( "root R a; a P b; b HAS value; a P c; c Q b" )
			);
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
		PrefixMapping pm = root.getModel();
		ShortnameService sns = new StandardShortnameService();
		XMLRenderer xr = new XMLRenderer( sns );
		Document d = DOMUtils.newDocument();
		xr.renderInto( root, d, false, false );
		Node de = d.getDocumentElement().getFirstChild();
		Node expected = new TinyParser().parse( desired );
		if (!de.isEqualNode( expected )) 
			{
			String exp = DOMUtils.renderNodeToString( new Times(), expected, pm );
			String obt = DOMUtils.renderNodeToString( new Times(), de, pm );
//			System.err.println( "expected:\n" + exp );
//			System.err.println( "obtained:\n" + obt );
			fail( "ALAS -- rendering not as expected:\n" + exp + "obtained:\n" + obt );
			}
		}
	
	}
