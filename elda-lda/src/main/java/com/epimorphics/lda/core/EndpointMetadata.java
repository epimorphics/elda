/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
*/
package com.epimorphics.lda.core;

import java.net.URI;
import java.util.*;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.APIResultSet.MergedModels;
import com.epimorphics.lda.query.QueryParameter;
import com.epimorphics.lda.query.WantsMetadata;
import com.epimorphics.lda.renderers.Factories.FormatNameAndType;
import com.epimorphics.lda.shortnames.CompleteContext;
import com.epimorphics.lda.sources.Source;
import com.epimorphics.lda.specs.EndpointDetails;
import com.epimorphics.lda.support.PropertyChain;
import com.epimorphics.lda.vocabularies.*;
import com.epimorphics.util.URIUtils;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.*;

/**
    Class to handle the construction of metadata for API endpoint results.
    Bit of a hotchpotch at the moment.
    
    @author Chris    
*/
public class EndpointMetadata {

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
	
	public static void addAllMetadata
		( MergedModels mergedModels
		, URI ru
		, Resource uriForDefinition
		, Bindings bindings
		, CompleteContext cc
		, boolean suppress_IPTO
		, Resource thisMetaPage
		, int page
		, int perPage
		, Integer totalResults
		, boolean hasMorePages
		, List<Resource> resultList
		, SetsMetadata setsMeta
		, WantsMetadata wantsMeta
		, String selectQuery
		, String viewQuery
		, Source source
		, Map<String, View> views
		, Set<FormatNameAndType> formats
		, EndpointDetails details
		) {
	//
		boolean listEndpoint = details.isListEndpoint();
        URI uriForList = URIUtils.withoutPageParameters( ru );
		Model metaModel = mergedModels.getMetaModel();
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

	    	if (totalResults != null)     		
	    		thisMetaPage.addLiteral( OpenSearch.totalResults, totalResults.intValue() );
	    	
	    	thisMetaPage.addProperty( API.items, content );
	    	Resource firstPage = URIUtils.adjustPageParameter( metaModel, ru, listEndpoint, 0 );
	    	Resource nextPage = URIUtils.adjustPageParameter( metaModel, ru, listEndpoint, page + 1 );
	    	Resource prevPage = URIUtils.adjustPageParameter( metaModel, ru, listEndpoint, page - 1 );

	    	thisMetaPage.addProperty( XHV.first, firstPage );
			if (hasMorePages) thisMetaPage.addProperty( XHV.next, nextPage );
			if (page > 0) thisMetaPage.addProperty( XHV.prev, prevPage );
			
			Resource listRoot = metaModel.createResource( uriForList.toString() );
			thisMetaPage
	    		.addProperty( DCTerms.isPartOf, listRoot )
	    		;
			listRoot
	    		.addProperty( DCTerms.hasPart, thisMetaPage )
	    		.addProperty( API.definition, uriForDefinition ) 
	    		.addProperty( RDF.type, API.ListEndpoint )
	    		;
	    } else {
			Resource content = firstOf(resultList).inModel(metaModel);
			thisMetaPage.addProperty( FOAF.primaryTopic, content );
			if (suppress_IPTO == false) content.addProperty( FOAF.isPrimaryTopicOf, thisMetaPage );
		}
	//
		EndpointMetadata em = new EndpointMetadata( details, thisMetaPage, "" + page, bindings, uriForList );
		Model metaModel1 = mergedModels.getMetaModel();
		Model mergedModels1 = mergedModels.getMergedModel();
	//
		Resource exec = metaModel1.createResource();
		Model versionsModel = ModelFactory.createDefaultModel();
		Model formatsModel = ModelFactory.createDefaultModel();
		Model bindingsModel = ModelFactory.createDefaultModel();
		Model execution = ModelFactory.createDefaultModel();
	//	
		em.addVersions( versionsModel, cc, views );
		em.addFormats( formatsModel, formats );
		em.addExecution( execution, exec );
	//
		em.addQueryMetadata( execution, exec, selectQuery, viewQuery, source, details.isListEndpoint() );
	//
		cc.include( versionsModel );
		cc.include( formatsModel );
		cc.include( execution );
	//
		em.addBindings( mergedModels1, bindingsModel, exec, cc );
	//
	    if (wantsMeta.wantsMetadata( "versions" )) metaModel1.add( versionsModel ); else setsMeta.setMetadata( "versions", versionsModel );
	    if (wantsMeta.wantsMetadata( "formats" )) metaModel1.add( formatsModel );  else setsMeta.setMetadata( "formats", formatsModel );
	    if (wantsMeta.wantsMetadata( "bindings" )) metaModel1.add( bindingsModel ); else setsMeta.setMetadata( "bindings", bindingsModel );
	    if (wantsMeta.wantsMetadata( "execution" )) metaModel1.add( execution ); else setsMeta.setMetadata( "execution", execution );
	}
	
	private static Resource firstOf(List<Resource> resultList) {
		return resultList.isEmpty() 
			? ResourceFactory.createResource("elda:missingEndpoint") 
			: resultList.get(0)
			;
	}

	/**
	    Create metadata describing the alternative views available
	    for this endpoint, given their names.
	*/
	public void addVersions( Model m, CompleteContext cc, Map<String, View> views ) {
		Map<String, String> uriToShortname = cc.Do();
		Resource page = thisPage.inModel( m );
		for (Map.Entry<String, View> e: views.entrySet()) {
			String viewName = e.getKey();
			if (!viewName.equals( View.SHOW_DEFAULT_INTERNAL )) {
	    		Resource v = resourceForView( m, viewName );
	    		page.addProperty( DCTerms.hasVersion, v );
				v.addProperty( DCTerms.isVersionOf, page );
				v.addProperty( RDFS.label, viewName );
			//
				v.addProperty( EXTRAS.viewName, viewName );
				for (PropertyChain pc: e.getValue().chains ) {
					v.addProperty( API.properties, chainsFor( uriToShortname, pc ) );
				}
			}
    	}
	}

	private String chainsFor(Map<String, String> uriToShortname, PropertyChain pc) {
		StringBuilder sb = new StringBuilder();
		String dot = "";
		for (Property p: pc.getProperties()) {
			sb.append(dot); dot = ".";
			sb.append( uriToShortname.get(p.getURI()));
		}
		return sb.toString();
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
			Resource v = resourceForFormat( thisPageAsURI, meta, formatNames, format.name );
			Resource formatNode = createBNode( meta ).addProperty( RDFS.label, format.mediaType );
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

	public void addBindings( Model toScan, Model meta, Resource anExec, CompleteContext cc ) {
		Resource exec = anExec.inModel(meta), page = thisPage.inModel(meta);
		exec.addProperty( RDF.type, API.Execution );
		addVariableBindings( meta, exec );
		cc.include( meta );
		addTermBindings( toScan, meta, exec, cc );
		page.addProperty( API.wasResultOf, exec );
	}

	// don't add variables that are not bound!
	public void addVariableBindings( Model meta, Resource exec ) {
		List<String> names = new ArrayList<String>( bindings.keySet() );
		Collections.sort( names );
		for (String name: names) {
			String valueString = bindings.getValueString( name );
			if (valueString != null) {
				Resource vb = createBNode( meta );
				vb.addProperty( API.label, name );
				vb.addProperty( API.value, valueString );
				exec.addProperty( API.variableBinding, vb );
			}
		}
	}

	int bnodeCounter = 1000;
	
	private Resource createBNode(Model m) {
		Resource b = m.createResource( new AnonId( "bnode-" + bnodeCounter++ ) );
		return b;
	}
	
	public void addTermBindings( Model toScan, Model meta, Resource exec, CompleteContext cc ) {
		Map<String, String> termBindings = cc.Do();
		List<String> uriList = new ArrayList<String>( termBindings.keySet() );
		Collections.sort( uriList );
		for (String uri: uriList) {
			Resource term = meta.createResource( uri );
			if (toScan.containsResource( term )) {
				String shorty = termBindings.get( uri );
	    		Resource tb = createBNode( meta );
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
		Resource P = createBNode( meta );
		ELDA.addEldaMetadata( P );
		exec.addProperty( API.processor, P );
		page.addProperty( API.wasResultOf, exec );
	}

	public void addQueryMetadata( Model meta, Resource anExec, String selectQuery, String viewQuery, Source source, boolean listEndpoint ) {
		Resource EP = meta.createResource( SPARQL.Service );
	//
		source.addMetadata( EP ); 
		Resource url = EP.getRequiredProperty( API.sparqlEndpoint ).getResource(); 
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
		( boolean isListEndpoint
		, CompleteContext cc
		, Map<String, View> views
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
		em.addVersions( versionsModel, cc, views );
		em.addFormats( formatsModel, formats );
		em.addBindings( mergedModels, bindingsModel, exec, cc );
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