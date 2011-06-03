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

	protected final CallContext cc;
	protected final Resource thisPage;
	
	public EndpointMetadata( Resource thisPage, CallContext cc ) {
		this.cc = cc;
		this.thisPage = thisPage;
	}
	
	public void addVersions( Model m, Set<String> viewNames ) {
		Resource page = thisPage.inModel( m );
		for (String viewName: viewNames) {
			if (!viewName.equals( View.SHOW_DEFAULT_INTERNAL )) {
	    		Resource v = EndpointMetadata.resourceForView( m, cc, viewName );
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

	public void addFormats( Model meta, Factories f ) {
		Resource page = thisPage.inModel(meta);
		for (String formatName: f.formatNames()) 
			if (formatName.charAt(0) != '_') {
				String typeForName = f.getTypeForName( formatName ).toString(); 
				Resource v = EndpointMetadata.resourceForFormat( meta, cc, formatName );
				Resource format = meta.createResource().addProperty( RDFS.label, typeForName );
				page.addProperty( DCTerms.hasFormat, v );
				v.addProperty( DCTerms.isFormatOf, thisPage );
				v.addProperty( DCTerms.format, format );
				v.addProperty( RDFS.label, formatName );
			}
	}

	public void addBindings( Model toScan, Model meta, Resource anExec, NameMap nm ) {
		Resource exec = anExec.inModel(meta), page = thisPage.inModel(meta);
		exec.addProperty( RDF.type, FIXUP.Execution );
		EndpointMetadata.addVariableBindings( meta, exec, cc );
		EndpointMetadata.addTermBindings( toScan, meta, exec, nm );
		page.addProperty( FIXUP.wasResultOf, exec );
	}

	public static void addVariableBindings( Model meta, Resource exec, CallContext cc) {
		for (Iterator<String> names = cc.parameters.keyIterator(); names.hasNext();) {
			String name = names.next();
			Resource vb = meta.createResource();
			vb.addProperty( FIXUP.label, name );
			vb.addProperty( FIXUP.value, cc.getStringValue( name ) );
			exec.addProperty( FIXUP.VB, vb );
		}
	}

	public static void addTermBindings( Model toScan, Model meta, Resource exec, NameMap nm ) {
		Stage2NameMap s2 = nm.stage2(false);
		MultiMap<String, String> mm = s2.result();
		for (String uri: mm.keySet()) {
			Resource term = meta.createResource( uri );
			if (toScan.containsResource( term )) {
				Set<String> shorties = mm.getAll( uri );
				String shorty = shorties.iterator().next();
				if (shorties.size() > 1) {
					APIEndpointImpl.log.warn( "URI <" + uri + "> has several short names, viz: " + shorties + "; picked " + shorty );
				}
	    		Resource tb = meta.createResource();
	    		exec.addProperty( FIXUP.TB, tb );
				tb.addProperty( FIXUP.label, shorty );
				tb.addProperty( API.property, term );
			}
		}
	}

	// following the Puelia model.
	public void addExecution( Model meta, Resource anExec ) {
		Resource exec = anExec.inModel(meta), page = thisPage.inModel(meta);
		exec.addProperty( RDF.type, FIXUP.Execution );
		Resource P = meta.createResource();
		ELDA.addEldaMetadata( P );
		exec.addProperty( FIXUP.processor, P );
		page.addProperty( FIXUP.wasResultOf, exec );
	}

	public void addQueryMetadata( Model meta, Resource anExec, APIQuery q, String detailsQuery, APISpec apiSpec, boolean listEndpoint ) {
		Resource exec = anExec.inModel(meta);
		if (listEndpoint) {
	    	Resource sr = meta.createResource();
	    	sr.addProperty( RDF.type, SPARQL.QueryResult );    	
	    	sr.addProperty( SPARQL.query, EndpointMetadata.inValue( meta, q.getQueryString( apiSpec, cc ) ) );
	    	exec.addProperty( FIXUP.selectionResult, sr );
		}
	//
		Resource EP = meta.createResource();
		EP.addProperty( RDF.type, SPARQL.Service );
		apiSpec.getDataSource().addMetadata( EP ); 
		Resource url = EP.getProperty( API.sparqlEndpoint ).getResource(); 
		EP.addProperty( SPARQL.url, url );
	//
		Resource vr = meta.createResource();
		vr.addProperty( RDF.type, SPARQL.QueryResult );
		vr.addProperty( SPARQL.query, EndpointMetadata.inValue( meta, detailsQuery ) ); 
		vr.addProperty( SPARQL.endpoint, EP );
		exec.addProperty( FIXUP.viewingResult, vr );
	}

	public static Resource inValue( Model rsm, String s ) {
		return rsm.createResource().addProperty( RDF.value, s );
	}
}