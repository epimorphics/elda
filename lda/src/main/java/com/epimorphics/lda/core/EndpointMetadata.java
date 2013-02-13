/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
*/
package com.epimorphics.lda.core;

import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.APIResultSet.MergedModels;
import com.epimorphics.lda.query.QueryParameter;
import com.epimorphics.lda.query.WantsMetadata;
import com.epimorphics.lda.renderers.Factories.FormatNameAndType;
import com.epimorphics.lda.shortnames.NameMap;
import com.epimorphics.lda.shortnames.NameMap.Stage2NameMap;
import com.epimorphics.lda.sources.Source;
import com.epimorphics.lda.specs.EndpointDetails;
import com.epimorphics.lda.vocabularies.ELDA;
import com.epimorphics.lda.vocabularies.OpenSearch;
import com.epimorphics.lda.vocabularies.SPARQL;
import com.epimorphics.lda.vocabularies.XHV;
import com.epimorphics.util.URIUtils;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
    Class to handle the construction of metadata for API endpoint results.
    Bit of a hotchpotch at the moment.
    
    @author Chris    
*/
public class EndpointMetadata {

	public static void addAllMetadata
		( URI ru
		, Resource uriForDefinition
		, Bindings bindings
		, NameMap nameMap
		, boolean suppress_IPTO
		, MergedModels mergedModels
		, Resource thisMetaPage
		, int page
		, int perPage
		, boolean hasMorePages
		, List<Resource> resultList
		, SetsMetadata setsMeta
		, WantsMetadata wantsMeta
		, String selectQuery
		, String viewQuery
		, Source source
		, Set<String> viewNames
		, Set<FormatNameAndType> formats
		, EndpointDetails details
		) {
	//
		boolean listEndpoint = details.isListEndpoint();
        URI uriForList = URIUtils.withoutPageParameters( ru );
		Model metaModel = mergedModels.meta;
		thisMetaPage.addProperty( API.definition, uriForDefinition );
	//
	    URI emv_uri = URIUtils.replaceQueryParam( URIUtils.newURI(thisMetaPage.getURI()), "_metadata", "all" );
	    thisMetaPage.addProperty( API.extendedMetadataVersion, metaModel.createResource( emv_uri.toString() ) );
	//
	    thisMetaPage.addProperty( RDF.type, API.Page );
	//
		if (listEndpoint) {
	    	
	    	RDFList content = metaModel.createList( resultList.iterator() );
	    	
	    	thisMetaPage
	        	.addLiteral( API.page, page )
	        	.addLiteral( OpenSearch.itemsPerPage, perPage )
	        	.addLiteral( OpenSearch.startIndex, perPage * page + 1 )
	        	;
	    	
	    	thisMetaPage.addProperty( API.items, content );
	    	
	    	Resource firstPage = URIUtils.adjustPageParameter( metaModel, ru, listEndpoint, 0 );
	    	Resource nextPage = URIUtils.adjustPageParameter( metaModel, ru, listEndpoint, page + 1 );
	    	Resource prevPage = URIUtils.adjustPageParameter( metaModel, ru, listEndpoint, page - 1 );

	    	thisMetaPage.addProperty( XHV.first, firstPage );
			if (hasMorePages) thisMetaPage.addProperty( XHV.next, nextPage );
			if (page > 0) thisMetaPage.addProperty( XHV.prev, prevPage );
			
			Resource listRoot = metaModel.createResource( uriForList.toString() );
			thisMetaPage
	    		.addProperty( DCTerms.hasPart, listRoot )
	    		;
			listRoot
	    		.addProperty( DCTerms.isPartOf, thisMetaPage )
	    		.addProperty( API.definition, uriForDefinition ) 
	    		.addProperty( RDF.type, API.ListEndpoint )
	    		;
	    } else {
			Resource content = resultList.get(0).inModel(metaModel);
			thisMetaPage.addProperty( FOAF.primaryTopic, content );
			if (suppress_IPTO == false) content.addProperty( FOAF.isPrimaryTopicOf, thisMetaPage );
		}
	//
		EndpointMetadata em = new EndpointMetadata( details, thisMetaPage, "" + page, bindings, uriForList );
		createOptionalMetadata
			( nameMap
			, details.isListEndpoint()
			, viewNames
			, formats
			, mergedModels
			, wantsMeta
			, setsMeta
			, selectQuery
			, viewQuery
			, source
			, em
			);
	}

	protected final Bindings bindings;
	protected final Resource thisPage;
	protected final URI thisPageAsURI;
	
	protected final String pageNumber;
	protected final boolean isListEndpoint;
	protected final URI pageURI;
	protected final boolean isParameterBasedFormat;
	
	public EndpointMetadata( EndpointDetails ep, Resource thisPage, String pageNumber, Bindings bindings, URI pageURI ) {
		this.bindings = bindings;
		this.pageURI = pageURI;
		this.thisPage = thisPage;
		this.pageNumber = pageNumber;
		this.isListEndpoint = ep.isListEndpoint();
		this.isParameterBasedFormat = ep.hasParameterBasedContentNegotiation();
    	this.thisPageAsURI = URIUtils.newURI( thisPage.getURI() );
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
	    		page.addProperty( DCTerms.hasVersion, v );
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
    	URI a = URIUtils.replaceQueryParam( thisPageAsURI, QueryParameter._VIEW, name );
    	URI b = isListEndpoint ? URIUtils.replaceQueryParam( a, QueryParameter._PAGE, pageNumber ) : a;
		return m.createResource( b.toString() );
    }
    
