/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.tests;

import static org.junit.Assert.fail;

import org.junit.Test;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.*;
import com.epimorphics.lda.query.APIQuery;
import com.epimorphics.lda.query.ContextQueryUpdater;
import com.epimorphics.lda.query.tests.QueryTestUtils;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.shortnames.StandardShortnameService;
import com.epimorphics.lda.tests_support.LoadsNothing;
import com.epimorphics.lda.tests_support.MakeData;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TestPropertyChainEndToEnd 
	{
	@Test public void testPropertyChainBuildsResultChain()
		{
		Model model = MakeData.specModel
			( "spec:spoo rdf:type api:API"
			+ "; spec:spoo api:sparqlEndpoint here:data"
			+ "; spec:spoo api:endpoint spec:schools"
		//
			+ "; spec:schools rdf:type api:ListEndpoint"
			+ "; spec:schools api:uriTemplate http://dummy/doc/schools"
			+ "; spec:schools api:defaultViewer spec:my-viewer"
			+ "; spec:schools api:selector _selector"
			+ "; _selector api:filter 'type=class'"
		//
			+ "; spec:my-viewer rdfs:label 'mine'"
			+ "; spec:my-viewer api:property ex:size"
		//	
			+ "; ex:size rdf:type owl:DatatypeProperty"
			+ "; ex:size api:label 'size'"
			+ "; ex:size rdfs:range xsd:int"
			+ "; ex:number api:label 'number'"
			+ "; ex:name api:label 'name'"
			+ "; rdf:type rdf:type rdf:Property"
			+ "; rdf:type rdfs:range spec:SomeObjectProperty"
			+ "; rdf:type api:label 'type'"
			+ "; ex:Class api:label 'class'"
		//
			+ "; school-ont:localAuthority api:label 'localAuthority'"
			+ "; rdfs:label api:label 'label'"
		//
			+ "; here:data spec:item ex:A"
		//
			+ "; ex:A rdf:type ex:Class"
			+ "; ex:A school-ont:localAuthority ex:LA-1"
		//
			+ "; ex:LA-1 ex:name 'la-one'"
			+ "; ex:LA-1 ex:number 17"
			);
		Model expect = MakeData.specModel
			( "ex:A school-ont:localAuthority ex:LA-1"
			+ "; ex:LA-1 ex:number 17"					
			);
		ModelLoader loader = LoadsNothing.instance;
		APITester t = new APITester( model, loader );
		String uriTemplate = "http://dummy/doc/schools";
		String queryString = "_properties=type,localAuthority.number";
		APIResultSet rs = t.runQuery( uriTemplate, queryString );
		assertContains( expect, rs.getMergedModel() );
		}

	@Test public void testPropertyChainInSpecBuildsResultChain()
		{
		Model model = MakeData.specModel
			( "spec:spoo rdf:type api:API"
			+ "; spec:spoo api:sparqlEndpoint here:data"
			+ "; spec:spoo api:endpoint spec:schools"
		//
			+ "; spec:schools rdf:type api:ListEndpoint"
			+ "; spec:schools api:uriTemplate http://dummy/doc/schools"
			+ "; spec:schools api:defaultViewer spec:my-viewer"
		//
			+ "; spec:my-viewer rdfs:label 'mine'"
			+ "; spec:my-viewer api:properties 'localAuthority.number'"
		//	
			+ "; ex:size rdf:type owl:DatatypeProperty"
			+ "; ex:size api:label 'size'"
			+ "; ex:size rdfs:range xsd:int"
			+ "; ex:number api:label 'number'"
			+ "; ex:name api:label 'name'"
		//
			+ "; school-ont:localAuthority api:label 'localAuthority'"
			+ "; rdfs:label api:label 'label'"
		//
			+ "; here:data spec:item ex:A"
		//
			+ "; ex:A rdf:type ex:Class"
			+ "; ex:A school-ont:localAuthority ex:LA-1"
		//
			+ "; ex:LA-1 ex:name 'la-one'"
			+ "; ex:LA-1 ex:number 17"
			);
		Model expect = MakeData.specModel
			( "ex:A school-ont:localAuthority ex:LA-1"
			+ "; ex:LA-1 ex:number 17"					
			);
		ModelLoader loader = LoadsNothing.instance;
		APITester t = new APITester( model, loader );
		String uriTemplate = "http://dummy/doc/schools";
		APIResultSet rs = t.runQuery( uriTemplate, "" );
		assertContains( expect, rs.getMergedModel() );
		}
	
	@Test public void ensureUnitPropertyHasType()
		{
		ensurePropertyThingHasType( "size" );
		}
	
	@Test public void ensureChainedPropertyHasType()
		{
		ensurePropertyThingHasType( "max-thing.size" );
		}

	private void ensurePropertyThingHasType( String propertyThing ) 
		{
		Model model = MakeData.specModel
			( "spec:spoo rdf:type api:API"
			+ "; ex:size rdf:type owl:DatatypeProperty"
			+ "; ex:size rdfs:range xsd:string"
			+ "; ex:thing rdf:type owl:DatatypeProperty"
			+ ""
			);
		Resource spec = model.createResource( model.expandPrefix( "spec:spoo" ) );
		ModelLoader loader = LoadsNothing.instance;
		PrefixMapping prefixes = PrefixMapping.Factory.create();
		ShortnameService sns = new StandardShortnameService( spec, prefixes, loader );
		APIQuery q = QueryTestUtils.queryFromSNS(sns);
		ContextQueryUpdater x = new ContextQueryUpdater( ContextQueryUpdater.ListEndpoint, (Bindings) null, NamedViews.noNamedViews, sns, q );
		x.addFilterFromQuery( Param.make(sns, propertyThing), "17.9" );
		assertContains( q.assembleSelectQuery( prefixes ), "\"17.9\"^^<http://www.w3.org/2001/XMLSchema#string>" );
		}
	
	private void assertContains(String target, String want) 
		{
		if (!target.contains(want))
			fail( "expected '" + target + "' to contain '" + want + "'" );
		}

	private void assertContains(Model expect, Model rs) 
		{
		if (!rs.containsAll(expect))
			{
			Model spoo = expect.difference( rs );
			StringBuilder them = new StringBuilder();
			for (Statement s: spoo.listStatements().toList()) 
				{
				them.append( s ).append( "\n" );
				}
//			expect.write( System.err, "Turtle" );
//			rs.write( System.err, "Turtle" );
			System.err.println( them );
			fail( "result set doesn't contain all expected triples: missing\n" + them );
			}
		}
	}
