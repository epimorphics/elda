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
		( ":dt_A a rdfs:Datatype." );
	
	static final Model modelB = ModelIOUtils.modelFromTurtle
		( ":p a owl:DatatypeProperty; rdfs:range :dt_B." );
	
	static final String NS = modelA.expandPrefix( ":" );
	
	static final Model model = ModelIOUtils.modelFromTurtle
		( "<fake:root> a api:API." 
		+ "\n<fake:root> api:vocabulary 'A', 'B'."
		+ "\n:dt_main a rdfs:Datatype."
		);

	@Test public void testFollowsVocabularyLinks() {
		Resource root = model.createResource( "fake:root" );
		ShortnameService sns = new StandardShortnameService(root, model, loader);
		assertTrue( ":dt_main should be a dadatype", sns.isDatatype( NS + "dt_main" ) );
		assertFalse( ":nowhere should not be a datatype", sns.isDatatype( NS + "nowhere" ) );
		assertTrue( ":dt_A should be a datatype", sns.isDatatype( NS + "dt_A" ) );
		assertTrue( ":dt_B should be a datatype", sns.isDatatype( NS + "dt_B" ) );
	}

}