	private Resource resourceForFormat( URI reqURI, Model m, Set<String> knownFormats, String formatName ) {
		if (isParameterBasedFormat) {
			URI u = URIUtils.replaceQueryParam(reqURI, QueryParameter._FORMAT, formatName);
			return m.createResource( u.toString() );
		} else {
			URI u = URIUtils.changeFormatSuffix(reqURI, knownFormats, formatName);
			return m.createResource( u.toString() );
		}
	}

	/**
	    Create metadata which describes the available alternative formats
	    this page could be presented in.
	*/
	public void addFormats( Model meta, Set<FormatNameAndType> formats ) {
		Set<String> formatNames = getFormatNames( formats );
		Resource page = thisPage.inModel(meta);
		for (FormatNameAndType format: formats) {
			Resource v = resourceForFormat( pageURI, meta, formatNames, format.name );
			Resource formatNode = meta.createResource().addProperty( RDFS.label, format.mediaType );
			page.addProperty( DCTerms.hasFormat, v );
			v.addProperty( DCTerms.isFormatOf, thisPage );
			v.addProperty( DCTerms.format, formatNode );
			v.addProperty( RDFS.label, format.name );
		}
	}

	private Set<String> getFormatNames(Set<FormatNameAndType> formats) {
		Set<String> result = new HashSet<String>();
		for (FormatNameAndType nt: formats) result.add( nt.name );
		return result;
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
		for (Iterator<String> names = bindings.keySet().iterator(); names.hasNext();) {
			String name = names.next();
			String valueString = bindings.getValueString( name );
			if (valueString != null) {
				Resource vb = meta.createResource();
				vb.addProperty( API.label, name );
				vb.addProperty( API.value, valueString );
				exec.addProperty( API.variableBinding, vb );
			}
		}
	}

	static final Property SKOSprefLabel = ResourceFactory.createProperty
		( "http://www.w3.org/2004/02/skos/core#" + "prefLabel" )
		;
	
	public void addTermBindings( Model toScan, Model meta, Resource exec, NameMap nm ) {
		Stage2NameMap s2 = nm.stage2().loadPredicates( toScan, toScan );
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

	public void addQueryMetadata( Model meta, Resource anExec, String selectQuery, String viewQuery, Source source, boolean listEndpoint ) {
		Resource EP = meta.createResource( SPARQL.Service );
	//
		source.addMetadata( EP ); 
		Resource url = EP.getProperty( API.sparqlEndpoint ).getResource(); 
		EP.addProperty( SPARQL.url, url );
	//
		Resource exec = anExec.inModel(meta);
		if (listEndpoint) {
	    	Resource sr = meta.createResource( SPARQL.QueryResult );    	
			sr.addProperty( SPARQL.query, inValue( meta, selectQuery ) );
	    	sr.addProperty( SPARQL.endpoint, EP );
	    	exec.addProperty( API.selectionResult, sr );
		}
	//
		Resource vr = meta.createResource( SPARQL.QueryResult );
		vr.addProperty( SPARQL.query, inValue( meta, viewQuery ) ); 
		vr.addProperty( SPARQL.endpoint, EP );
		exec.addProperty( API.viewingResult, vr );
	}

	/**
	    <p>
	    	Create the optional endpoint metadata for this endpoint and query.
	    	The metadata is in four parts: the other versions (aka views) of
	    	this page, the other formats (aka renderers) of this page, the
	    	bindings (values of variables, full URIs of shortnames) for this
	    	page, and the execution description (which processor etc) for the
	    	process that built this page.
	    </p>
	    <p>
	    	Metadata that has been requested by the _metadata= query argument 
	    	is copied into the result-set model. Unrequested metadata is stored
	    	in the result-sets named metadata models in case it is requested by
	    	a renderer (ie, the xslt renderer in the education example).
	    </p>
	*/
	static void createOptionalMetadata
		( NameMap nameMap
		, boolean isListEndpoint
		, Set<String> viewNames
		, Set<FormatNameAndType> formats
		, MergedModels mm
		, WantsMetadata wantsMeta
		, SetsMetadata setsMeta
		, String selectQuery
		, String viewQuery
		, Source source
		, EndpointMetadata em 
		) {
		Model metaModel = mm.getMetaModel();
		Model mergedModels = mm.getMergedModel();
	//
		Resource exec = metaModel.createResource();
		Model versionsModel = ModelFactory.createDefaultModel();
		Model formatsModel = ModelFactory.createDefaultModel();
		Model bindingsModel = ModelFactory.createDefaultModel();
		Model execution = ModelFactory.createDefaultModel();
	//	
		em.addVersions( versionsModel, viewNames );
		em.addFormats( formatsModel, formats );
		em.addBindings( mergedModels, bindingsModel, exec, nameMap );
		em.addExecution( execution, exec );
	//
		em.addQueryMetadata( execution, exec, selectQuery, viewQuery, source, isListEndpoint );
	//
	    if (wantsMeta.wantsMetadata( "versions" )) metaModel.add( versionsModel ); else setsMeta.setMetadata( "versions", versionsModel );
	    if (wantsMeta.wantsMetadata( "formats" )) metaModel.add( formatsModel );  else setsMeta.setMetadata( "formats", formatsModel );
	    if (wantsMeta.wantsMetadata( "bindings" )) metaModel.add( bindingsModel ); else setsMeta.setMetadata( "bindings", bindingsModel );
	    if (wantsMeta.wantsMetadata( "execution" )) metaModel.add( execution ); else setsMeta.setMetadata( "execution", execution );
	}

	public static Resource inValue( Model rsm, String s ) {
		return rsm.createResource().addProperty( RDF.value, s );
	}
}