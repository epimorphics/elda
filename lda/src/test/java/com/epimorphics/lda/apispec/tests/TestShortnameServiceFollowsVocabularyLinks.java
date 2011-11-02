/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.apispec.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.core.ModelLoaderI;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.shortnames.StandardShortnameService;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class TestShortnameServiceFollowsVocabularyLinks {
	
	ModelLoaderI loader = new ModelLoaderI() {
		
		@Override public Model loadModel( String uri ) {
			if (uri.equals( "A" )) return modelA;
			if (uri.equals( "B" )) return modelB;
			return null;
		}
	};
	
	static final Model modelA = ModelIOUtils.modelFromTurtle
		( ":dt_A a rdfs:Datatype." 
		+ "\n:d api:label 'name_d'." 
		+ "\n:e a rdf:Property; rdfs:label 'name_e'."
		+ "\n:f api:label 'f_api_label'; rdfs:label 'f_rdf_label'."
		+ "\n:g api:label 'g_from_A'."
		);
	
	static final Model modelB = ModelIOUtils.modelFromTurtle
		( ":p a owl:DatatypeProperty; rdfs:range :dt_B." );
	
	static final String NS = modelA.expandPrefix( ":" );
	
	static final Model model = ModelIOUtils.modelFromTurtle
		( "<fake:root> a api:API." 
		+ "\n<fake:root> api:vocabulary 'A', 'B'."
		+ "\n:dt_main a rdfs:Datatype."
		+ "\n:a api:label 'name_a'."
		+ "\n:b a rdf:Property; rdfs:label 'name_b'."
		+ "\n:c api:label 'c_api_label'; rdfs:label 'c_rdf_label'."
		+ "\n:g api:label 'g_from_spec'."
		);

	@Test public void testRecognisesDatatypesInSpec() {
		Resource root = model.createResource( "fake:root" );
		ShortnameService sns = new StandardShortnameService(root, model, loader);
		assertTrue( ":dt_main should be a datatype", sns.isDatatype( NS + "dt_main" ) );
		assertFalse( ":nowhere should not be a datatype", sns.isDatatype( NS + "nowhere" ) );
	}

	@Test public void testRecognisesDatatypesFromVocab() {
		Resource root = model.createResource( "fake:root" );
		ShortnameService sns = new StandardShortnameService(root, model, loader);
		assertFalse( ":nowhere should not be a datatype", sns.isDatatype( NS + "nowhere" ) );
		assertTrue( ":dt_A should be a datatype", sns.isDatatype( NS + "dt_A" ) );
	}

	@Test public void testRecognisesImplicitDatatypeFromVocab() {
		Resource root = model.createResource( "fake:root" );
		ShortnameService sns = new StandardShortnameService(root, model, loader);
		assertFalse( ":nowhere should not be a datatype", sns.isDatatype( NS + "nowhere" ) );
		assertTrue( ":dt_B should be a datatype", sns.isDatatype( NS + "dt_B" ) );
	}

	@Test public void testRecognisesLabelsInSpec() {
		Resource root = model.createResource( "fake:root" );
		ShortnameService sns = new StandardShortnameService( root, model, loader );
		assertEquals( NS + "a", sns.expand( "name_a" ) );
		assertEquals( NS + "b", sns.expand( "name_b" ) );
		assertEquals( NS + "c", sns.expand( "c_api_label" ) );
		assertEquals( null, sns.expand( "c_rdf_label" ) );
	}
	
	@Test public void testRecognisesLabelsFromVocab() {
		Resource root = model.createResource( "fake:root" );
		ShortnameService sns = new StandardShortnameService( root, model, loader );
		assertEquals( NS + "d", sns.expand( "name_d" ) );
		assertEquals( "name_d", sns.asContext().getNameForURI( NS + "d" ) );
		assertEquals( NS + "e", sns.expand( "name_e" ) );	
		assertEquals( "name_e", sns.asContext().getNameForURI( NS + "e" ) );
		assertEquals( NS + "f", sns.expand( "f_api_label" ) );
		assertEquals( null, sns.expand( "f_rdf_label" ) );		
		assertEquals( "f_api_label", sns.asContext().getNameForURI( NS + "f" ) );
	}
	
	@Test public void testSpecOverridesVocabName() {
		Resource root = model.createResource( "fake:root" );
		ShortnameService sns = new StandardShortnameService( root, model, loader );
		assertEquals( null, sns.expand( "g_from_A" ) );		
		assertEquals( NS + "g", sns.expand( "g_from_spec" ) );
		assertEquals( "g_from_spec", sns.asContext().getNameForURI( NS + "g" ) );
	}

}
