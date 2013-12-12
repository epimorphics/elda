/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.tests;

import static com.epimorphics.util.CollectionUtils.list;
import static com.hp.hpl.jena.rdf.model.test.ModelTestBase.property;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Ignore;
import org.junit.Test;

import com.epimorphics.lda.core.VarSupply;
import com.epimorphics.lda.rdfq.RDFQ;
import com.epimorphics.lda.rdfq.Variable;
import com.epimorphics.lda.support.PropertyChain;
import com.epimorphics.lda.support.PropertyChainTranslator;

public class TestPropertyChainToSPARQLTranslator 
	{
	@Test @Ignore public void ensureNoChainsGeneratesEmptyOptionals()
		{
		PropertyChainTranslator t = new PropertyChainTranslator();
		String generated = t.translate( new VarSupplyByCount(), false );
		assertThat( generated, is( "" ) );
		}
	
	@Test @Ignore public void ensureSingleOneElementChainGeneratesOneOptional()
		{
		PropertyChain P = new PropertyChain( list( property( "P" ) ) ); 
		PropertyChainTranslator t = new PropertyChainTranslator( P );
		String generated = t.translate( new VarSupplyByCount(), false );
		assertThat( generated, is( "\nOPTIONAL { ?item <eh:/P> ?v1 . }" ) );
		}

	@Test @Ignore public void ensureMultipleElementChainGeneratesNestedOptional()
		{
		PropertyChain PQ = new PropertyChain( list( property( "P" ), property( "Q" ) ) ); 
		PropertyChainTranslator t = new PropertyChainTranslator( PQ );
		String generated = t.translate( new VarSupplyByCount(), "meti", false );
		assertThat( generated, is( "\nOPTIONAL { ?meti <eh:/P> ?v1 .\nOPTIONAL { ?v1 <eh:/Q> ?v2 . } }" ) );
		}

	@Test @Ignore public void ensureMultiplePropertyChainsGenerateSeparateOptions()
		{
		PropertyChain P = new PropertyChain( list( property( "P" ) ) ); 
		PropertyChain QR = new PropertyChain( list( property( "Q" ), property( "R" ) ) ); 
		PropertyChainTranslator t = new PropertyChainTranslator( P, QR );
		String generated = t.translate( new VarSupplyByCount(), "X", false );
		assertThat( generated, is( "\nOPTIONAL { ?X <eh:/P> ?v1 . }\nOPTIONAL { ?X <eh:/Q> ?v2 .\nOPTIONAL { ?v2 <eh:/R> ?v3 . } }" ) );
		}
	
	static class VarSupplyByCount implements VarSupply
		{
		int count = 0;
		
		@Override public Variable newVar() 
			{ return RDFQ.var( "v" + ++count ); }
		}
	}
