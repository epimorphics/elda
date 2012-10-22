package com.epimorphics.lda.renderers.velocity.tests;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

import com.epimorphics.lda.renderers.velocity.Help;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
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
		Map<Resource, String> shortNames = Help.getShortnames( m );
		assertEquals( null, shortNames.get( RDF.first ) );
		assertEquals( "TYPE", shortNames.get( RDF.type ) );
	}

	private Resource tb(Model m, Resource r, String shortName ) {
		return m.createResource()
		    .addProperty( API.label, shortName )
		    .addProperty( API.property, r )
		    ;
	}
}
