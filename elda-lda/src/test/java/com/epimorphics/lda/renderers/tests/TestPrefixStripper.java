package com.epimorphics.lda.renderers.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.epimorphics.lda.renderers.StripPrefixes;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;

public class TestPrefixStripper {
	
	static final String NS_xyzzy = "http://epimorphics.example.org/ns_xyzzy#";
	static final String NS_plugh = "http://epimorphics.example.org/ns_plugh#";
	
	static final String NS_none = "http://epimorphics.example.org/ns_none#";
	
	Model A = ModelFactory.createDefaultModel();
	
	Resource S = A.createResource( NS_xyzzy + "S" );
	Property P = A.createProperty( NS_xyzzy + "P" );
	RDFNode O  = A.createResource( NS_xyzzy + "O" );
	
	RDFNode OLiteral = A.createLiteral( "lexicalForm" );
	
	RDFNode OTyped = A.createTypedLiteral("lexicalForm", NS_xyzzy + "type");
	
	Property none = A.createProperty( NS_none + "P" );
	
	{
		A.setNsPrefix( "xyzzy", NS_xyzzy );
		A.setNsPrefix( "plugh", NS_plugh );
	}

	@Test public void testVisitsSubject() {
		A.add(S, none, none);
		check();
	}
	
	@Test public void testVisitsPredicate() {
		A.add(none, P, none);
		check();
	}
	
	@Test public void testVisitsResourceObject() {
		A.add(none, none, O);
		check();
	}
	
	@Test public void testVisitsLiteralObject() {
		A.add(none, none, OLiteral);
		checkLiteral();
	}
	
	@Test public void testVisitsTypedObject() {
		A.add(none, none, OTyped);
		check();
	}

	// no prefixes should be preserved
	private void checkLiteral() {
		Model B = StripPrefixes.Do(A);
		ModelTestBase.assertIsoModels( A, B );
		assertEquals( null, B.getNsPrefixURI( "xyzzy" ) );
		assertEquals( null, B.getNsPrefixURI( "plugh" ) );
	}

	// only the xyzzy suffix should be preserved
	private void check() {
		Model B = StripPrefixes.Do(A);
		ModelTestBase.assertIsoModels( A, B );
		assertEquals( NS_xyzzy, B.getNsPrefixURI( "xyzzy" ) );
		assertEquals( null, B.getNsPrefixURI( "plugh" ) );
	}
	

}
