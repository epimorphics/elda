/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
    
    File:        APIEndpointImpl.java
    Created by:  Dave Reynolds
    Created on:  31 Jan 2010
*/

package com.epimorphics.lda.core;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.cache.Cache;
import com.epimorphics.lda.cache.Cache.Registry;
import com.epimorphics.lda.exceptions.APIException;
import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.exceptions.QueryParseException;
import com.epimorphics.lda.query.APIQuery;
import com.epimorphics.lda.query.ContextQueryUpdater;
import com.epimorphics.lda.query.QueryParameter;
import com.epimorphics.lda.renderers.*;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.support.Controls;
import com.epimorphics.lda.vocabularies.EXTRAS;
import com.epimorphics.lda.vocabularies.OpenSearch;
import com.epimorphics.lda.vocabularies.XHV;
import com.epimorphics.util.Couple;
import com.epimorphics.util.MediaType;
import com.epimorphics.util.RDFUtils;
import com.epimorphics.util.Triad;
import com.epimorphics.util.URIUtils;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Implements a single endpoint for an API.
 * 
 * @author <a href="mailto:der@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public class APIEndpointImpl implements APIEndpoint {

    protected final APIEndpointSpec spec;
    protected final boolean specWantsContext;
    protected final Cache cache;
    
    static Logger log = LoggerFactory.getLogger(APIEndpointImpl.class);
    
    public APIEndpointImpl( APIEndpointSpec spec ) {
    	this( spec, Registry.cacheFor( spec.getCachePolicyName(), spec.getAPISpec().getDataSource() ) );
    	// System.err.println( ">> cache is " + cache.summary() );
    }
    
    public APIEndpointImpl( APIEndpointSpec spec, Cache cache ) {
        this.spec = spec;
        this.cache = cache;
        // System.err.println( ">> endpoint with cache " + cache.summary() );
        this.specWantsContext = spec.wantsContext();
    }
    
    @Override public String toString() {
    	return spec.toString();
    }
	
	static final Bindings defaults = new Bindings().put( "_resourceRoot", "/elda/lda-assets/" );
	
    @Override public Bindings defaults() {
    	return defaults;
    }
    
    @Override public Triad<APIResultSet, String, Bindings> call( Controls c, URI reqURI, Bindings given ) {
    	Bindings cc = given.copyWithDefaults( spec.getBindings() );
        APIQuery query = spec.getBaseQuery();
        Couple<View, String> viewAndFormat = buildQueryAndView( cc, query );
        View view = viewAndFormat.a; 
    //
        String format = viewAndFormat.b;
        if (format == null || format.equals("")) format = given.getAsString( "_suffix", "" );
    //    
        APIResultSet unfiltered = query.runQuery( c, spec.getAPISpec(), cache, cc, view );
        APIResultSet filtered = unfiltered.getFilteredSet( view, query.getDefaultLanguage() );
        filtered.setNsPrefixes( spec.getAPISpec().getPrefixMap() );
        insertResultSetRoot(filtered, reqURI, format, cc, query);
        return new Triad<APIResultSet, String, Bindings>( filtered, format, cc );
    }

    private Couple<View, String> buildQueryAndView( Bindings context, APIQuery query ) {
    	ShortnameService sns = spec.getAPISpec().getShortnameService();
    	int endpointType = isListEndpoint() ? ContextQueryUpdater.ListEndpoint : ContextQueryUpdater.ItemEndpoint;
    	ContextQueryUpdater cq = new ContextQueryUpdater( endpointType, context, spec, sns, query );
		try { 
			Couple<View, String> result = cq.updateQueryAndConstructView( query.deferredFilters );
			String format = result.b.equals( "" ) ? context.getValueString( "_suffix") : result.b;
			if (context.getValueString( "callback" ) != null && !"json".equals( format ))
				EldaException.BadRequest( "callback specified but format '" + format + "' is not JSON." );
			return result; 
		}
		catch (APIException e) { throw new QueryParseException( "query construction failed", e ); }
    }
    
    /**
     * Return a metadata description for the query that would be run by this endpoint
     */
    @Override public Resource getMetadata(Bindings context, URI ru, Model metadata) {
        APIQuery query = spec.getBaseQuery();
        buildQueryAndView(context, query);
        metadata.setNsPrefix("api", API.getURI());
		Resource meta = metadata.createResource( withoutPageParameters( ru ).toString() );
        APISpec aSpec = spec.getAPISpec();
        meta.addProperty(EXTRAS.sparqlQuery, query.getQueryString(aSpec, context));
        aSpec.getDataSource().addMetadata(meta);
        meta.addProperty( EXTRAS.listURL, meta );
        meta.addProperty( RDFS.comment, "Metadata describing the query and source for endpoint " + spec.getURITemplate() );
        return meta;
    }
    
    private boolean isListEndpoint() {
    	return spec.isListEndpoint();
    }

	private Resource adjustPageParameter( Model m, URI ru, int page) {
		URI x = isListEndpoint()
			? URIUtils.replaceQueryParam( ru, QueryParameter._PAGE, Integer.toString(page) )
			: URIUtils.replaceQueryParam( ru, QueryParameter._PAGE );
		return m.createResource( x.toString() );
    }
    
    private String createDefinitionURI( URI ru, Resource uriForSpec, String template, String expanded ) {
    	String pseudoTemplate = template.replaceAll( "\\{([A-Za-z0-9]+)\\}", "_$1" );
       	if (pseudoTemplate.startsWith("http:")) {
    		// Avoid special case from the TestAPI uriTemplates, qv.
    		return pseudoTemplate + "/meta";
    	}
		String other = ru.toString().replace( expanded, "/meta" + pseudoTemplate );
//		System.err.println( ">> createDefinitionURI" );
//		System.err.println( ">> ru: " + ru );
//		System.err.println( ">> pseudoTemplate: " + template );
//		System.err.println( ">> template: " + pseudoTemplate );
//		System.err.println( ">> uriForSpec: " + uriForSpec );
//		System.err.println( ">> RESULT: " + replaced );
//		System.err.println( ">> OTHER:  " + other );
//		System.err.println( ">>" );
		return other;
	}

    private URI withoutPageParameters( URI ru ) {
    	URI rqp1 = URIUtils.replaceQueryParam( ru, QueryParameter._PAGE );
//    	System.err.println( ">> withoutPageParameters: rqp1 = " + rqp1 );
    	return URIUtils.replaceQueryParam( rqp1, QueryParameter._PAGE_SIZE );
    }
    
	private void insertResultSetRoot( APIResultSet rs, URI ru, String format, Bindings b, APIQuery query ) {
		
//		System.err.println( ">> insertResultSetRoot: ru = " + ru );
		
		boolean suppress_IPTO = b.getAsString( "_suppress_ipto", "no" ).equals( "yes" );
		boolean exceptionIfEmpty = b.getAsString( "_exceptionIfEmpty", "yes" ).equals( "yes" );
	//
		if (rs.isEmpty() && exceptionIfEmpty && !isListEndpoint()) EldaException.NoItemFound();
		
	//
		Model metaModel = rs.getModels().getMetaModel();
		Model objectModel = rs.getModels().getObjectModel();
	//
        int page = query.getPageNumber();
        int perPage = query.getPageSize();
    //
        Resource uriForSpec = metaModel.createResource( spec.getSpecificationURI() ); 
        String template = spec.getURITemplate();
        Set<String> formatNames = spec.getRendererFactoryTable().formatNames();
    //
        URI uriForList = withoutPageParameters( ru );
    //  
        Resource thisMetaPage = metaModel.createResource( ru.toString() ); 
        Resource thisObjectPage = thisMetaPage.inModel( objectModel );
    //
        rs.setContentLocation( URIUtils.changeFormatSuffix( ru, formatNames, format ) );
    //        
        Resource uriForDefinition = metaModel.createResource( createDefinitionURI( uriForList, uriForSpec, template, b.expandVariables( template ) ) ); 
    //
        rs.setRoot(thisMetaPage);
    //
		thisMetaPage.addProperty( API.definition, uriForDefinition );
    //
        URI emv_uri = URIUtils.replaceQueryParam( URIUtils.newURI(thisMetaPage.getURI()), "_metadata", "all" );
        thisMetaPage.addProperty( API.extendedMetadataVersion, metaModel.createResource( emv_uri.toString() ) );
    //
        thisMetaPage.addProperty( RDF.type, API.Page );
    //
        if (isListEndpoint()) {
        	
        	RDFList content = metaModel.createList( rs.getResultList().iterator() );
        	
        	thisMetaPage
	        	.addLiteral( API.page, page )
	        	.addLiteral( OpenSearch.itemsPerPage, perPage )
	        	.addLiteral( OpenSearch.startIndex, perPage * page + 1 )
	        	;
        	
        	thisMetaPage.addProperty( API.items, content );
        	
        	Resource firstPage = adjustPageParameter( metaModel, ru, 0 );
        	Resource nextPage = adjustPageParameter( metaModel, ru, page + 1 );
        	Resource prevPage = adjustPageParameter( metaModel, ru, page - 1 );

        	thisMetaPage.addProperty( XHV.first, firstPage );
    		if (!rs.isCompleted) thisMetaPage.addProperty( XHV.next, nextPage );
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
			Resource content = rs.getResultList().get(0).inModel(metaModel);
			thisMetaPage.addProperty( FOAF.primaryTopic, content );
			if (suppress_IPTO == false) content.addProperty( FOAF.isPrimaryTopicOf, thisMetaPage );
		}
        EndpointMetadata em = new EndpointMetadata( spec, thisMetaPage, "" + page, b, uriForList, formatNames );
        createOptionalMetadata(rs, query, em);   
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
	private void createOptionalMetadata( APIResultSet rs, APIQuery query, EndpointMetadata em ) {
		Model metaModel = rs.getModels().getMetaModel();
		Model mergedModels = rs.getModels().getMergedModel();
		Resource exec = metaModel.createResource();
		Model versions = ModelFactory.createDefaultModel();
		Model formats = ModelFactory.createDefaultModel();
		Model bindings = ModelFactory.createDefaultModel();
		Model execution = ModelFactory.createDefaultModel();
	//	
		em.addVersions( versions, spec.getExplicitViewNames() );
		em.addFormats( formats, spec.getRendererFactoryTable() );
		em.addBindings( mergedModels, bindings, exec, spec.getAPISpec().getShortnameService().nameMap() );
		em.addExecution( execution, exec );
		em.addQueryMetadata( execution, exec, query, rs.getDetailsQuery(), spec.getAPISpec(), isListEndpoint() );
	//
        if (query.wantsMetadata( "versions" )) metaModel.add( versions ); else rs.setMetadata( "versions", versions );
        if (query.wantsMetadata( "formats" )) metaModel.add( formats );  else rs.setMetadata( "formats", formats );
        if (query.wantsMetadata( "bindings" )) metaModel.add( bindings ); else rs.setMetadata( "bindings", bindings );
        if (query.wantsMetadata( "execution" )) metaModel.add( execution ); else rs.setMetadata( "execution", execution );
	}
    /**
     * The URI template at which this APIEndpoint should be attached
     */
    @Override public String getURITemplate() {
        return spec.getURITemplate();
    }
    
    /**
     * Return the specification for this endpoint
     */
    @Override public APIEndpointSpec getSpec() {
        return spec;
    }

    /**
        Answer the SELECT query that would be used in the current
        state of this endpoint to find the iterms of interest.
    */
	public String getSelectQuery() {
		return spec.getBaseQuery().assembleSelectQuery( RDFUtils.noPrefixes );
	}
	
	/**
	    Answer a Renderer of the format named <code>name</code>, configured
	    with the appropriate shortname service.
	*/
	@Override public Renderer getRendererNamed( String name ) {
		RendererFactory s = spec.getRendererFactoryTable().getFactoryByName( name );
		return configure( s );		
	}
	
	/**
	    Answer a Renderer appropriate to the given MediaType <code>mt</code>,
	    configured with the appropriate shortname service.
	*/
	@Override public Renderer getRendererByType( MediaType mt ) {
		RendererFactory s = spec.getRendererFactoryTable().getFactoryByType( mt );
		return configure( s );	
	}

	private Map<RendererFactory, Renderer> ready = new HashMap<RendererFactory, Renderer>();
	
	/**
	    Only build a renderer for a given factory once.
	*/
	private Renderer configure(RendererFactory s) {
		if (s == null) return null;
		Renderer r = ready.get( s );
		if (r == null) ready.put( s,  r = s.buildWith( this, getSpec().getAPISpec().getShortnameService() ) );
		return r;
	}
}

