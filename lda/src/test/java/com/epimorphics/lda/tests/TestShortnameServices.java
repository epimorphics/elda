/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.tests;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.rdfq.Any;
import com.epimorphics.lda.rdfq.RDFQ;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.shortnames.ShortnameService.Util;
import com.epimorphics.lda.shortnames.StandardShortnameService;
import com.epimorphics.lda.tests_support.ExpandOnly;
import com.epimorphics.lda.tests_support.LoadsNothing;
import com.epimorphics.util.CollectionUtils;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;

import static com.hp.hpl.jena.rdf.model.test.ModelTestBase.property;

/**
    Some (limited) tests for shortname services
 
 	@author chris
*/
public class TestShortnameServices
	{
	@Test public void testExpandProperties()
		{
		Property P = property( "eh:/P" ), Q = property( "spoo:/Q" );
		ShortnameService sns = new ExpandOnly( "P=eh:/P;Q=spoo:/Q" );
		List<Property> p = Util.expandProperties( "P.Q", sns );
		assertEquals( CollectionUtils.list( P, Q ), p );
		}
	
	static final String EX = "http://www.epimorphics.com/tools/example#";
		
	static final String XSDinteger = XSDDatatype.XSDinteger.getURI();
	
	@Test  public void testLanguageDoesNotOverrideType()
		{
		Model m = ModelIOUtils.modelFromTurtle( ":root a api:API. <eh:/P> a owl:DatatypeProperty; api:name 'P'; rdfs:range xsd:integer." );
		Resource root = m.createResource( EX + "root" );
		PrefixMapping pm = PrefixMapping.Factory.create();
		ShortnameService sns = new StandardShortnameService( root, pm, LoadsNothing.instance );
		Any a = sns.valueAsRDFQ( "P", "17", "en" );
		assertEquals( RDFQ.literal( "17", "", XSDinteger ), a );
		}
	}
