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
import com.hp.hpl.jena.vocabulary.RDF;

import static com.epimorphics.lda.support.ReflectionSupport.classForName;
import static com.epimorphics.lda.support.ReflectionSupport.newInstanceOf;

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
		Factories result = rf.copy();
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
		Resource type = getRendererType( r );
		if (type == null) throw new RuntimeException
			(
			"no renderer type for "
			+ (name != null ? name 
			  : mimeType != null ? ("spec with mime type " + mimeType)
			  : "node " + r.toString() )
			);
		RendererFactory rfx = BuiltinRendererTable.getFactory( type ); // BuiltinRendererTable.factoryWithURI( type );
		result.putFactory( name, r, mimeType, pickFactory( className, rfx ), isDefault );
	}

	private static Resource getRendererType( Resource r ) {
		for (RDFNode tn: r.listProperties( RDF.type ).mapWith( Statement.Util.getObject ).toList()) {
			Resource t = tn.asResource();
			if (BuiltinRendererTable.isRendererType( t )) return t;
		}
		return null;
	}

	private static String getMimeType(Resource r) {
		return r.hasProperty( API.mimeType ) ? r.getProperty( API.mimeType ).getString() : "text/plain";
	}

	private static RendererFactory pickFactory( String className, RendererFactory rfx ) {
		if (className != null) return (RendererFactory) newInstanceOf( classForName( className ) );
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
