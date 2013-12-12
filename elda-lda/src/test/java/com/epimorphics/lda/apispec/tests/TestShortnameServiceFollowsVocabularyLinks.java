/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.apispec.tests;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

import com.epimorphics.jsonrdf.ContextPropertyInfo;
import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.core.ModelLoader;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.shortnames.StandardShortnameService;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.XSD;

public class TestShortnameServiceFollowsVocabularyLinks {
	
	ModelLoader loader = new ModelLoader() {
		
		@Override public Model loadModel( String uri ) {
			if (uri.equals( "A" )) return namesVocabA;
			if (uri.equals( "B" )) return namesVocabB;
			if (uri.equals( "C" )) return propertiesVocabC;
			return null;
		}
	};
	
	static final Model namesVocabA = ModelIOUtils.modelFromTurtle
		( ":dt_A a rdfs:Datatype." 
		+ "\n:d api:label 'name_d'." 
		+ "\n:e a rdf:Property; rdfs:label 'name_e'."
		+ "\n:f api:label 'f_api_label'; rdfs:label 'fRDFlabel'."
		+ "\n:g api:label 'g_from_A'."
		);
	
	static final Model namesVocabB = ModelIOUtils.modelFromTurtle
		( ":p a owl:DatatypeProperty; rdfs:range :dt_B." );
	
	static final String NS = namesVocabA.expandPrefix( ":" );
	
	static final Model namesModel = ModelIOUtils.modelFromTurtle
		( "<fake:root> a api:API." 
		+ "\n<fake:root> api:vocabulary 'A', 'B'."
		+ "\n:dt_main a rdfs:Datatype."
		+ "\n:a api:label 'name_a'."
		+ "\n:b a rdf:Property; rdfs:label 'name_b'."
		+ "\n:c api:label 'c_api_label'; rdfs:label 'cRDFlabel'."
		+ "\n:g api:label 'gFromSpec'."
		);

	@Test public void testRecognisesDatatypesInSpec() {
		Resource root = namesModel.createResource( "fake:root" );
		ShortnameService sns = new StandardShortnameService(root, namesModel, loader);
		assertTrue( ":dt_main should be a datatype", sns.isDatatype( NS + "dt_main" ) );
		assertFalse( ":nowhere should not be a datatype", sns.isDatatype( NS + "nowhere" ) );
	}

	@Test public void testRecognisesDatatypesFromVocab() {
		Resource root = namesModel.createResource( "fake:root" );
		ShortnameService sns = new StandardShortnameService(root, namesModel, loader);
		assertFalse( ":nowhere should not be a datatype", sns.isDatatype( NS + "nowhere" ) );
		assertTrue( ":dt_A should be a datatype", sns.isDatatype( NS + "dt_A" ) );
	}

	@Test public void testRecognisesImplicitDatatypeFromVocab() {
		Resource root = namesModel.createResource( "fake:root" );
		ShortnameService sns = new StandardShortnameService(root, namesModel, loader);
		assertFalse( ":nowhere should not be a datatype", sns.isDatatype( NS + "nowhere" ) );
		assertTrue( ":dt_B should be a datatype", sns.isDatatype( NS + "dt_B" ) );
	}

	@Test public void testRecognisesLabelsInSpec() {
		Resource root = namesModel.createResource( "fake:root" );
		ShortnameService sns = new StandardShortnameService( root, namesModel, loader );
		assertEquals( NS + "a", sns.expand( "name_a" ) );
		assertEquals( NS + "b", sns.expand( "name_b" ) );
		assertEquals( NS + "c", sns.expand( "c_api_label" ) );
		assertEquals( null, sns.expand( "cRDFlabel" ) );
	}
	
	@Test public void testRecognisesLabelsFromVocab() {
		Resource root = namesModel.createResource( "fake:root" );
		ShortnameService sns = new StandardShortnameService( root, namesModel, loader );
		Map<String, String> uriToName = sns.constructURItoShortnameMap(namesModel, namesModel);
		assertEquals( NS + "d", sns.expand( "name_d" ) );
		assertEquals( "name_d", uriToName.get( NS + "d" ) );
		assertEquals( NS + "e", sns.expand( "name_e" ) );	
		assertEquals( "name_e", uriToName.get( NS + "e" ) );
		assertEquals( NS + "f", sns.expand( "f_api_label" ) );
		assertEquals( null, sns.expand( "fRDFlabel" ) );		
		assertEquals( "f_api_label", sns.asContext().getNameForURI( NS + "f" ) );
	}
	
	@Test public void testSpecOverridesVocabName() {
		Resource root = namesModel.createResource( "fake:root" );
		ShortnameService sns = new StandardShortnameService( root, namesModel, loader );
		assertEquals( null, sns.expand( "gFromA" ) );		
		assertEquals( NS + "g", sns.expand( "gFromSpec" ) );
		assertEquals( "gFromSpec", sns.constructURItoShortnameMap( namesModel, namesModel).get( NS + "g" ) );
	}

	static final Model propertiesVocabC = ModelIOUtils.modelFromTurtle
		( ":p a owl:DatatypeProperty; rdfs:range xsd:integer." 
		+ "\n:q a owl:DatatypeProperty; rdfs:range xsd:string."		
		);
	
	static final Model propertiesModel = ModelIOUtils.modelFromTurtle
		( "<fake:root> a api:API."
		+ "\n<fake:root> api:vocabulary 'C'."
		+ "\n:p a rdf:Property; api:label 'name_p'."
		+ "\n:q a rdf:Property; api:label 'name_q'; rdfs:range xsd:decimal."
		);
	
	@Test public void testSeePropertyFromConfig() {
		Resource root = propertiesModel.createResource( "fake:root" );
		ShortnameService sns = new StandardShortnameService( root, propertiesModel, loader );
		
		ContextPropertyInfo cpi_p = sns.getPropertyByName( "name_p" );
		ContextPropertyInfo cpi_q = sns.getPropertyByName( "name_q" );
		
		assertEquals( "should see vocab property type", XSD.integer.getURI(), cpi_p.getType() );
		assertEquals( "should see config property type", XSD.decimal.getURI(), cpi_q.getType() );
	}

}
