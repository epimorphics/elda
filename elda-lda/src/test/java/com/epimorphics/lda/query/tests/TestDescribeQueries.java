package com.epimorphics.lda.query.tests;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.epimorphics.lda.core.View;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
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
			+ "\nDESCRIBE"
			+ "\n  rdf:intruder"
			+ "\n  <http://www.w3.org/2000/01/rdf-schema#stranger>"
			;
	
	static final String expectB =
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
			+ "\nDESCRIBE"
			+ "\n  <http://www.w3.org/2000/01/rdf-schema#stranger>"
			+ "\n  rdf:intruder"
			;
			
	@Test public void testX() {
		Resource a = m.createResource( RDF.getURI() + "intruder" );
		Resource b = m.createResource( RDFS.getURI() + "stranger" );
		List<Resource> both = Arrays.asList( a, b );
		String q = View.createDescribeQueryForItems( pm, both );
		if (!q.equals( expectA ) && !q.equals( expectB )) 
			fail( "wrong describe query created:\n" + q ); 
	}

}
