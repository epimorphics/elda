/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
*/
package com.epimorphics.lda.core;

import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.query.APIQuery;
import com.epimorphics.lda.query.QueryParameter;
import com.epimorphics.lda.renderers.Factories;
import com.epimorphics.lda.shortnames.NameMap;
import com.epimorphics.lda.shortnames.NameMap.Stage2NameMap;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.vocabularies.ELDA;
import com.epimorphics.lda.vocabularies.SPARQL;
import com.epimorphics.util.URIUtils;
import com.epimorphics.vocabs.API;
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

	protected final Bindings cc;
	protected final Resource thisPage;
	protected final String pageNumber;
	protected final Set<String> formatNames;
	protected final boolean isListEndpoint;
	protected final URI pageURI; 
	
	public EndpointMetadata( Resource thisPage, boolean isListEndpoint, String pageNumber, Bindings cc, URI pageURI, Set<String> formatNames ) {
		this.cc = cc;
		this.pageURI = pageURI;
		this.thisPage = thisPage;
		this.pageNumber = pageNumber;
		this.formatNames = formatNames;
		this.isListEndpoint = isListEndpoint;
	}
	
	/**
	    Create metadata describing the alternative views available
	    for this endpoint, given their names.
	*/
	public void addVersions( Model m, Set<String> viewNames ) {
		Resource page = thisPage.inModel( m );
		for (String viewName: viewNames) {
			if (!viewName.equals( View.SHOW_DEFAULT_INTERNAL )) {
	    		Resource v = resourceForView( m, viewName );
				page.addProperty( DCTerms.hasVersion, v	);
				v.addProperty( DCTerms.isVersionOf, page );
				v.addProperty( RDFS.label, viewName );
			}
    	}
	}

	/**
	 	Answer the URL which is the request URL from the context
	 	modified by replacing the _view with the requested name.
	*/
    private Resource resourceForView( Model m, String name ) {
    	URI a = URIUtils.replaceQueryParam( pageURI, QueryParameter._VIEW, name );
    	URI b = isListEndpoint ? URIUtils.replaceQueryParam( a, QueryParameter._PAGE, pageNumber ) : a;
		return m.createResource( b.toString() );
    }

	private Resource resourceForFormat( URI reqURI, Model m, Set<String> knownFormats, String formatName ) {
		URI u = URIUtils.changeFormatSuffix(reqURI, knownFormats, formatName);
		return m.createResource( u.toString() );
	}

	/**
	    Create metadata which describes the available alternative formats
	    this page could be presented in.
	*/
	public void addFormats( Model meta, Factories f ) {
		Resource page = thisPage.inModel(meta);
		for (String formatName: f.formatNames()) 
			if (formatName.charAt(0) != '_') {
				String typeForName = f.getTypeForName( formatName ).toString(); 
				Resource v = resourceForFormat( pageURI, meta, formatNames, formatName );
				Resource format = meta.createResource().addProperty( RDFS.label, typeForName );
				page.addProperty( DCTerms.hasFormat, v );
				v.addProperty( DCTerms.isFormatOf, thisPage );
				v.addProperty( DCTerms.format, format );
				v.addProperty( RDFS.label, formatName );
			}
	}

	public void addBindings( Model toScan, Model meta, Resource anExec, NameMap nm ) {
		Resource exec = anExec.inModel(meta), page = thisPage.inModel(meta);
		exec.addProperty( RDF.type, API.Execution );
		addVariableBindings( meta, exec );
		addTermBindings( toScan, meta, exec, nm );
		page.addProperty( API.wasResultOf, exec );
	}

	// don't add variables that are not bound!
	public void addVariableBindings( Model meta, Resource exec ) {
		for (Iterator<String> names = cc.keySet().iterator(); names.hasNext();) {
			String name = names.next();
			String valueString = cc.getValueString( name );
			if (valueString != null) {
				Resource vb = meta.createResource();
				vb.addProperty( API.label, name );
				vb.addProperty( API.value, valueString );
				exec.addProperty( API.variableBinding, vb );
			}
		}
	}

	public void addTermBindings( Model toScan, Model meta, Resource exec, NameMap nm ) {
		Stage2NameMap s2 = nm.stage2(false).loadPredicates( toScan, toScan );
		Map<String, String> mm = s2.result();
		for (String uri: mm.keySet()) {
			Resource term = meta.createResource( uri );
			if (toScan.containsResource( term )) {
				String shorty = mm.get( uri );
	    		Resource tb = meta.createResource();
	    		exec.addProperty( API.termBinding, tb );
				tb.addProperty( API.label, shorty );
				tb.addProperty( API.property, term );
			}
		}
	}

	// following the Puelia model.
	public void addExecution( Model meta, Resource anExec ) {
		Resource exec = anExec.inModel(meta), page = thisPage.inModel(meta);
		exec.addProperty( RDF.type, API.Execution );
		Resource P = meta.createResource();
		ELDA.addEldaMetadata( P );
		exec.addProperty( API.processor, P );
		page.addProperty( API.wasResultOf, exec );
	}

	public void addQueryMetadata( Model meta, Resource anExec, APIQuery q, String detailsQuery, APISpec apiSpec, boolean listEndpoint ) {
		Resource EP = meta.createResource( SPARQL.Service );
	//
		apiSpec.getDataSource().addMetadata( EP ); 
		Resource url = EP.getProperty( API.sparqlEndpoint ).getResource(); 
		EP.addProperty( SPARQL.url, url );
	//
		Resource exec = anExec.inModel(meta);
		if (listEndpoint) {
	    	Resource sr = meta.createResource( SPARQL.QueryResult );    	
	    	sr.addProperty( SPARQL.query, inValue( meta, q.getQueryString( apiSpec, cc ) ) );
	    	sr.addProperty( SPARQL.endpoint, EP );
	    	exec.addProperty( API.selectionResult, sr );
		}
	//
		Resource vr = meta.createResource( SPARQL.QueryResult );
		vr.addProperty( SPARQL.query, inValue( meta, detailsQuery ) ); 
		vr.addProperty( SPARQL.endpoint, EP );
		exec.addProperty( API.viewingResult, vr );
	}

	public static Resource inValue( Model rsm, String s ) {
		return rsm.createResource().addProperty( RDF.value, s );
	}
}