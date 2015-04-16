/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.apispec.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.core.*;
import com.epimorphics.lda.core.Param.Info;
import com.epimorphics.lda.query.ValTranslator;
import com.epimorphics.lda.query.ValTranslator.Filters;
import com.epimorphics.lda.rdfq.Any;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.support.PrefixLogger;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.*;

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
		ValTranslator vt = new ValTranslator( vs, expressions, s.getShortnameService() );
		Info yearInf = Param.make(s.getShortnameService(), "year" ).fullParts()[0];
		Any x = vt.objectForValue( yearInf, "spoo", null );
		String eg = m.getNsPrefixURI( "" );
		assertThat( x.asSparqlTerm(pl), is( "\"spoo\"^^<" + eg + "faketype>" ) );
	}

	final VarSupply vs = null;
	final Filters expressions = null;
	
	@Test public void testPlainLiteral() {
		PrefixLogger pl = new PrefixLogger();
		Model m = ModelIOUtils.modelFromTurtle( spec );
		Resource root = m.createResource( m.expandPrefix( ":my" ) );
		APISpec s = SpecUtil.specFrom( root );
		ValTranslator vt = new ValTranslator(vs, expressions, s.getShortnameService() );
		Info nameInf = Param.make(s.getShortnameService(), "name" ).fullParts()[0];
		Any x = vt.objectForValue( nameInf, "Frodo", null );
		assertThat( x.asSparqlTerm(pl), is( "\"Frodo\"" ) );
	}

}
