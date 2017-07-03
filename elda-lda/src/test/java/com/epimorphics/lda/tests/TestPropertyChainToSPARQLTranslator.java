/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.tests;

import static com.epimorphics.util.CollectionUtils.list;
import static org.apache.jena.rdf.model.test.ModelTestBase.property;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.epimorphics.lda.core.VarSupply;
import com.epimorphics.lda.rdfq.RDFQ;
import com.epimorphics.lda.rdfq.Variable;
import com.epimorphics.lda.support.PropertyChain;
import com.epimorphics.lda.support.PropertyChainTranslator;

public class TestPropertyChainToSPARQLTranslator 
	{
	@Test public void ensureNoChainsGeneratesEmptyOptionals()
		{
		PropertyChainTranslator t = new PropertyChainTranslator();
		String generated = t.translate( new VarSupplyByCount(), false );
		assertThat( generated, is( "" ) );
		}
	
	@Test public void ensureSingleOneElementChainGeneratesOneOptional()
		{
		PropertyChain P = new PropertyChain( list( property( "P" ) ) ); 
		PropertyChainTranslator t = new PropertyChainTranslator( P );
		String generated = t.translate( new VarSupplyByCount(), false );
		assertThat( generated, is( "{ {}\nUNION { ?item <eh:/P> ?v1}}" ) );
		}

	@Test public void ensureMultipleElementChainGeneratesNestedOptional()
		{
		PropertyChain PQ = new PropertyChain( list( property( "P" ), property( "Q" ) ) ); 
		PropertyChainTranslator t = new PropertyChainTranslator( PQ );
		String generated = t.translate( new VarSupplyByCount(), "meti", false );
		assertThat( generated, is( "{ {}\nUNION { ?meti <eh:/P> ?v1\nOPTIONAL { ?v1 <eh:/Q> ?v2 . }}}" ) );
		}

	@Test public void ensureMultiplePropertyChainsGenerateSeparateOptions()
		{
		PropertyChain P = new PropertyChain( list( property( "P" ) ) ); 
		PropertyChain QR = new PropertyChain( list( property( "Q" ), property( "R" ) ) ); 
		PropertyChainTranslator t = new PropertyChainTranslator( P, QR );
		String generated = t.translate( new VarSupplyByCount(), "X", false );
		assertThat( generated, is( "{ {}\nUNION { ?X <eh:/P> ?v1}UNION { ?X <eh:/Q> ?v2\nOPTIONAL { ?v2 <eh:/R> ?v1 . }}}" ) );
		}
	
	static class VarSupplyByCount implements VarSupply
		{
		int count = 0;
		
		@Override public Variable newVar() 
			{ return RDFQ.var( "v" + ++count ); }
		}
	}
