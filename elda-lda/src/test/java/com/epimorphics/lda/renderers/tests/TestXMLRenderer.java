/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
*/
package com.epimorphics.lda.renderers.tests;

import static org.junit.Assert.fail;

import java.util.HashMap;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.epimorphics.lda.core.APIResultSet.MergedModels;
import com.epimorphics.lda.renderers.XMLRenderer;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.support.Times;
import com.epimorphics.lda.tests.SNS;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.util.DOMUtils;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.shared.PrefixMapping;

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
		ensureWrappedRendering( "(P href=eh:/b))", resourceInModel( "a P b" ) );
		}
	
	protected String wrapA( String lispy ) 
		{ return wrap( "eh:/a", lispy ); }
	
	protected String wrap( String x, String lispy ) 
		{
		return "(items (item href=X Y))".replaceAll( "X", x ).replaceAll( "Y", lispy );
		}
	
	@Test public void testSimpleChain() 
		{
		ensureWrappedRendering( "(P href=eh:/b (Q href=eh:/c))", resourceInModel( "a P b; b Q c" ) );
		}
	
	@Test public void testSingleDataStatement()
		{
		ensureWrappedRendering( "(P 'b')", resourceInModel( "a P 'b'" ) );
		}
	
	@Test public void testSingleDataStatementWithLanguage()
		{
		ensureWrappedRendering( "(P lang=en-uk 'b')", resourceInModel( "a P 'b'en-uk" ) );
		}
	
	@Test public void testSingleDataStatementWithType()
		{
		ensureWrappedRendering( "(P datatype=string 'b')", resourceInModel( "a P 'b'xsd:string" ) );
		}
	
	@Test public void testSortingByPredicate()
		{
		// FRAGILE. The test may succeed even if value-sorting doesn't work, if the
		// order that statements come out of the model in has magically sorted. Hence
		// the choice of 'b' and 'aa' and their order of appearance in the model string.
		// not sure how to improve this without arranging a pipeline through to the
		// renderer.
		ensureRendering( wrap("eh:/root", "(R href=eh:/a (P (item 'aa') (item 'b')))" ), resourceInModel( "root R a; a P 'b'; a P 'aa'" ) );
		ensureWrappedRendering( "(P datatype=string 'b')", resourceInModel( "a P 'b'xsd:string" ) );
		}
	
	/*
	    Test that a shared graph node is unpacked at all (here, both) of its occurrences,
	    rather than at only the first.
	*/
	@Test public void testUnpackingRepeatedResources()
		{
		String unwrapped = "(R href=eh:/a (P (item href=eh:/b (HAS href=eh:/value)) (item href=eh:/c (Q href=eh:/b (HAS href=eh:/value)))))";
		ensureRendering
			( wrap( "eh:/root", unwrapped )
			, resourceInModel( "root R a; a P b; b HAS value; a P c; c Q b" )
			);
		}
	
	@Test public void testRootWithSingletonList()
		{
		ensureWrappedRendering
			( "(P (item href=eh:/A))", 
			resourceInModel( "a P _b; _b rdf:first A; _b rdf:rest rdf:nil" ) 
			);
		}
	
	@Test public void testRootWithDoubletonList()
		{
		ensureWrappedRendering
			( "(P (item href=eh:/A) (item href=eh:/B))", 
			resourceInModel( "a P _b; _b rdf:first A; _b rdf:rest _c; _c rdf:first B; _c rdf:rest rdf:nil" ) 
			);
		}

    protected Resource resourceInModel( String string )
        {
        Model m = ModelTestBase.modelWithStatements( string );
        String firstResourceString = string.substring( 0, string.indexOf( ' ' ) );
		Resource r = ModelTestBase.resource( m, firstResourceString );
        return r.inModel( m );        
        }
    
	private void ensureWrappedRendering( String desired, Resource root ) 
		{ ensureRendering( wrapA( desired ), root ); }
	
	private void ensureRendering( String wrapped, Resource root ) {
		Model m = root.getModel();	
	//
		PrefixMapping pm = root.getModel();
		ShortnameService sns = new SNS("P=eh:/P; Q=eh:/Q; R=eh:/R; HAS=eh:/HAS" );
	//
		XMLRenderer xr = new XMLRenderer( sns );
		Document d = DOMUtils.newDocument();
		MergedModels mm = new MergedModels( m );
	//
		Model meta = mm.getMetaModel();
		meta.add( root, API.items, meta.createList( new RDFNode[] {root} ) );
		xr.renderInto( root.inModel( meta ), mm, d, new HashMap<String, String>() );
	//
		Node de = d.getDocumentElement().getFirstChild();
		Node expected = new TinyParser().parse( wrapped );
	//
		if (!de.isEqualNode( expected )) 
			{
			String exp = DOMUtils.renderNodeToString( new Times(), expected, pm );
			String obt = DOMUtils.renderNodeToString( new Times(), de, pm );
			fail( "ALAS -- rendering not as expected:\n" + exp + "obtained:\n" + obt );
			}
		}
	
	}
