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

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.bindings.BindingSet;
import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.core.APIEndpointImpl;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.core.APISpec;
import com.epimorphics.lda.core.CallContext;
import com.epimorphics.lda.support.MultiValuedMapSupport;
import com.epimorphics.lda.tests.APITesterUriInfo;
import com.epimorphics.lda.tests_support.LoadsNothing;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
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
    
	@Parameters public static Collection<Object[]> data()
		{
		List<Object[]> result = new ArrayList<Object[]>();
		findTestsFromRoot( result, new File( "src/test/resources/test-tree" ) );
//		System.err.println( ">> " + result.size() + " tests.");
		return result;
		}
	
	private static void findTestsFromRoot( List<Object[]> items, File d ) 
		{
		Pair<String, Model> spec = getModelNamedEnding( d, "-spec.ttl" );
		Pair<String, Model> data = getModelNamedEnding( d, "-data.ttl" );
		log.debug( "considering: " + d );
		if (spec == null || data == null)
			{
			log.debug( "directory " + d + " ignored" );
			}
		else
			{
			for (File f: d.listFiles( endsWith( "-test.ask") )) 
				{
				String fileName = f.toString();
				Query probe = loadQuery( spec.b, fileName );
				String name = fileName.replaceAll( ".*/", "" ).replaceAll( "!.*", "" ).replace( "-test.ask", "" );
				String [] parts = name.split( "\\?" );
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
				w.shouldAppear = probe;
				w.pathToData = d.toString() + "/" + data.a + "-data.ttl";
			//
				items.add( new Object[] {w} );
				}
			}
		if (d != null)
			for (File f: d.listFiles())
				if (f.isDirectory()) findTestsFromRoot( items, f );
		}

	private static Query loadQuery( Model spec, String fileName ) 
		{
		String body = FileManager.get().readWholeFileAsUTF8( fileName );
		String prefixes = sparqlPrefixesFrom( spec );
		return QueryFactory.create( prefixes + body );
		}

	public static class Pair<A, B> 
		{
		public final A a;
		public final B b;
		public Pair(A a, B b) { this.a = a; this.b = b; }		
		}
	
	private static Pair<String, Model> getModelNamedEnding( File d, String end ) 
		{
		File [] specFiles = d.listFiles( endsWith( end ) );
		if (specFiles == null || specFiles.length == 0) 
			{
			return null;
			}
		else if (specFiles.length > 1) 
			{
			return null;
			}
		String specFile = specFiles[0].getPath();
		String name = specFiles[0].getName().replace( end, "" );
		String body = FileManager.get().readWholeFileAsUTF8( specFile );
		return new Pair<String, Model>( name, ModelIOUtils.modelFromTurtle( body ) );
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
		Query shouldAppear;
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
		APISpec s = new APISpec( root, new LoadsNothing() );
		APIEndpoint ep = new APIEndpointImpl( s.getEndpoints().get(0) );        
        MultivaluedMap<String, String> map = MultiValuedMapSupport.parseQueryString( w.queryParams );
		UriInfo ui = new APITesterUriInfo( w.path, map );
		CallContext cc = CallContext.createContext( ui, new BindingSet() );
		APIResultSet rs = ep.call( cc );
//		System.err.println( ">> " + rs.getResultList() );
		QueryExecution qe = QueryExecutionFactory.create( w.shouldAppear, rs );
		if (!qe.execAsk())
			{
			fail
				( "test " + w.title + ": the probe query\n"
				+ shortStringFor( w.shouldAppear ) + "\n"
				+ "failed for the result set\n"
				+ shortStringFor( rs )
				)
				;			
			}
		}

	private String shortStringFor( Model rs ) 
		{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		rs.write( bos, "Turtle" );
		return bos.toString();
		}

	private String shortStringFor( Query q ) 
		{
		return q.toString()
			.replaceAll( "PREFIX [^\n]*\n", "" )
			.replaceAll( "\n *", " " )
			;
		}
	}
