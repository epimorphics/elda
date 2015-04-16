/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.*;
import com.epimorphics.lda.query.APIQuery;
import com.epimorphics.lda.query.ContextQueryUpdater;
import com.epimorphics.lda.query.tests.QueryTestUtils;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.support.MultiMap;
import com.epimorphics.lda.tests_support.LoadsNothing;
import com.epimorphics.lda.tests_support.MakeData;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TestParameterNameAndValueExpansion 
	{
	@Test public void ensure_PropertiesRespected()
		{
		Model model = MakeData.specModel
			( "spec:spoo rdf:type api:API"
			+ "; spec:spoo api:sparqlEndpoint here:data"
			+ "; spec:spoo api:endpoint spec:coins"
		//
			+ "; spec:coins rdf:type api:ListEndpoint"
			+ "; spec:coins api:uriTemplate http://dummy/{p0}/{v0}"
			+ "; spec:coins api:selector _selector"
			+ "; _selector api:filter '{p0}={v0}'"
		//	
			+ "; ex:doc a owl:DatatypeProperty"
			+ "; ex:doc api:label 'doc'"
			+ "; ex:size api:label 'size'"
			+ "; ex:size rdfs:range xsd:int"
			+ "; ex:number api:label 'number'"
			+ "; ex:name api:label 'name'"
			+ "; rdf:type api:label 'type'"
			+ "; rdf:type rdf:type rdf:Property"
			+ "; ex:Class api:label 'class'"
		//
			+ "; school-ont:localAuthority api:label 'localAuthority'"
			+ "; rdfs:label api:label 'label'"
		//
			+ "; here:data spec:item ex:A"
		//
			+ "; ex:A rdf:type ex:Class"
			+ "; ex:A school-ont:localAuthority ex:LA-1"
			+ "; ex:A ex:doc 'schools'"
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
		assertContains( expect, rs );
		}
	
	private void assertContains( Model expect, APIResultSet rs) {
		Model m = rs.getMergedModel();
		if (!m.containsAll( expect )) {
			PrefixMapping pm = prefixes(rs);
			String message = "result set " 
				+ show(m, pm)
				+ " does not contain all of "
				+ show(expect, pm)
				;
			fail( message );
		}
	}

	private PrefixMapping prefixes( APIResultSet rs ) {
		return PrefixMapping.Factory.create()
			.setNsPrefixes(rs.getMergedModel())
			.setNsPrefixes( PrefixMapping.Extended )
			.setNsPrefix( "terms", "http://purl.org/dc/terms/" )
			.setNsPrefix( "dum", "http://dummy/doc/schools" )
			.setNsPrefix( "dumx", "http://dummy//doc/schools" )
			;
	}
	
	private String show( Model m, PrefixMapping pm ) {
		StringBuilder sb = new StringBuilder();
		sb.append( "\n" );
		for (Statement s: m.listStatements().toList()) {
			sb.append( "  " ).append( s.asTriple().toString( pm ) ).append( "\n" );
		}
		return sb.toString();
	}

	@Test public void deferredPropertyShouldAppearInQuery()
		{
		MultiMap<String, String> qp = MakeData.parseQueryString( "{aname}=value" );
		Bindings bindings = MakeData.variables( "aname=bname" );
		Bindings cc = Bindings.createContext( bindings, qp );
		NamedViews nv = new FakeNamedViews();
		ShortnameService sns = new SNS( "bname=eh:/full-bname" );
		APIQuery aq = QueryTestUtils.queryFromSNS(sns);
		ContextQueryUpdater cq = new ContextQueryUpdater( ContextQueryUpdater.ListEndpoint, cc, nv, sns, aq );
		cq.updateQueryAndConstructView( aq.deferredFilters );
		String q = aq.assembleSelectQuery( PrefixMapping.Factory.create() );
		int where = q.indexOf( "?item <eh:/full-bname> \"value\"" );
		assertFalse( "deferred property has not appeared in query", where < 0 );
		}
	}
