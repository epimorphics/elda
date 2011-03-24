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
import com.epimorphics.lda.core.ModelLoaderI;
import com.epimorphics.lda.specs.APISpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

import static org.hamcrest.CoreMatchers.*;

public class TestAPISpecAcceptsFakeTypes 
	{
	ModelLoaderI NoLoader = null;
	
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
	
	@Test public void testFakeType() 
		{
		Model m = ModelIOUtils.modelFromTurtle( spec );
		Resource root = m.createResource( m.expandPrefix( ":my" ) );
		APISpec s = new APISpec( root, NoLoader );
		String x = s.getShortnameService().normalizeNodeToString( "year", "spoo" );
		String eg = m.getNsPrefixURI( "" );
		assertThat( x, is( "'spoo'^^<" + eg + "faketype>" ) );
		}
	
	@Test public void testPlainLiteral() 
		{
		Model m = ModelIOUtils.modelFromTurtle( spec );
		Resource root = m.createResource( m.expandPrefix( ":my" ) );
		APISpec s = new APISpec( root, NoLoader );
		String x = s.getShortnameService().normalizeNodeToString( "name", "Frodo" );
		assertThat( x, is( "'Frodo'" ) );
		}

	}
