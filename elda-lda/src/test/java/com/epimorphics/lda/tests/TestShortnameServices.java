/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.tests;

import static org.apache.jena.rdf.model.test.ModelTestBase.property;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.core.Param;
import com.epimorphics.lda.core.VarSupply;
import com.epimorphics.lda.query.ValTranslator;
import com.epimorphics.lda.query.ValTranslator.Filters;
import com.epimorphics.lda.rdfq.Any;
import com.epimorphics.lda.rdfq.RDFQ;
import com.epimorphics.lda.shortnames.*;
import com.epimorphics.lda.tests_support.ExpandOnly;
import com.epimorphics.lda.tests_support.LoadsNothing;
import com.epimorphics.util.CollectionUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.*;
import org.apache.jena.shared.PrefixMapping;

/**
    Some (limited) tests for shortname services
 
 	@author chris
*/
public class TestShortnameServices
	{
	
	static final String EX = "http://www.epimorphics.com/tools/example#";
		
	static final String XSDinteger = XSDDatatype.XSDinteger.getURI();
	
	@Test  public void testLanguageDoesNotOverrideType()
		{
		Model m = ModelIOUtils.modelFromTurtle( ":root a api:API. <eh:/P> a owl:DatatypeProperty; api:name 'P'; rdfs:range xsd:integer." );
		Resource root = m.createResource( EX + "root" );
		PrefixMapping pm = PrefixMapping.Factory.create();
		ShortnameService sns = new StandardShortnameService( root, pm, LoadsNothing.instance );
		VarSupply vs = null;
		Filters expressions = null;
		ValTranslator vt = new ValTranslator( vs, expressions, sns );
		Param.Info pInf = Param.make( sns, "P" ).fullParts()[0];
		Any a = vt.objectForValue( pInf, "17", "en" );
		assertEquals( RDFQ.literal( "17", "", XSDinteger ), a );
		}
	}
