/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.tests;

import static com.hp.hpl.jena.rdf.model.test.ModelTestBase.resourceSet;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.util.*;

import org.junit.Ignore;
import org.junit.Test;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.*;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.support.*;
import com.epimorphics.lda.tests_support.MakeData;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.util.Triad;
import com.epimorphics.util.URIUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;

public class ExploreTestingForLatAndLongEtc 
	{	

	static final Controls controls = new Controls( true, new Times() );
	
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

	@Test @Ignore public void testFindsResourceByLatAndLong()
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
		ModelLoader ml = new ModelLoader() 
			{
			@Override public Model loadModel(String uri) 
				{
				return latLongTestDescription;
				}
			};
		Resource specification = latLongTestDescription.createResource( "eh:/spec" );
		APISpec parent = new APISpec( EldaFileManager.get(), specification, ml );
		APIEndpointSpec spec = new APIEndpointSpec( parent, parent, endpoint );
		APIEndpoint e = new APIEndpointImpl( spec );
		MultiMap<String, String> map = MakeData.parseQueryString( settings.replaceAll( " ", "\\&" ) );
		URI ru = URIUtils.newURI("http://dummy/doc/schools");
		Bindings cc = Bindings.createContext( MakeData.variables( settings ), map );
		ResponseResult resultsAndFormat = e.call( new APIEndpoint.Request( controls, ru, cc ), new NoteBoard() );
		APIResultSet rs = resultsAndFormat.resultSet;
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
