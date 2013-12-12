/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.support;

import static com.epimorphics.lda.support.ReflectionSupport.classForName;
import static com.epimorphics.lda.support.ReflectionSupport.newInstanceOf;

import java.util.List;

import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.renderers.*;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.lda.vocabularies.EXTRAS;
import com.epimorphics.util.MediaType;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;

public class RendererFactoriesSpec {

	/**
	 	Answers a renderer factory table with the built-in formatters and additional
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
		MediaType mt = getMimeType( r );
		Resource type = getRendererType( r );		
		if (type == null) EldaException.BadSpecification
			(
			"no renderer type for "
			+ (name != null ? name 
			  : mt != null ? ("spec with mime type " + mt)
			  : "node " + r.toString() )
			);
		RendererFactory rfx = BuiltinRendererTable.getFactory( type ); 
		RendererFactory fac = pickFactory( className, rfx );
		result.putFactory( name, r, mt, fac, isDefault );
	}

	private static Resource getRendererType( Resource r ) {
		for (RDFNode tn: r.listProperties( RDF.type ).mapWith( Statement.Util.getObject ).toList()) {
			Resource t = tn.asResource();
			if (BuiltinRendererTable.isRendererType( t )) return t;
		}
		return null;
	}

	private static MediaType getMimeType(Resource r) {
		return r.hasProperty( API.mimeType ) 
			? MediaType.decodeType( r.getProperty( API.mimeType ).getString() ) 
			: MediaType.TEXT_PLAIN;
	}

	private static RendererFactory pickFactory( String className, RendererFactory rfx ) {
		if (className != null) return (RendererFactory) newInstanceOf( classForName( className ) );
		if (rfx == null) EldaException.NotFound( "renderer class", className );
		return rfx;
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
