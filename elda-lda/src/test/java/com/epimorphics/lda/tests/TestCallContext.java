/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.support.MultiMap;
import com.epimorphics.lda.tests_support.MakeData;
import com.epimorphics.lda.tests_support.Matchers;
import com.hp.hpl.jena.test.JenaTestBase;

public class TestCallContext 
	{	
	@Test public void ensureContextCreatedEmptyIsEmpty()
		{
		MultiMap<String, String> map = MakeData.parseQueryString("");
		Bindings cc = Bindings.createContext( MakeData.variables( "" ), map );
		assertThat( cc.parameterNames(), Matchers.isEmpty() );
//		assertThat( cc.getUriInfo(), sameInstance(ui) );
		}
	
	@Test public void ensureContextRecallsParameterNames()
		{
		MultiMap<String, String> map = MakeData.parseQueryString("spoo=fresh&space=cold");
		Bindings cc = Bindings.createContext( MakeData.variables( "" ), map );
		assertThat( cc.parameterNames(), is( JenaTestBase.setOfStrings( "spoo space" ) ) );
//		assertThat( cc.getUriInfo(), sameInstance(ui) );
		}
	
	@Test public void ensureContextRecallsBindingNames()
		{
		MultiMap<String, String> map = MakeData.parseQueryString("");
		Bindings cc = Bindings.createContext( MakeData.variables( "a=b c=d" ), map );
		assertThat( cc.parameterNames(), Matchers.isEmpty() );
//		assertThat( cc.getUriInfo(), sameInstance(ui) );
		assertThat( cc.getValueString( "a" ), is( "b" ) );
		assertThat( cc.getValueString( "c" ), is( "d" ) );
		}
	
	@Test public void ensureContextGetsAppropriateValues()
		{
		MultiMap<String, String> map = MakeData.parseQueryString("p1=v1&p2=v2");
		Bindings cc = Bindings.createContext( MakeData.variables( "x=y" ), map );
		assertThat( cc.parameterNames(), is( JenaTestBase.setOfStrings( "p1 p2" ) ) );
//		assertThat( cc.getUriInfo(), sameInstance(ui) );
		assertThat( cc.getValueString( "x" ), is( "y" ) );
		assertThat( cc.getValueString( "p1" ), is( "v1" ) );
		}
	
	@Test public void ensureCopyingConstructorPreservesValues()
		{
		MultiMap<String, String> map = MakeData.parseQueryString( "p1=v1&p2=v2" );
		Bindings base = Bindings.createContext( MakeData.variables( "" ), map );
		Bindings cc = base.copyWithDefaults( MakeData.variables( "fly=fishing" ) );
//		assertThat( cc.getUriInfo(), is( base.getUriInfo() ) );
		assertThat( cc.getValueString( "fly" ), is( "fishing" ) );
//		assertThat( cc.getMediaSuffix(), is( mediaSuffix ) );
		}
	}
