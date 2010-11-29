/*
    See lda-top/LICENCE for the licence for this software.
    (c) copyright Epimorphics Limited 2010
    $Id$
*/

package com.epimorphics.lda.tests;

import org.junit.Test;

import static org.junit.Assert.*;

import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.core.ModelLoaderI;
import com.epimorphics.lda.tests_support.LoadsNothing;
import com.epimorphics.lda.tests_support.MakeData;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;

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
			+ "; ex:size a owl:DatatypeProperty"
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
			+ "; ex:size a owl:DatatypeProperty"
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
