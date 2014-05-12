package com.epimorphics.lda.query.tests;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.epimorphics.lda.core.View;
import com.epimorphics.lda.core.View.State;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class TestDescribeQueries {
	
	PrefixMapping pm = PrefixMapping.Factory.create()
		.setNsPrefix( "rdf", RDF.getURI() )
		;

	Model m = ModelFactory.createDefaultModel();
	
	static final String expectA =
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
			+ "\nDESCRIBE "
			+ "\n  rdf:intruder"
			+ "\n  <http://www.w3.org/2000/01/rdf-schema#stranger>"
			;
	
	static final String expectB =
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
			+ "\nDESCRIBE "
			+ "\n  <http://www.w3.org/2000/01/rdf-schema#stranger>"
			+ "\n  rdf:intruder"
			;
			
	@Test public void testX() {
		m.withDefaultMappings(pm);
		Resource a = m.createResource( RDF.getURI() + "intruder" );
		Resource b = m.createResource( RDFS.getURI() + "stranger" );
		List<Resource> both = Arrays.asList( a, b );
		State s = new State("select string", both, m, null, null, null);
		String q = View.createDescribeQueryForItems( s, both );
		if (!q.equals( expectA ) && !q.equals( expectB )) 
			fail( "wrong describe query created:\n" + q ); 
	}

}
