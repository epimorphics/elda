/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.apispec.tests;

import static org.junit.Assert.*;

import org.hamcrest.core.IsNot;
import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.rdfq.Value;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.tests_support.Matchers;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;

public class TestAPISpecExtractsVariables 
	{

	@Test public void testNoVariablesInRDFMeansNoneInSpec()
		{
		testVariableExtraction( "", ":my a api:API; api:sparqlEndpoint :spoo;." );
		}

	@Test public void testAVariableInRDFMeansOneInSpec()
		{
		testVariableExtraction( "fred=17", 
			":my a api:API; api:sparqlEndpoint :spoo; api:variable [api:name 'fred'; api:value 17].");
		}

	@Test public void testTwoVariablesInRDFMeansTwoInSpec()
		{
		testVariableExtraction( "fred=api:value;tom='frodo'", 
			":my a api:API; api:sparqlEndpoint :spoo; api:variable"
			+ " [api:name 'fred'; api:value api:value]"
			+ ", [api:name 'tom'; api:value 'frodo']." 
			);
		}
	
	@Test public void ensureSpotsCircularity() 
		{
		try
			{
			testVariableExtraction( "fred=1;tom=2", // force evaluation
					":my a api:API; api:sparqlEndpoint :spoo; api:variable"
					+ " [api:name 'fred'; api:value '{tom}']"
					+ ", [api:name 'tom'; api:value '{fred}']." 
					);
			}
		catch (RuntimeException e)
			{
			assertTrue( "should trap circular definition", e.getMessage().contains( "circularity" ) );
			}
		}
	
	@Test public void testVariableSubstitution() 
		{
		testVariableExtraction( "fred='17';tom=17", 
				":my a api:API; api:sparqlEndpoint :spoo; api:variable"
				+ " [api:name 'fred'; api:value '{tom}']"
				+ ", [api:name 'tom'; api:value 17]." 
				);
		}
	
	@Test public void ensureEndpointsInheritVariables()
		{
		String spec = 
			":my a api:API; api:sparqlEndpoint :spoo; api:variable"
			+ " [api:name 'fred'; api:value '{tom}']"
			+ ", [api:name 'tom'; api:value 17]"
			+ "; api:endpoint :myE."
			+ "\n"
			+ ":myE a api:ListEndpoint"
			+ ";   api:uriTemplate '/whatsit'" 
			+ "."
			;
		Model m = ModelIOUtils.modelFromTurtle( spec );
		Resource root = m.createResource( m.expandPrefix( ":my" ) );
		APISpec s = SpecUtil.specFrom( root );
		assertThat( s.getEndpoints(), IsNot.not( Matchers.isEmpty() ) );
		for (APIEndpointSpec x: s.getEndpoints())
			assertEqualBindings( s.getBindings(), x.getBindings() );
		}
	
	@Test public void ensureEndpointsAddVariables()
		{
		String spec = 
			":my a api:API; api:sparqlEndpoint :spoo; api:variable"
			+ " [api:name 'fred'; api:value '{tom}']"
			+ ", [api:name 'tom'; api:value 17]"
			+ "; api:endpoint :myE."
			+ "\n"
			+ ":myE a api:ListEndpoint"
			+ ";   api:uriTemplate '/whatsit'" 
			+ "; api:variable [api:name 'harry'; api:value 'x{fred}y']"
			+ "."
			;
		Model m = ModelIOUtils.modelFromTurtle( spec );
		Resource root = m.createResource( m.expandPrefix( ":my" ) );
		APISpec s = SpecUtil.specFrom( root );
		assertThat( s.getEndpoints(), IsNot.not( Matchers.isEmpty() ) );
		assertEqualBindings( binding("tom=17;fred='17';harry='x17y'"), s.getEndpoints().get(0).getBindings() );
		}
	
	public void testVariableExtraction( String expected, String spec ) 
		{
		Model m = ModelIOUtils.modelFromTurtle( spec );
		Resource root = m.createResource( m.expandPrefix( ":my" ) );
		APISpec s = SpecUtil.specFrom( root );
		assertEqualBindings( binding( expected ), s.getBindings() );
		}
	
	private Node term( String term )
		{
		Model m = ModelIOUtils.modelFromTurtle( ":x :value " + term + " ." );
		return m.listStatements().toList().get(0).getObject().asNode();
		}

	private Value asVar( String name, Node n ) 
		{
		if (n.isURI()) return new Value( n.getURI(), "", RDFS.Resource.getURI() );
		if (n.isLiteral()) return new Value
			( n.getLiteralLexicalForm(), n.getLiteralLanguage(), fixNull(n.getLiteralDatatypeURI()) );
		throw new RuntimeException( "cannot convert " + n + " to an RDFQ node" );
		}

	private String fixNull(String u) 
		{ return u == null ? "" : u; }

	private Bindings binding( String desc ) 
		{
		Bindings result = new Bindings();
		if (desc.length() > 0)
			for (String bind: desc.split(" *; *" ))
				{
				String [] parts = bind.split( "=" );
				result.put( parts[0], asVar( parts[0], term( parts[1] ) ) );
				}
		return result;
		}
	
	protected void assertEqualBindings( Bindings expected, Bindings actual )
		{
		if (!expected.sameValuesAs( actual )) 
			fail( "Expected bindings:\n" + expected + "\nbut got:\n" + actual + "\n" );
		}	
	}
