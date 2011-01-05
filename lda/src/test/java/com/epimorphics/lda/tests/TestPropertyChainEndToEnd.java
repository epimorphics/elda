/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) copyright Epimorphics Limited 2010
    $Id$
*/

package com.epimorphics.lda.tests;

import org.junit.Test;

import static org.junit.Assert.*;

import com.epimorphics.lda.core.APIQuery;
import com.epimorphics.lda.core.APIQuery.Param;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.core.ModelLoaderI;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.shortnames.StandardShortnameService;
import com.epimorphics.lda.tests_support.LoadsNothing;
import com.epimorphics.lda.tests_support.MakeData;
import com.epimorphics.lda.tests_support.ShortnameFake;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
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
			+ "; spec:my-viewer api:properties ex:size"
		//	
			+ "; ex:size rdf:type owl:DatatypeProperty"
			+ "; ex:size api:label 'size'"
			+ "; ex:size rdfs:range xsd:int"
			+ "; ex:number api:label 'number'"
			+ "; ex:name api:label 'name'"
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
		ModelLoaderI loader = new LoadsNothing();
		APITester t = new APITester( model, loader );
		String uriTemplate = "http://dummy/doc/schools";
		String queryString = "_properties=type,localAuthority.number";
		APIResultSet rs = t.runQuery( uriTemplate, queryString );
		assertContains( expect, rs );
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
		ModelLoaderI loader = new LoadsNothing();
		APITester t = new APITester( model, loader );
		String uriTemplate = "http://dummy/doc/schools";
		APIResultSet rs = t.runQuery( uriTemplate, "" );
		assertContains( expect, rs );
		}
	
	@Test public void ensureUnitPropertyHasType()
		{
		ensurePropertyThingHasType( "max-size" );
		}
	
	@Test public void ensureChainedPropertyHasType()
		{
		ensurePropertyThingHasType( "max-thing.size" );
		}

	private void ensurePropertyThingHasType(String propertyThing) 
		{
		Model model = MakeData.specModel
			( "spec:spoo rdf:type api:API"
			+ "; ex:size rdf:type owl:DatatypeProperty"
			+ "; ex:size rdfs:range xsd:int"
			+ "; ex:thing rdf:type owl:DatatypeProperty"
			+ ""
			);
		Resource spec = model.createResource( model.expandPrefix( "spec:spoo" ) );
		ModelLoaderI loader = new LoadsNothing();
		PrefixMapping prefixes = PrefixMapping.Factory.create();
		ShortnameService sns = new StandardShortnameService( spec, prefixes, loader );
		APIQuery q = new APIQuery( sns );
		q.addFilterFromQuery( Param.make( propertyThing), "17" );
		assertContains( q.assembleSelectQuery( prefixes ), "\"17\"^^< http://www.w3.org/2001/XMLSchema#int>" );
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
