/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.apispec.tests;

import org.junit.Test;
import static org.junit.Assert.*;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.rdfq.Any;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.support.PrefixLogger;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import static org.hamcrest.CoreMatchers.*;

public class TestAPISpecAcceptsFakeTypes 
	{	
	String spec = 
		":my a api:API; api:sparqlEndpoint :spoo; api:variable"
		+ " [api:name 'fred'; api:value '{tom}']"
		+ ", [api:name 'tom'; api:value 17]"
		+ "; api:endpoint :myE."
		+ "\n"
		+ "\n:myE a api:ListEndpoint"
		+ "\n;   api:uriTemplate '/whatsit'" 
		+ "\n;   api:selector [api:filter 'year=1066']"
		+ "."
		+ "\n:year a owl:DatatypeProperty; rdfs:label 'year'; rdfs:range :faketype."
		+ "\n:name a owl:DatatypeProperty; rdfs:label 'name'."
		+ "\n"
		;
	
	@Test public void ensureRespectsDataypesByType() 
		{
		Model m = ModelIOUtils.modelFromTurtle( spec );
		m.removeAll( null, RDF.type, OWL.DatatypeProperty );
		Resource ft = m.createResource( m.expandPrefix( ":faketype" ) );
		m.add( ft, RDF.type, RDFS.Datatype );
	//
		ensureRespectsDatatypes( m );
		}
	
	@Test public void ensureRespectsDataypesByPropertyType() 
		{
		Model m = ModelIOUtils.modelFromTurtle( spec );
	//
		ensureRespectsDatatypes( m );
		}

	private void ensureRespectsDatatypes(Model m) {
		PrefixLogger pl = new PrefixLogger();
		Resource root = m.createResource( m.expandPrefix( ":my" ) );
		APISpec s = SpecUtil.specFrom( root );
		Any x = s.getShortnameService().normalizeNodeToRDFQ( "year", "spoo", null );
		String eg = m.getNsPrefixURI( "" );
		assertThat( x.asSparqlTerm(pl), is( "\"spoo\"^^<" + eg + "faketype>" ) );
	}
	
	@Test public void testPlainLiteral() {
		PrefixLogger pl = new PrefixLogger();
		Model m = ModelIOUtils.modelFromTurtle( spec );
		Resource root = m.createResource( m.expandPrefix( ":my" ) );
		APISpec s = SpecUtil.specFrom( root );
		Any x = s.getShortnameService().normalizeNodeToRDFQ( "name", "Frodo", null );
		assertThat( x.asSparqlTerm(pl), is( "\"Frodo\"" ) );
	}

}
