/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) copyright Epimorphics Limited 2010
    $Id$
*/

package com.epimorphics.lda.tests_support;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.epimorphics.lda.bindings.Binding;
import com.epimorphics.lda.bindings.BindingSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;

/**
    Helper code to construct String->String hash maps from tiny strings
    and models from longer strings.
 
 	@author chris
*/
public class MakeData 
	{
	/**
	    Answer a hashmap based on the bindings: k1=v1 k2=v2 ...
	*/
	public static Map<String, String> hashMap( String bindings ) 
		{ return hashMap( bindings, " +" ); }

	/**
	    Answer a hashmap based on the bindings: k1=v1 snip k2=v2 ...
	*/
	public static Map<String, String> hashMap( String bindings, String snip ) 
		{
		Map<String, String> result = new HashMap<String, String>();
		if (bindings.length() > 0)
			for (String b: bindings.split( snip ))
				{
				String [] parts = b.split( "=" );
				result.put( parts[0], parts[1] );
				}
		return result;
		}

	/**
	    Answer a model based on the given triples in ModelTestBase
	    style, with all the prefixes appropriate to this text environment. 
	*/
	public static Model specModel( String triples ) 
		{
		Model m = ModelFactory.createDefaultModel();
		m.setNsPrefix( "rdf",  "http://www.w3.org/1999/02/22-rdf-syntax-ns#" );
		m.setNsPrefix( "rdfs", "http://www.w3.org/2000/01/rdf-schema#" );
		m.setNsPrefix( "owl",  "http://www.w3.org/2002/07/owl#" );
		m.setNsPrefix( "api",  "http://purl.org/linked-data/api/vocab#" );
		m.setNsPrefix( "ex",   "http://www.epimorphics.com/examples/eg1#" );
		m.setNsPrefix( "spec", "http://www.epimorphics.com/examples/spec1#" );
		m.setNsPrefix( "foaf", "http://xmlns.com/foaf/0.1/" );
		m.setNsPrefix( "xsd", " http://www.w3.org/2001/XMLSchema#" );
		m.setNsPrefix( "school-ont", "http://education.data.gov.uk/def/school/" );
		return ModelTestBase.modelAdd( m, triples );
		}

	public static Properties properties( String bindings ) 
		{
		Properties result = new Properties();
		Map<String, String> h = hashMap( bindings );
		for (Map.Entry<String, String> e: h.entrySet()) result.put( e.getKey(), e.getValue() );
		return result;
		}

	public static BindingSet variables( String bindings ) 
		{
		BindingSet result = new BindingSet();
		if (bindings.length() > 0)
			for (String b: bindings.split( " +" ))
				{
				String [] parts = b.split( "=" );
				result.put( parts[0], new Binding( parts[0], "", "", parts[1] ) );
				}
		return result;
		}
	}
