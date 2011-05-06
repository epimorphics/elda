/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.tests;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;

import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.core.APIEndpointImpl;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.core.CallContext;
import com.epimorphics.lda.core.ModelLoaderI;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.support.MultiValuedMapSupport;
import com.epimorphics.lda.tests_support.MakeData;
import com.epimorphics.util.Couple;
import com.epimorphics.util.Util;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import static com.hp.hpl.jena.rdf.model.test.ModelTestBase.*;

public class ExploreTestingForLatAndLongEtc 
	{	
	
	private final static Model latLongTestDescription = model
		( "spec rdf:type api:API" 
		+ "; spec api:sparqlEndpoint here:example"
		+ "; spec api:endPoint End"
		+ "; End rdf:type api:ListEndpoint"
		+ "; End api:uriTemplate 'http://dummy/doc/schools'"
		+ ""
		+ "; here:example has:root A; here:example has:root B; here:example has:root C; here:example has:root D"
		+ "; A geo:lat '0.5'xsd:float; A geo:long '0.5'xsd:float"
		+ "; B geo:lat '0.5'xsd:float; B geo:long '-0.5'xsd:float"
		+ "; C geo:lat '-0.5'xsd:float; C geo:long '0.5'xsd:float"
		+ "; D geo:lat '-0.5'xsd:float; D geo:long '-0.5'xsd:float"
		);

	@Test public void testFindsResourceByLatAndLong()
		{
		assertThat( resourcesFor( "near-lat=0.5 near-long=0.5 _distance=30" ), is( resourceSet( "A" ) ) );
		assertThat( resourcesFor( "near-lat=0.5 near-long=-0.5 _distance=30" ), is( resourceSet( "B" ) ) );
		assertThat( resourcesFor( "near-lat=-0.5 near-long=0.5 _distance=30" ), is( resourceSet( "C" ) ) );
		assertThat( resourcesFor( "near-lat=-0.5 near-long=-0.5 _distance=30" ), is( resourceSet( "D" ) ) );
		assertThat( resourcesFor( "near-lat=-0.1 near-long=-0.1 _distance=10" ), is( resourceSet( "" ) ) );
		assertThat( resourcesFor( "near-lat=-0.1 near-long=-0.1 _distance=100" ), is( resourceSet( "A B C D" ) ) );
		}

	private Set<Resource> resourcesFor( String settings ) 
		{
		Resource endpoint = latLongTestDescription.createResource( "eh:/End" );
		ModelLoaderI ml = new ModelLoaderI() 
			{
			@Override public Model loadModel(String uri) 
				{
				return latLongTestDescription;
				}
			};
		Resource specification = latLongTestDescription.createResource( "eh:/spec" );
		APISpec parent = new APISpec( specification, ml );
		APIEndpointSpec spec = new APIEndpointSpec( parent, parent, endpoint );
		APIEndpoint e = new APIEndpointImpl( spec );
		MultivaluedMap<String, String> map = MultiValuedMapSupport.parseQueryString( settings.replaceAll( " ", "\\&" ) );
		CallContext cc = CallContext.createContext( Util.newURI("http://dummy/doc/schools"), map, MakeData.variables( settings ) );
		Couple<APIResultSet, String> resultsAndFormat = e.call( cc );
		APIResultSet rs = resultsAndFormat.a;
		return new HashSet<Resource>( rs.getResultList() );
		}

	private static Model model( String triples ) 
		{
		Model m = ModelTestBase.modelWithStatements( "" );
		m.setNsPrefix( "api", API.getURI() );
		m.setNsPrefix( "geo", "http://www.w3.org/2003/01/geo/wgs84_pos#" );
		ModelTestBase.modelAdd( m, triples );
		return m;
		}
	}
