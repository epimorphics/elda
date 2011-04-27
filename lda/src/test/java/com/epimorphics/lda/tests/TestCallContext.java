/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.tests;

import static org.junit.Assert.*;


import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import static org.hamcrest.CoreMatchers.*;
import org.junit.Test;

import com.epimorphics.lda.core.CallContext;
import com.epimorphics.lda.support.MultiValuedMapSupport;
import com.epimorphics.lda.tests_support.MakeData;
import com.epimorphics.lda.tests_support.Matchers;
import com.hp.hpl.jena.test.JenaTestBase;

public class TestCallContext 
	{
	@Test public void ensureContextCreatedEmptyIsEmpty()
		{
		MultivaluedMap<String, String> map = MultiValuedMapSupport.parseQueryString("");
		UriInfo ui = new APITesterUriInfo( "eh:/spoo", map );
		CallContext cc = CallContext.createContext( ui, MakeData.variables( "" ) );
		assertThat( cc.getFilterPropertyNames(), Matchers.isEmpty() );
//		assertThat( cc.getUriInfo(), sameInstance(ui) );
		}
	
//	@Test public void ensureContextStoresMediaSuffix()
//		{
//		ensureContextStoresMediaSuffix( ".json" );
//		ensureContextStoresMediaSuffix( ".html" );
//		}

//	private void ensureContextStoresMediaSuffix(String suffix) 
//		{
//		MultivaluedMap<String, String> map = MultiValuedMapSupport.parseQueryString("");
//		UriInfo ui = new APITesterUriInfo( "eh:/spoo", map );
//		CallContext cc = CallContext.createContext( ui, MakeData.variables( "" ), suffix );
//		assertThat( cc.getMediaSuffix(), is( suffix ) );
//		}
	
	@Test public void ensureContextRecallsParameterNames()
		{
		MultivaluedMap<String, String> map = MultiValuedMapSupport.parseQueryString("spoo=fresh&space=cold");
		UriInfo ui = new APITesterUriInfo( "eh:/spoo", map );
		CallContext cc = CallContext.createContext( ui, MakeData.variables( "" ) );
		assertThat( cc.getFilterPropertyNames(), is( JenaTestBase.setOfStrings( "spoo space" ) ) );
//		assertThat( cc.getUriInfo(), sameInstance(ui) );
		}
	
	@Test public void ensureContextRecallsBindingNames()
		{
		MultivaluedMap<String, String> map = MultiValuedMapSupport.parseQueryString("");
		UriInfo ui = new APITesterUriInfo( "eh:/spoo", map );
		CallContext cc = CallContext.createContext( ui, MakeData.variables( "a=b c=d" ) );
		assertThat( cc.getFilterPropertyNames(), Matchers.isEmpty() );
//		assertThat( cc.getUriInfo(), sameInstance(ui) );
		assertThat( cc.getStringValue( "a" ), is( "b" ) );
		assertThat( cc.getStringValue( "c" ), is( "d" ) );
		}
	
	@Test public void ensureContextGetsAppropriateValues()
		{
		MultivaluedMap<String, String> map = MultiValuedMapSupport.parseQueryString("p1=v1&p2=v2");
		UriInfo ui = new APITesterUriInfo( "eh:/spoo", map );
		CallContext cc = CallContext.createContext( ui, MakeData.variables( "x=y" ) );
		assertThat( cc.getFilterPropertyNames(), is( JenaTestBase.setOfStrings( "p1 p2" ) ) );
//		assertThat( cc.getUriInfo(), sameInstance(ui) );
		assertThat( cc.getStringValue( "x" ), is( "y" ) );
		assertThat( cc.getStringValue( "p1" ), is( "v1" ) );
		}
	
	@Test public void ensureCopyingConstructorPreservesValues()
		{
		MultivaluedMap<String, String> map = MultiValuedMapSupport.parseQueryString( "p1=v1&p2=v2" );
		UriInfo ui = new APITesterUriInfo( "eh:/spoo", map );
		CallContext base = CallContext.createContext( ui, MakeData.variables( "" ) );
		CallContext cc = new CallContext( MakeData.variables( "fly=fishing" ), base );
//		assertThat( cc.getUriInfo(), is( base.getUriInfo() ) );
		assertThat( cc.getStringValue( "fly" ), is( "fishing" ) );
//		assertThat( cc.getMediaSuffix(), is( mediaSuffix ) );
		}
	}
