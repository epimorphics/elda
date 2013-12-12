/*
	See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
	for the licence for this software.
	
	(c) Copyright 2011 Epimorphics Limited
	$Id$
*/
package com.epimorphics.lda.views.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.core.View;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.support.PropertyChain;
import com.epimorphics.lda.tests.SNS;
import com.epimorphics.util.CollectionUtils;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.OWL;

public class TestPropertyChain {
	
	static Property P = ResourceFactory.createProperty( "eh:/p" );
	static Property Q = ResourceFactory.createProperty( "eh:/q" );

	static ShortnameService sns = new SNS( "p=eh:/p;q=eh:/q;p.q=NEVER" );
	
	@Test public void testBuildFromDottedNames() {
		View v = new View();
		v.addViewFromParameterValue( "p.q", sns);
		PropertyChain pc = new PropertyChain( CollectionUtils.list( P, Q ) );
		assertEquals( CollectionUtils.set( pc ), v.chains() );
	}
	
	@Test public void testBuildFromProperty() {
		View v = new View();
		v.addViewFromRDFList( ResourceFactory.createResource( "eh:/p" ), sns );
		PropertyChain pc = new PropertyChain( CollectionUtils.list( P ) );
		assertEquals( CollectionUtils.set( pc ), v.chains() );
	}
	
	@Test public void testBuildFromPropertyList() {
		View v = new View();
		Model m = ModelIOUtils.modelFromTurtle( "@prefix : <eh:/>. :root owl:sameAs (:p :q)." );
		Statement s = m.listStatements( null, OWL.sameAs, (RDFNode) null ).next();
		v.addViewFromRDFList( s.getResource(), sns );
		PropertyChain pc = new PropertyChain( CollectionUtils.list( P, Q ) );
		assertEquals( CollectionUtils.set( pc ), v.chains() );
	}

}
