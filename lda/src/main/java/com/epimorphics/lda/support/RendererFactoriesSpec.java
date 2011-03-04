/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.support;

import java.util.Map;

import com.epimorphics.lda.renderers.BuiltinRendererTable;
import com.epimorphics.lda.renderers.RendererFactory;
import com.epimorphics.lda.vocabularies.EXTRAS;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class RendererFactoriesSpec {

	/**
	 	Answers a renderer factory table with the built-in formaters and additional
	 	entries from the api:formatter properties of <code>endpoint</code>.
	*/
	public static Map<String, RendererFactory> createFactoryTable( Resource endpoint ) {
		return createFactoryTable( endpoint, BuiltinRendererTable.getBuiltinRenderers() );
	}

	/**
	 	Answers a renderer factory table by updating <code>rf</code> with additional
	 	entries from the api:formatter properties of <code>endpoint</code>.
	*/
	public static Map<String, RendererFactory> createFactoryTable( Resource endpoint, Map<String, RendererFactory> rf ) {
		Map<String, RendererFactory> result = BuiltinRendererTable.getBuiltinRenderers();
		for (RDFNode n: endpoint.listProperties( API.formatter ).mapWith( Statement.Util.getObject ).toList()) {
			Resource r = (Resource) n;
			if (r.hasProperty( API.name ) && r.hasProperty( EXTRAS.className )) {
				String name = r.getProperty( API.name ).getString();
				String className = r.getProperty( EXTRAS.className ).getString();
				Class<?> c = ReflectionSupport.classForName( className );
				result.put( name, (RendererFactory) ReflectionSupport.newInstanceOf(c) );
			}
		}
		return result;
	}

}
