package com.epimorphics.lda.support.tests;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.support.ModelPrefixEditor;
import com.hp.hpl.jena.rdf.model.Model;
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

}
