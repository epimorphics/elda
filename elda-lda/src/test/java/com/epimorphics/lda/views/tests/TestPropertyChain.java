/*
	See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
	for the licence for this software.
	
	(c) Copyright 2011 Epimorphics Limited
	$Id$
*/
package com.epimorphics.lda.views.tests;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import com.epimorphics.lda.core.VarSupply;
import com.epimorphics.lda.core.property.ViewProperty;
import com.epimorphics.lda.rdfq.Any;
import com.epimorphics.lda.rdfq.RDFQ;
import com.epimorphics.lda.rdfq.URINode;
import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.core.View;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.support.PropertyChain;
import com.epimorphics.lda.tests.SNS;
import com.epimorphics.util.CollectionUtils;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.OWL;

import java.util.List;

public class TestPropertyChain {
	
	static ViewProperty P = new ViewProperty.Base(ResourceFactory.createProperty( "eh:/p" ));
	static ViewProperty Q = new ViewProperty.Base(ResourceFactory.createProperty( "eh:/q" ));

	static ShortnameService sns = new SNS( "p=eh:/p;q=eh:/q;p_q=NEVER" );
	
	@Test public void testBuildFromDottedNames() {
		View v = new View();
		v.addViewFromParameterValue( "p.q", sns);
		List<PropertyChain> chains = v.chains();
		assertEquals(1, chains.size());
		List<ViewProperty> chain = chains.get(0).getProperties();
		assertEquals(2, chain.size());
		assertEquals("eh:/p", chain.get(0).toString());
		assertEquals("eh:/q", chain.get(1).toString());
	}
	
	@Test public void testBuildFromProperty() {
		View v = new View();
		v.addViewFromRDFList( ResourceFactory.createResource( "eh:/p" ), sns );
		List<PropertyChain> chains = v.chains();
		assertEquals(1, chains.size());
		List<ViewProperty> chain = chains.get(0).getProperties();
		assertEquals(1, chain.size());
		assertEquals("eh:/p", chain.get(0).toString());
	}
	
	@Test public void testBuildFromPropertyList() {
		View v = new View();
		Model m = ModelIOUtils.modelFromTurtle( "@prefix : <eh:/>. :root owl:sameAs (:p :q)." );
		Statement s = m.listStatements( null, OWL.sameAs, (RDFNode) null ).next();
		v.addViewFromRDFList( s.getResource(), sns );
		List<PropertyChain> chains = v.chains();
		assertEquals(1, chains.size());
		List<ViewProperty> chain = chains.get(0).getProperties();
		assertEquals(2, chain.size());
		assertEquals("eh:/p", chain.get(0).toString());
		assertEquals("eh:/q", chain.get(1).toString());
	}

	private void verifyInverseProperty(ViewProperty vp, String expectedUri) {
		Any subject = mock(Any.class);
		Any object = mock(Any.class);

		RDFQ.Triple triple = vp.asTriple(subject, object, mock(VarSupply.class));
		assertEquals(object, triple.S);
		assertEquals(subject, triple.O);
		assertEquals(expectedUri, ((URINode) triple.P).spelling());
	}

	@Test
	public void addViewFromParameterValue_InverseProperty_ReturnsInvertedProperty() {
		View v = new View();
		v.addViewFromParameterValue("~p", sns);

		List<PropertyChain> result = v.chains();
		assertEquals(1, result.size());
		List<ViewProperty> chain = result.get(0).getProperties();
		assertEquals(1, chain.size());

		ViewProperty vp = chain.get(0);

		assertEquals("~p", vp.shortName(sns.asContext()));
		assertEquals("eh:/p", vp.asProperty().getURI());
		verifyInverseProperty(vp, "eh:/p");
	}

	@Test
	public void addViewFromParameterValue_AddsInversePropertyChain() {
		View v = new View();
		v.addViewFromParameterValue("~p.~q", sns);

		List<PropertyChain> result = v.chains();
		assertEquals(1, result.size());
		List<ViewProperty> chain = result.get(0).getProperties();
		assertEquals(2, chain.size());

		ViewProperty vp1 = chain.get(0);
		assertEquals("~p", vp1.shortName(sns.asContext()));
		assertEquals("eh:/p", vp1.asProperty().getURI());
		verifyInverseProperty(vp1, "eh:/p");

		ViewProperty vp2 = chain.get(1);
		assertEquals("~q", vp2.shortName(sns.asContext()));
		assertEquals("eh:/q", vp2.asProperty().getURI());
		verifyInverseProperty(vp2, "eh:/q");
	}
}
