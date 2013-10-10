package com.epimorphics.lda.shortnames.tests;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.lda.shortnames.CompleteContext;
import com.epimorphics.lda.vocabularies.API;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;

public class TestShortnameSynthesis {
	
	String NS0 = "http://elda.example.org/x#";
	
	String NS1 = "http://elda.example.org/ns1#";
	
	String NS2 = "http://elda.example.org/ns2#";
	
	String NS3 = "http://elda.example.org/ns3#";
	
	String NS4 = "http://elda.example.org/ns4#";

	Model ontology = ModelFactory.createDefaultModel()
		.add( RDF.type, API.label, "type" )
		;
	
	Context context = new Context( ontology );
	
	PrefixMapping prefixes = PrefixMapping.Factory.create()
		.setNsPrefix( "rdf", RDF.getURI() )
		.setNsPrefix( "dc", DC.getURI() )
		.setNsPrefix( "ns3", NS3 )
		.setNsPrefix( "ns4", NS4 )
		;
	
	CompleteContext cc = new CompleteContext
		( CompleteContext.Mode.PreferLocalnames
		, context
		, prefixes
		);
	
	Model m = ModelFactory.createDefaultModel();
	
	Resource mA = m.createResource( NS0 + "A" );
	
	Property P(String ns, String ln) {
		return m.createProperty(ns + ln);
	}
	
	@Test public void testTrivial() {
		Model wild = ModelFactory.createDefaultModel()
			.add( mA, P(NS1, "trivial"), "v" )
			;
		
		Map<String, String> result = cc.Do( wild, prefixes );
		
		assertEquals( "trivial", result.get(NS1 + "trivial") );
	}
	
	@Test public void testNastyLocalname() {
		Model wild = ModelFactory.createDefaultModel()
			.add( mA, P(NS1, "x,y"), "v" )
			;
			
		Map<String, String> result = cc.Do( wild, prefixes );
			
		assertEquals( "y", result.get(NS1 + "x,y" ) );
		}
	
	@Test public void testUsingPrefixes() {
		Model wild = ModelFactory.createDefaultModel()
			.add( mA, P(NS3, "common"), "v" )
			.add( mA, P(NS4, "common"), "v" )
			;
		
		Map<String, String> result = cc.Do( wild, prefixes );
		
		assertEquals( "ns3_common", result.get(NS3 + "common" ) );
		assertEquals( "ns4_common", result.get(NS4 + "common" ) );
	}
	
	@Test public void testFallbackTohashed() {
		Model wild = ModelFactory.createDefaultModel()
			.add( mA, P(NS1, "local"), "v" )
			.add( mA, P(NS2, "local"), "v" )
			;
		
		Map<String, String> result = cc.Do( wild, prefixes );
		
		int hashNS1 = Math.abs( NS1.hashCode() ) % 10000;
		int hashNS2 = Math.abs( NS2.hashCode() ) % 10000;
		
		assertEquals( "local_" + hashNS1, result.get(NS1 + "local" ) );
		assertEquals( "local_" + hashNS2, result.get(NS2 + "local" ) );
		
	}
}
