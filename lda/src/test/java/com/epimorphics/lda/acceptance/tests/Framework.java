/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
*/
package com.epimorphics.lda.acceptance.tests;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.bindings.VarValues;
import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.core.APIEndpointImpl;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.core.CallContext;
import com.epimorphics.lda.core.MultiMap;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.tests_support.LoadsNothing;
import com.epimorphics.lda.tests_support.MakeData;
import com.epimorphics.util.Couple;
import com.epimorphics.util.Triad;
import com.epimorphics.util.Util;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.DCTerms;

/**
    Framework for running tests from suite in filing
    system.
*/
@RunWith(Parameterized.class) public class Framework 
	{
	private final WhatToDo w;
	
	public Framework( WhatToDo w )
		{ this.w = w; }

    static Logger log = LoggerFactory.getLogger(Framework.class);
    
    /**
        An empty model. Do not put things into it.
    */
    static final Model emptyModel = ModelFactory.createDefaultModel();
    
	@Parameters public static Collection<Object[]> data()
		{
		List<Object[]> result = new ArrayList<Object[]>();
		findTestsFromRoot( result, emptyModel, emptyModel, new File( "src/test/resources/test-tree" ) );
//		System.err.println( ">> " + result.size() + " tests.");
		return result;
		}
	
	/**
	    An ASK query, with the expected result: true (Positive) or
	    false (!isPositive).
	*/
	static class Ask 
		{
		boolean isPositive;
		Query ask;
		
		public Ask( boolean isPositive, Query ask ) 
			{ this.isPositive = isPositive; this.ask = ask;	}
		}
	
	private static void findTestsFromRoot( List<Object[]> items, Model givenSpec, Model givenData, File d ) 
		{
		Couple<String, Model> spec = getModelNamedEnding( d, givenSpec, "-spec.ttl" );
		Couple<String, Model> data = getModelNamedEnding( d, givenData, "-data.ttl" );
		log.debug( "considering: " + d );
		if (spec == null || data == null)
			{
			System.err.println( ">> " + "directory " + d + " ignored" );
			log.debug( "directory " + d + " ignored" );
			}
		else
			{
			for (File f: d.listFiles( endsWith( "-test.ask") )) 
				{
				String fileName = f.toString();
				List<Couple<String, List<Ask>>> urisAndQuerys = loadQueries( spec.b, fileName );
				for (Couple<String, List<Ask>> uriAndQuery: urisAndQuerys) 
					{
					List<Ask> probes = uriAndQuery.b;
					String uri = uriAndQuery.a;
					String [] parts = uri.split( "\\?" );
					String path = parts[0].replaceAll( "_", "/" );
					String queryParams = parts.length > 1 ? parts[1] : "";
				//
					WhatToDo w = new WhatToDo();
					w.title = 
						"from directory: " + d.toString() 
						+ ", spec file: " + spec.a 
						+ ", query path: " + path 
						+ ", params: " + queryParams
						;
					w.specModel = spec.b;
					w.path = path;
					w.queryParams = queryParams;
					w.shouldAppear = probes;
					w.pathToData = d.toString() + "/" + data.a + "-data.ttl";
				//
					items.add( new Object[] {w} );
					}
				}
			}
		if (d != null)
			for (File f: d.listFiles())
				if (f.isDirectory()) findTestsFromRoot( items, spec.b, data.b, f );
		}

	private static List<Couple<String, List<Ask>>> loadQueries( Model spec, String fileName ) 
		{
		List<Couple<String, List<Ask>>> result = new ArrayList<Couple<String,List<Ask>>>();
//		System.err.println( ">> loading query " + fileName );
		String wholeFile = FileManager.get().readWholeFileAsUTF8( fileName );
		
		String [] elements = wholeFile.split( "(^|\n)URI=" );
		for (String element: elements)
			if (element.length() > 0)
				{
				String [] parts = element.split( "\n", 2 );
				String uri = parts[0], queries = parts[1];
				String prefixes = sparqlPrefixesFrom( spec );
				List<Ask> asks = getQueries( queries, prefixes);
				result.add( new Couple<String, List<Ask>>( uri, asks ) );
				}
		return result;
		}

	private static List<Ask> getQueries( String query, String prefixes ) {
		List<Ask> result = new ArrayList<Ask>();
		for (String s: query.split( "(\n|^)ASK" ))
			if (s.length() > 0)
				{
				boolean isPositive = true;
				if (s.startsWith( " NOT" )) { isPositive = false; s = s.substring(4); }
				result.add( new Ask( isPositive, getQuery( prefixes, s ) ) );
				}
		return result;
	}

	private static Query getQuery(String prefixes, String s) 
		{
		try { return QueryFactory.create( prefixes + "ASK " + s ); }
		catch (Exception e) { throw new RuntimeException( "Could not parse query: " + s, e ); }
		}

	/**
	   Answer the name and contents of the model ending with <code>end</code>.
	   If the name of the model starts 'and-', the result model includes the
	   model from the previous layer up the directory tree.
	*/
	private static Couple<String, Model> getModelNamedEnding( File d, Model given, String end ) 
		{
		File [] specFiles = d.listFiles( endsWith( end ) );
		if (specFiles.length == 1) 
			{
			String specFile = specFiles[0].getPath();
			String name = specFiles[0].getName().replace( end, "" );
			return new Couple<String, Model>( name, loadSpecFile( specFile ) );
			}
		else
			return new Couple<String, Model>( "inherited", given );
		}

	private static Model loadSpecFile( String specFile ) 
		{
		String body = FileManager.get().readWholeFileAsUTF8( specFile );
		try 
			{ return ModelIOUtils.modelFromTurtle( body ); }
		catch (Exception e) 
			{ throw new RuntimeException( "Error loading " + specFile, e ); }
		}

	private static FilenameFilter endsWith( final String end ) 
		{ 
		return new FilenameFilter() 
			{
			@Override public boolean accept( File dir, String name ) 
				{ return name.endsWith( end ); }			
			};
		}
	
	private static String sparqlPrefixesFrom( Model m ) 
		{
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> e: m.getNsPrefixMap().entrySet())
			sb
				.append( "PREFIX " ).append( e.getKey() ).append( ": " )
				.append( "<" ).append( e.getValue() ).append( ">" )
				.append( "\n" )
				;
		sb.append( "PREFIX dcterms: <" + DCTerms.getURI() + ">\n" );
		return sb.toString();
		}

	public static class WhatToDo
		{
		String title;
		String pathToData;
		Model specModel;
		String path;
		String queryParams;
		List<Ask> shouldAppear;
		}
	
	@Test public void RUN()
		{ 
		// Messing around with exceptions because JUnit's Parameterized
		// runner doesn't display a decent test name. So we bash out the
		// test title if it fails.
		try
			{ 
			RunTestAllowingFailures(); 
			}
		catch (RuntimeException e)
			{
			System.err.println( ">> test " + w.title + " FAILED." );
			throw e;
			}
		catch (Error e)
			{
			System.err.println( ">> test " + w.title + " FAILED." );
			throw e;
			}
		}
	
	public void RunTestAllowingFailures()
		{
		log.debug( "running test " + w.title );
//		System.err.println( "running test " + w.title );
//		System.err.println( ">> " + w.pathToData );
		FileManager.get().getLocationMapper().addAltEntry( "CURRENT-TEST", w.pathToData );
		Model specModel = w.specModel;
		Resource root = specModel.createResource( specModel.expandPrefix( ":root" ) );
		APISpec s = new APISpec( root, LoadsNothing.instance );
		APIEndpoint ep = new APIEndpointImpl( s.getEndpoints().get(0) );        
		MultiMap<String, String> map = MakeData.parseQueryString( w.queryParams );
		CallContext cc = CallContext.createContext( Util.newURI(w.path), map, new VarValues() );
		Triad<APIResultSet, String, CallContext> resultsAndFormat = ep.call( cc );
		Model rsm = resultsAndFormat.a.getModel();
//		System.err.println( ">> " + rs.getResultList() );
		for (Ask a: w.shouldAppear)
			{
//			System.err.println( ">>  asking ... " + (a.isPositive ? "ASSERT" : "DENY") );
			QueryExecution qe = QueryExecutionFactory.create( a.ask, rsm );
			if (qe.execAsk() != a.isPositive)
				{
				fail
					( "test " + w.title + ": the probe query\n"
					+ shortStringFor( a ) + "\n"
					+ "failed for the result set\n"
					+ shortStringFor( rsm )
					)
					;			
				}
			}
		}

	private String shortStringFor( Model rs ) 
		{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		rs.write( bos, "Turtle" );
		return bos.toString();
		}
	
	private String shortStringFor( Ask a )
		{
		StringBuilder result = new StringBuilder();
		result.append( a.isPositive ? "POSITIVE: " : "NEGATIVE: " );
		result.append( a.ask.toString().replaceAll( "PREFIX [^\n]*\n", "" ).replaceAll( "\n *", " " ) );
		return result.toString();
		}
	}
