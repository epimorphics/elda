/*
    See lda-top/LICENCE for the licence for this software.
    (c) copyright Epimorphics Limited 2010
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
		CallContext cc = CallContext.createContext( ui, MakeData.hashMap( "" ) );
		assertThat( cc.getFilterPropertyNames(), Matchers.isEmpty() );
		assertThat( cc.getUriInfo(), sameInstance(ui) );
		}
	
	@Test public void ensureContextRecallsParameterNames()
		{
		MultivaluedMap<String, String> map = MultiValuedMapSupport.parseQueryString("spoo=fresh&space=cold");
		UriInfo ui = new APITesterUriInfo( "eh:/spoo", map );
		CallContext cc = CallContext.createContext( ui, MakeData.hashMap( "" ) );
		assertThat( cc.getFilterPropertyNames(), is( JenaTestBase.setOfStrings( "spoo space" ) ) );
		assertThat( cc.getUriInfo(), sameInstance(ui) );
		}
	
	@Test public void ensureContextRecallsBindingNames()
		{
		MultivaluedMap<String, String> map = MultiValuedMapSupport.parseQueryString("");
		UriInfo ui = new APITesterUriInfo( "eh:/spoo", map );
		CallContext cc = CallContext.createContext( ui, MakeData.hashMap( "a=b c=d" ) );
		assertThat( cc.getFilterPropertyNames(), Matchers.isEmpty() );
		assertThat( cc.getUriInfo(), sameInstance(ui) );
		assertThat( cc.getParameterValue( "a" ), is( "b" ) );
		assertThat( cc.getParameterValue( "c" ), is( "d" ) );
		}
	
	@Test public void ensureContextGetsAppropriateValues()
		{
		MultivaluedMap<String, String> map = MultiValuedMapSupport.parseQueryString("p1=v1&p2=v2");
		UriInfo ui = new APITesterUriInfo( "eh:/spoo", map );
		CallContext cc = CallContext.createContext( ui, MakeData.hashMap( "x=y" ) );
		assertThat( cc.getFilterPropertyNames(), is( JenaTestBase.setOfStrings( "p1 p2" ) ) );
		assertThat( cc.getUriInfo(), sameInstance(ui) );
		assertThat( cc.getParameterValue( "x" ), is( "y" ) );
		assertThat( cc.getParameterValue( "p1" ), is( "v1" ) );
		}
	
	@Test public void ensureCopyingConstructorePreservesValues()
		{
		MultivaluedMap<String, String> map = MultiValuedMapSupport.parseQueryString( "p1=v1&p2=v2" );
		UriInfo ui = new APITesterUriInfo( "eh:/spoo", map );
		CallContext base = CallContext.createContext( ui, MakeData.hashMap( "" ) );
		CallContext cc = new CallContext( MakeData.hashMap( "fly=fishing" ), base );
		assertThat( cc.getUriInfo(), is( base.getUriInfo() ) );
		assertThat( cc.getParameterValue( "fly" ), is( "fishing" ) );
		}
	}
