/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.tests;

import static org.junit.Assert.*;

import static org.hamcrest.CoreMatchers.*;
import org.junit.Test;

import com.epimorphics.lda.core.CallContext;
import com.epimorphics.lda.core.MultiMap;
import com.epimorphics.lda.tests_support.MakeData;
import com.epimorphics.lda.tests_support.Matchers;
import com.hp.hpl.jena.test.JenaTestBase;

public class TestCallContext 
	{	
	@Test public void ensureContextCreatedEmptyIsEmpty()
		{
		MultiMap<String, String> map = MakeData.parseQueryString("");
		CallContext cc = CallContext.createContext( map, MakeData.variables( "" ) );
		assertThat( cc.getFilterPropertyNames(), Matchers.isEmpty() );
//		assertThat( cc.getUriInfo(), sameInstance(ui) );
		}
	
	@Test public void ensureContextRecallsParameterNames()
		{
		MultiMap<String, String> map = MakeData.parseQueryString("spoo=fresh&space=cold");
		CallContext cc = CallContext.createContext( map, MakeData.variables( "" ) );
		assertThat( cc.getFilterPropertyNames(), is( JenaTestBase.setOfStrings( "spoo space" ) ) );
//		assertThat( cc.getUriInfo(), sameInstance(ui) );
		}
	
	@Test public void ensureContextRecallsBindingNames()
		{
		MultiMap<String, String> map = MakeData.parseQueryString("");
		CallContext cc = CallContext.createContext( map, MakeData.variables( "a=b c=d" ) );
		assertThat( cc.getFilterPropertyNames(), Matchers.isEmpty() );
//		assertThat( cc.getUriInfo(), sameInstance(ui) );
		assertThat( cc.getStringValue( "a" ), is( "b" ) );
		assertThat( cc.getStringValue( "c" ), is( "d" ) );
		}
	
	@Test public void ensureContextGetsAppropriateValues()
		{
		MultiMap<String, String> map = MakeData.parseQueryString("p1=v1&p2=v2");
		CallContext cc = CallContext.createContext( map, MakeData.variables( "x=y" ) );
		assertThat( cc.getFilterPropertyNames(), is( JenaTestBase.setOfStrings( "p1 p2" ) ) );
//		assertThat( cc.getUriInfo(), sameInstance(ui) );
		assertThat( cc.getStringValue( "x" ), is( "y" ) );
		assertThat( cc.getStringValue( "p1" ), is( "v1" ) );
		}
	
	@Test public void ensureCopyingConstructorPreservesValues()
		{
		MultiMap<String, String> map = MakeData.parseQueryString( "p1=v1&p2=v2" );
		CallContext base = CallContext.createContext( map, MakeData.variables( "" ) );
		CallContext cc = new CallContext( MakeData.variables( "fly=fishing" ), base );
//		assertThat( cc.getUriInfo(), is( base.getUriInfo() ) );
		assertThat( cc.getStringValue( "fly" ), is( "fishing" ) );
//		assertThat( cc.getMediaSuffix(), is( mediaSuffix ) );
		}
	}
