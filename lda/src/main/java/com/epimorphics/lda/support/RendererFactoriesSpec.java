/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.support;

import java.util.List;

import com.epimorphics.lda.renderers.BuiltinRendererTable;
import com.epimorphics.lda.renderers.Factories;
import com.epimorphics.lda.renderers.RendererFactory;
import com.epimorphics.lda.vocabularies.EXTRAS;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.NotFoundException;

public class RendererFactoriesSpec {

	/**
	 	Answers a renderer factory table with the built-in formaters and additional
	 	entries from the api:formatter properties of <code>endpoint</code>.
	*/
	public static Factories createFactoryTable( Resource endpoint ) {
		return createFactoryTable( endpoint, BuiltinRendererTable.getBuiltinRenderers() );
	}

	/**
	 	Answers a renderer factory table by updating <code>rf</code> with additional
	 	entries from the api:formatter properties of <code>endpoint</code>.
	*/
	public static Factories createFactoryTable( Resource endpoint, Factories rf ) {
		Factories result = BuiltinRendererTable.getBuiltinRenderers();
		for (RDFNode n: formattersOf( endpoint )) {
			addEntry( result, n.asResource(), false );
		}
		if (endpoint.hasProperty( API.defaultFormatter)) {
			Resource r = endpoint.getProperty( API.defaultFormatter ).getObject().asResource();
			addEntry( result, r, true );
		}
		return result;
	}

	private static void addEntry( Factories result, Resource r, boolean isDefault ) {
		String name = getName( r );
		String className = getClassName( r );
		String mimeType = getMimeType( r );
		RendererFactory rfx = BuiltinRendererTable.factoryWithURI( r );
		result.putFactory( name, r, mimeType, pickFactory( className, rfx ), isDefault );
	}

	private static String getMimeType(Resource r) {
		return r.hasProperty( EXTRAS.mediaType ) ? r.getProperty( EXTRAS.mediaType ).getString() : "text/plain";
	}

	private static RendererFactory pickFactory( String className, RendererFactory rfx ) {
		if (className != null) return (RendererFactory) ReflectionSupport.newInstanceOf( ReflectionSupport.classForName( className ) );
		if (rfx != null) return rfx;
		throw new NotFoundException( "renderer class: name" );
	}

	private static String getClassName(Resource r) {
		return r.hasProperty( EXTRAS.className ) ? r.getProperty( EXTRAS.className ).getString() : null;
	}

	private static String getName( Resource r ) {
		return r.hasProperty( API.name ) ? r.getProperty( API.name ).getString() : null;
	}

	private static List<RDFNode> formattersOf( Resource endpoint ) {
		return endpoint.listProperties( API.formatter ).mapWith( Statement.Util.getObject ).toList();
	}

}
