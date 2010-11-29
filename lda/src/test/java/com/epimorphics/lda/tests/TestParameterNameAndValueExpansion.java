package com.epimorphics.lda.tests;

import static org.junit.Assert.*;

import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Ignore;
import org.junit.Test;

import com.epimorphics.lda.core.APIQuery;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.core.CallContext;
import com.epimorphics.lda.core.ContextQueryUpdater;
import com.epimorphics.lda.core.ModelLoaderI;
import com.epimorphics.lda.core.NamedViews;
import com.epimorphics.lda.core.View;
import com.epimorphics.lda.rdfq.RDFQ;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.support.MultiValuedMapSupport;
import com.epimorphics.lda.tests_support.ExpandOnly;
import com.epimorphics.lda.tests_support.LoadsNothing;
import com.epimorphics.lda.tests_support.MakeData;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TestParameterNameAndValueExpansion 
	{
	@Test public void ensureFlarn()
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
		// assertContains( expect, rs );
		}
	
	@Test public void deferredPropertyShouldAppearInQuery()
		{
		MultivaluedMap<String, String> qp = MultiValuedMapSupport.parseQueryString( "{aname}=value" );
		UriInfo ui = new APITesterUriInfo( "my:URI", qp );
		Map<String, String> bindings = MakeData.hashMap( "aname=bname" );
		CallContext cc = CallContext.createContext( ui, bindings );
		NamedViews nv = new FakeNamedViews();
		ShortnameService sns = new SNS( "bname=eh:/full-bname" );
		APIQuery aq = new APIQuery( sns );
		ContextQueryUpdater cq = new ContextQueryUpdater( cc, nv, sns, aq );
		cq.updateQueryAndConstructView();
		String q = aq.assembleSelectQuery( PrefixMapping.Factory.create() );
		int where = q.indexOf( "?item <eh:/full-bname> \"value\" ." );
		assertFalse( "deferred property has not appeared in query", where < 0 );
		}
	
	private final class SNS extends ExpandOnly
		{
		public SNS(String expansions) 
			{ super( expansions ); }

		@Override public Resource normalizeResource( String s ) 
			{
			String u = expand( s );
//			System.err.println( ">> normalise '" + s + "' to '" + u + "'" );
			return ResourceFactory.createResource( u );
			}

		@Override public RDFQ.Any normalizeNodeToRDFQ( String prop, String val, String language ) 
			{
			return RDFQ.literal( val );
			}
		}
	
	private final class FakeNamedViews implements NamedViews 
		{
		final View v = new View();
	
		@Override public View getView(String viewname) { return v; }
	
		@Override public View getDefaultView() { return v; }
		}
	}
