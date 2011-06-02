package com.epimorphics.lda.core;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Set;

import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.renderers.Factories;
import com.epimorphics.lda.shortnames.NameMap;
import com.epimorphics.lda.shortnames.NameMap.Stage2NameMap;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.vocabularies.ELDA;
import com.epimorphics.lda.vocabularies.SPARQL;
import com.epimorphics.vocabs.API;
import com.epimorphics.vocabs.FIXUP;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
    Class to handle the construction of metadata for API endpoint results.
    Bit of a hotchpotch at the moment.
    
    @author Chris    
*/
public class EndpointMetadata {

	public static void addVersions( Model m, Set<String> viewNames, CallContext c, Resource aPage ) {
		Resource page = aPage.inModel( m );
		for (String viewName: viewNames) {
			if (!viewName.equals( View.SHOW_DEFAULT_INTERNAL )) {
	    		Resource v = EndpointMetadata.resourceForView( m, c, viewName );
				page.addProperty( DCTerms.hasVersion, v	);
				v.addProperty( DCTerms.isVersionOf, page );
				v.addProperty( RDFS.label, viewName );
			}
    	}
	}

    public static Resource resourceForView( Model m, CallContext context, String name ) {
    	URI req = context.getRequestURI();
    	return m.createResource( replaceQueryParam( req, QueryParameter._VIEW, name ) );
    }

	public static String replaceQueryParam(URI ru, String key, String... values) {
		try {
			String q = ru.getQuery();
			String newQuery = q == null ? "" : strip( q, key );
			String and = newQuery.isEmpty() ? "" : "&";
			for (String value: values) {
				newQuery = newQuery + and + key + "=" + quoteForValue(value);
				and = "&";
			}
			return new URI
				(
				ru.getScheme(), 
				ru.getAuthority(), 
				ru.getPath(),
				(newQuery.isEmpty() ? null : newQuery), 
				ru.getFragment() 
				).toASCIIString();
		} catch (URISyntaxException e) {			
			throw new EldaException( "created a broken URI", "", EldaException.SERVER_ERROR, e );
		}
	}

	public static String quoteForValue(String value) {
		return value.replace( "&", "%??" );
	}

	public static String strip( String query, String key ) {
		return query.replaceAll( "(^|[&])" + key + "=[^&]*[&]?", "" );
	}

	// TODO should only substitute .foo if it's a renderer or language
	public static String replaceSuffix( String key, String oldPath ) {
		int dot_pos = oldPath.lastIndexOf( '.' ), slash_pos = oldPath.lastIndexOf( '/' );
		return dot_pos > -1 && dot_pos > slash_pos
			? oldPath.substring(0, dot_pos + 1) + key
			: oldPath + "." + key
			;
	}

	public static Resource resourceForFormat( Model m, CallContext c, String formatName ) {
		URI ru = c.getRequestURI();
		try {
			URI x = new URI
				( ru.getScheme()
				, ru.getAuthority()
				, EndpointMetadata.replaceSuffix( formatName, ru.getPath() )
				, ru.getQuery()
				, ru.getFragment() 
				);
			return m.createResource( x.toASCIIString() );
		} catch (URISyntaxException e) {
			throw new EldaException( "created a broken URI", "", EldaException.SERVER_ERROR, e );
		}
	}

	public static void addFormats(Model m, CallContext c, Resource thisPage, Factories f) {
		for (String formatName: f.formatNames()) 
			if (formatName.charAt(0) != '_') {
				String typeForName = f.getTypeForName( formatName ).toString(); 
				Resource v = EndpointMetadata.resourceForFormat( m, c, formatName );
				Resource format = m.createResource().addProperty( RDFS.label, typeForName );
				thisPage.addProperty( DCTerms.hasFormat, v );
				v.addProperty( DCTerms.isFormatOf, thisPage );
				v.addProperty( DCTerms.format, format );
				v.addProperty( RDFS.label, formatName );
			}
	}

	public static void addBindings( Model m, Resource anExec, NameMap nm, CallContext cc, Resource aPage ) {
		Resource exec = anExec.inModel(m), page = aPage.inModel(m);
		exec.addProperty( RDF.type, FIXUP.Execution );
		EndpointMetadata.addVariableBindings( m, exec, cc );
		EndpointMetadata.addTermBindings( m, exec, nm );
		page.addProperty( FIXUP.wasResultOf, exec );
	}

	public static void addVariableBindings(Model rsm, Resource exec, CallContext cc) {
		for (Iterator<String> names = cc.parameters.keyIterator(); names.hasNext();) {
			String name = names.next();
			Resource vb = rsm.createResource();
			vb.addProperty( FIXUP.label, name );
			vb.addProperty( FIXUP.value, cc.getStringValue( name ) );
			exec.addProperty( FIXUP.VB, vb );
		}
	}

	public static void addTermBindings( Model rsm, Resource exec, NameMap nm ) {
		Stage2NameMap s2 = nm.stage2(false);
		MultiMap<String, String> mm = s2.result();
		for (String uri: mm.keySet()) {
			Resource term = rsm.createResource( uri );
			if (rsm.containsResource( term )) {
				Set<String> shorties = mm.getAll( uri );
				String shorty = shorties.iterator().next();
				if (shorties.size() > 1) {
					APIEndpointImpl.log.warn( "URI <" + uri + "> has several short names, viz: " + shorties + "; picked " + shorty );
				}
	    		Resource tb = rsm.createResource();
	    		exec.addProperty( FIXUP.TB, tb );
				tb.addProperty( FIXUP.label, shorty );
				tb.addProperty( API.property, term );
			}
		}
	}

	// following the Puelia model.
	public static void addExecution( Model rsm, Resource exec, CallContext cc, Resource thisPage ) {
		exec.addProperty( RDF.type, FIXUP.Execution );
		Resource P = rsm.createResource();
		ELDA.addEldaMetadata( P );
		exec.addProperty( FIXUP.processor, P );
		thisPage.addProperty( FIXUP.wasResultOf, exec );
	}

	public static void addQueryMetadata( Model rsm, Resource exec, CallContext context, APIQuery q, String detailsQuery, APISpec apiSpec, boolean listEndpoint ) {
		if (listEndpoint) {
	    	Resource sr = rsm.createResource();
	    	sr.addProperty( RDF.type, SPARQL.QueryResult );    	
	    	sr.addProperty( SPARQL.query, EndpointMetadata.inValue( rsm, q.getQueryString( apiSpec, context ) ) );
	    	exec.addProperty( FIXUP.selectionResult, sr );
		}
	//
		Resource EP = rsm.createResource();
		EP.addProperty( RDF.type, SPARQL.Service );
		apiSpec.getDataSource().addMetadata( EP ); 
		Resource url = EP.getProperty( API.sparqlEndpoint ).getResource(); 
		EP.addProperty( SPARQL.url, url );
	//
		Resource vr = rsm.createResource();
		vr.addProperty( RDF.type, SPARQL.QueryResult );
		vr.addProperty( SPARQL.query, EndpointMetadata.inValue( rsm, detailsQuery ) ); 
		vr.addProperty( SPARQL.endpoint, EP );
		exec.addProperty( FIXUP.viewingResult, vr );
	}

	public static Resource inValue( Model rsm, String s ) {
		return rsm.createResource().addProperty( RDF.value, s );
	}
}