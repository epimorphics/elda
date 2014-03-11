/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
*/
package com.epimorphics.lda.acceptance.tests;

import static org.junit.Assert.fail;

import java.io.*;
import java.net.URI;
import java.util.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.apispec.tests.SpecUtil;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.cache.Cache;
import com.epimorphics.lda.core.*;
import com.epimorphics.lda.routing.MatchSearcher;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.support.*;
import com.epimorphics.lda.tests_support.MakeData;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.util.*;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.LocationMapper;
import com.hp.hpl.jena.vocabulary.DCTerms;

/**
    Framework for running tests from suite in filing
    system.
*/
@RunWith(Parameterized.class) public class TestFramework 
	{
	private final WhatToDo w;
	
	public TestFramework( WhatToDo w )
		{ this.w = w; }

    static Logger log = LoggerFactory.getLogger(TestFramework.class);
    
	static final Controls controls = new Controls( true, new Times() );
    
    /**
        An empty model. Do not put things into it.
    */
    static final Model emptyModel = ModelFactory.createDefaultModel();
    
	@Parameters public static Collection<Object[]> data()
		{ return data( "" ); }
	
	public static Collection<Object[]> data( String path )
		{
		List<Object[]> result = new ArrayList<Object[]>();
		findTestsFromRoot( result, emptyModel, emptyModel, new File( "src/test/resources/test-tree" + path ) );
//		findTestsFromRoot( result, emptyModel, emptyModel, new File( "src/test/resources/test-tree/elda-talk-example" ) );
//		System.err.println( ">> " + result.size() + " tests.");
		return result;
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
				List<UriAndAsks> urisAndQuerys = loadQueries( spec.b, fileName );
				String uriTemplate = getUriTemplate( spec.b );
				for (UriAndAsks uriAndQuery: urisAndQuerys) 
					{
					List<Ask> probes = uriAndQuery.asks;
					String uri = uriAndQuery.uri;
					String [] parts = uri.split( "\\?" );
					String path = parts[0].replaceAll( "_", "/" );
					String queryParams = parts.length > 1 ? parts[1] : "";
				//
					WhatToDo w = new WhatToDo();
					w.template = uriTemplate;
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

	private static String getUriTemplate( Model spec ) {
		StmtIterator s = spec.listStatements( null, API.uriTemplate, (RDFNode) null );
		if (!s.hasNext()) throw new RuntimeException( "Did not find uri template in test config." );
		String template = s.next().getString();
		if (s.hasNext()) throw new RuntimeException( "Multiple uri templates in test config." );
		return template;
	}

	private static List<UriAndAsks> loadQueries( Model spec, String fileName ) 
		{
		List<UriAndAsks> result = new ArrayList<UriAndAsks>();
//		System.err.println( ">> loading query " + fileName );
		String wholeFile = EldaFileManager.get().readWholeFileAsUTF8( fileName );
		String [] elements = wholeFile.split( "(^|\n)URI=" );
		for (String element: elements)
			if (element.length() > 0)
				{
				String [] parts = element.split( "\n", 2 );
				String uri = parts[0], queries = parts[1];
				String prefixes = sparqlPrefixesFrom( spec );
				List<Ask> asks = getQueries( queries, prefixes);
				result.add( new UriAndAsks( uri, asks ) );
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
		String body = EldaFileManager.get().readWholeFileAsUTF8( specFile );
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
		Cache.Registry.clearAll();
		log.debug( "running test " + w.title );
//		System.err.println( ">> " + w.pathToData );
	//
	// this little dance of resetting the location mapper bypasses a
	// problem that hits a null pointer exception. FileManager issue?
	//
		EldaFileManager.get().setLocationMapper( new LocationMapper() );
		EldaFileManager.get().getLocationMapper().addAltEntry( "CURRENT-TEST", w.pathToData );
	//
		Model specModel = w.specModel;
		Resource root = specModel.createResource( specModel.expandPrefix( ":root" ) );
		APISpec s = SpecUtil.specFrom( root );
		APIEndpoint ep = new APIEndpointImpl( s.getEndpoints().get(0) ); 
		Bindings epBindings = ep.getSpec().getBindings();
		MultiMap<String, String> map = MakeData.parseQueryString( w.queryParams );
		URI ru = URIUtils.newURI(w.path);
		Bindings cc = Bindings.createContext( bindTemplate( epBindings, w.template, w.path, map ), map );
		ResponseResult resultsAndFormat = ep.call( new APIEndpoint.Request( controls, ru, cc ), new NoteBoard() );
		Model rsm = resultsAndFormat.resultSet.getMergedModel();
//		System.err.println( ">> " + rs.getResultList() );				
//		System.err.println( "||>> " + resultsAndFormat.a.getSelectQuery() );

		for (Ask a: w.shouldAppear)
			{
//			System.err.println( ">>  asking ... " + (a.isPositive ? "ASSERT" : "DENY") );
			QueryExecution qe = QueryExecutionFactory.create( a.ask, rsm );
			if (qe.execAsk() != a.isPositive)
				{
//				System.err.println( ">> WHOOPS------------------------------------__" );
//				System.err.println( ">> path: " + w.path );
//				System.err.println( ">> qp: " + w.queryParams );
//				System.err.println( ">> template: " + w.template );
//				System.err.println( ">> ------------------------------------------__" );
//				System.err.println( resultsAndFormat.a.getSelectQuery() );
//				System.err.println( ">> ------------------------------------------__" );
//				System.err.println( ">> cc = " + cc );
//				System.err.println( ">> ------------------------------------------__" );
				// System.err.println( ">>\n>> Failing result model for " + w.title + ":" );
				// rsm.write( System.err, "TTL" );
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

	// this seems a bit tedious. There should be a more straightforward way.
	private Bindings bindTemplate( Bindings epBindings, String template, String path, MultiMap<String, String> qp ) {
		MatchSearcher<String> ms = new MatchSearcher<String>();
		ms.register( template, "IGNORED" );
		Map<String, String> bindings = new HashMap<String, String>();
		ms.lookup( bindings, path, qp );
		return epBindings.updateAll( bindings ); 
	}

	public static String shortStringFor( Model rs ) 
		{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		rs.write( bos, "Turtle" );
		return bos.toString();
		}
	
	public static String shortStringFor( Ask a )
		{
		StringBuilder result = new StringBuilder();
		result.append( a.isPositive ? "POSITIVE: " : "NEGATIVE: " );
		result.append( a.ask.toString().replaceAll( "PREFIX [^\n]*\n", "" ).replaceAll( "\n *", " " ) );
		return result.toString();
		}
	}
