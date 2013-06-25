package com.epimorphics.lda.support.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.support.ModelPrefixEditor;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;

public class TestModelPrefixEditor {
	
	@Test public void testSubjectEdited() {
		ModelPrefixEditor pe = new ModelPrefixEditor().set( "http://alpha.com/", "http://localalpha/" );
		Model x = ModelIOUtils.modelFromTurtle( "<http://alpha.com/path> :property 'http://alpha.com/path'." );
		Model y = pe.rename( x );
		Model expected = ModelIOUtils.modelFromTurtle( "<http://localalpha/path> :property 'http://alpha.com/path'." );
		ModelTestBase.assertIsoModels( expected, y );
	}
	
	@Test public void testObjectEdited() {
		ModelPrefixEditor pe = new ModelPrefixEditor().set( "http://alpha.com/", "http://localalpha/" );
		Model x = ModelIOUtils.modelFromTurtle( "<http://unchanged/path> :property <http://alpha.com/path>." );
		Model y = pe.rename( x );
		Model expected = ModelIOUtils.modelFromTurtle( "<http://unchanged/path> :property <http://localalpha/path>." );
		ModelTestBase.assertIsoModels( expected, y );
	}
	
	@Test public void testPredicateEdited() {
		ModelPrefixEditor pe = new ModelPrefixEditor().set( "http://alpha.com/", "http://localalpha/" );
		Model x = ModelIOUtils.modelFromTurtle( "<http://unchanged/path> <http://alpha.com/path> 17 ." );
		Model y = pe.rename( x );
		Model expected = ModelIOUtils.modelFromTurtle( "<http://unchanged/path> <http://localalpha/path> 17 ." );
		ModelTestBase.assertIsoModels( expected, y );
	}
	
	@Test public void testRenamesLiteralType() {
		Model m = ModelFactory.createDefaultModel();
		String typeA_URI = "http://alpha.com/renamed";
		String typeB_URI = "http://unchanged/path";
		Literal typedA = m.createTypedLiteral( "typed_A", typeA_URI );
		Literal typedB = m.createTypedLiteral( "typed_B", typeB_URI );
		Literal renamed = m.createTypedLiteral( "typed_A", "http://localalpha/renamed" );
		ModelPrefixEditor pe = new ModelPrefixEditor().set( "http://alpha.com/", "http://localalpha/" );
		assertEquals( renamed, pe.rename( typedA ) );
		assertEquals( typedB, pe.rename( typedB ) );
	}
	
	@Test public void testRenamesResource() {
		Model m = ModelFactory.createDefaultModel();
		Resource blank = m.createResource(new AnonId("http://alpha.com/renamed" ) );
		Literal literal = m.createLiteral( "http://alpha.com/unchanged" );
		ModelPrefixEditor pe = new ModelPrefixEditor().set( "http://alpha.com/", "http://localalpha/" );
		assertEquals( literal, pe.rename( literal ) );
		assertEquals( blank, pe.rename( blank ) );
		assertEquals( m.createResource( "http://localalpha/renamed" ), pe.rename( m.createResource( "http://alpha.com/renamed" ) ) );
	}

}
