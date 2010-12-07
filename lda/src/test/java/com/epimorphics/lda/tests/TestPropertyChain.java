/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) copyright Epimorphics Limited 2010
    $Id$
*/

package com.epimorphics.lda.tests;

import org.junit.Test;

import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.support.PropertyChain;
import com.epimorphics.lda.tests_support.ExpandOnly;
import com.hp.hpl.jena.rdf.model.*;
import com.epimorphics.lda.core.ChainScanner;

import static com.hp.hpl.jena.rdf.model.test.ModelTestBase.*;

public class TestPropertyChain 
	{
	@Test public void testA()
		{
		testPropertyChain("s P a", "s P a; a Q b", "eh:/s", "P");
		}
	
	@Test public void testB()
		{
		testPropertyChain("s P a; a Q b", "s P a; a Q b; b R c; b R d", "eh:/s", "P.Q");
		}
	
//	@Test public void testC()
//		{
//		testPropertyChain("s P a; a Q b; b R c; b R d", "s P a; a Q b; b R c; b R d", "eh:/s", "P.Q");
//		}
//	
//	@Test public void testD()
//		{
//		testPropertyChain("s P a; a Q b; b R c; b R d", "s P a; a Q b; b R c; b R d", "eh:/s", "P.Q");
//		}

	private void testPropertyChain(String pExpected, String pOriginal, String pRoot, String pChain) 
		{
		ShortnameService sns = new ExpandOnly( "*=eh:/*" );
		Resource root = modelWithStatements( pOriginal ).createResource( pRoot );
		Model obtained = ChainScanner.extract( root, createChain( sns, pChain ), false );
		assertIsoModels( modelWithStatements( pExpected ), obtained );
		}

	public static PropertyChain createChain( ShortnameService sns, String shortNames ) 
		{
		return new PropertyChain( ShortnameService.Util.expandProperties( shortNames, sns ) );
		}
	}
