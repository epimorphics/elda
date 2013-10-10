package com.epimorphics.lda.renderers.velocity.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.epimorphics.lda.renderers.velocity.Help;
import com.epimorphics.lda.renderers.velocity.ShortNames;
import com.epimorphics.lda.vocabularies.API;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;

public class TestVelocityShortnames {

	@Test public void ensure() {
		Model m = ModelFactory.createDefaultModel();
		Resource execution = m.createResource();
		m.createResource()
			.addProperty( RDF.type, API.Page )
			.addProperty( API.wasResultOf, execution )
			;
		execution.addProperty( API.termBinding, tb( m, RDF.type, "TYPE" ) );
		ShortNames shortNames = Help.getShortnames( m );
		assertEquals( null, shortNames.getEntry( RDF.value ) );
		assertEquals( "TYPE", shortNames.getEntry( RDF.type ) );
	}

	private Resource tb(Model m, Resource r, String shortName ) {
		return m.createResource()
		    .addProperty( API.label, shortName )
		    .addProperty( API.property, r )
		    ;
	}
}
