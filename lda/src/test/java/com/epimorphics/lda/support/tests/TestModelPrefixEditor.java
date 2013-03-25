package com.epimorphics.lda.support.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.support.ModelPrefixEditor;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
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
