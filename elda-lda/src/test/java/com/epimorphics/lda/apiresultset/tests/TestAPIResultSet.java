/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2012 Epimorphics Limited
*/

package com.epimorphics.lda.apiresultset.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.vocabularies.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.vocabulary.DOAP;
import com.hp.hpl.jena.vocabulary.*;

public class TestAPIResultSet {

	final PrefixMapping none = PrefixMapping.Factory.create();
	
	@Test public void testDefaultPrefixes() {
		Model m = ModelFactory.createDefaultModel();
		APIResultSet.setUsedPrefixes( m, none );
		assertEquals( RDF.getURI(), m.getNsPrefixURI( "rdf" ) );
		assertEquals( RDFS.getURI(), m.getNsPrefixURI( "rdfs" ) );
		assertEquals( DCTerms.getURI(), m.getNsPrefixURI( "dct" ) );
		assertEquals( OpenSearch.getURI(), m.getNsPrefixURI( "os" ) );
		assertEquals( SPARQL.NS, m.getNsPrefixURI( "sparql" ) );
		assertEquals( DOAP.NS, m.getNsPrefixURI( "doap" ) );
		assertEquals( XHV.getURI(), m.getNsPrefixURI( "xhv" ) );
		assertEquals( ELDA.COMMON.NS, m.getNsPrefixURI( "opmv" ) );
	}
	
	final PrefixMapping changes = PrefixMapping.Factory.create()
		.setNsPrefix( "mine", "eh:/mine#" )
	//
		.setNsPrefix( "rdf", "eh:/mine/rdf#" )
		.setNsPrefix( "rdfs", "eh:/mine/rdfs#" )
		.setNsPrefix( "dct", "eh:/mine/dct#" )
		.setNsPrefix( "os", "eh:/mine/os#" )
		.setNsPrefix( "sparql", "eh:/mine/sparql#" )
		.setNsPrefix( "doap", "eh:/mine/doap#" )
		.setNsPrefix( "xhv", "eh:/mine/xhv#" )
		.setNsPrefix( "opmv", "eh:/mine/opmv#" )
		;
	
	@Test public void testModifiedPrefixes() {
		Model m = ModelFactory.createDefaultModel();
		APIResultSet.setUsedPrefixes( m, changes );
		assertEquals( "eh:/mine#", m.getNsPrefixURI( "mine" ) );
	//
		assertEquals( "eh:/mine/rdf#", m.getNsPrefixURI( "rdf" ) );
		assertEquals( "eh:/mine/rdfs#", m.getNsPrefixURI( "rdfs" ) );
		assertEquals( "eh:/mine/dct#", m.getNsPrefixURI( "dct" ) );
		assertEquals( "eh:/mine/os#", m.getNsPrefixURI( "os" ) );
		assertEquals( "eh:/mine/sparql#", m.getNsPrefixURI( "sparql" ) );
		assertEquals( "eh:/mine/doap#", m.getNsPrefixURI( "doap" ) );
		assertEquals( "eh:/mine/xhv#", m.getNsPrefixURI( "xhv" ) );
		assertEquals( "eh:/mine/opmv#", m.getNsPrefixURI( "opmv" ) );
	}
}
