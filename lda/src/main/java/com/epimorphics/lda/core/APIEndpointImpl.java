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
			Couple<View, String> result = cq.updateQueryAndConstructView( query.deferredFilters);
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
    
    private Resource createDefinitionURI( Model rsm, URI ru, Resource uriForSpec, String template ) {
    	String quasiTemplate = template.replaceAll( "\\{", "(" ).replaceAll( "\\}", ")");
    	if (template.startsWith("http:")) {
    		// Avoid special case from the TestAPI uriTemplates, qv.
    		return rsm.createResource( quasiTemplate + "/meta" );
    	}
		String argPattern = "\\{[-._A-Za-z]+\\}";
		String pattern = quasiTemplate.replaceAll( argPattern, "[^/]*" );
		String replaced = ru.toString().replaceAll( pattern, "/meta" + quasiTemplate );
		return rsm.createResource( replaced );
	}

    private URI withoutPageParameters( URI ru ) {
    	URI rqp1 = URIUtils.replaceQueryParam( ru, QueryParameter._PAGE );
    	return URIUtils.replaceQueryParam( rqp1, QueryParameter._PAGE_SIZE );
    }
    
	private void insertResultSetRoot( APIResultSet rs, URI ru, String format, Bindings cc, APIQuery query ) {
    	Model rsm = rs.getModel();
        int page = query.getPageNumber();
        int perPage = query.getPageSize();
        Resource uriForSpec = rsm.createResource( spec.getSpecificationURI() ); 
        String template = spec.getURITemplate();
        Resource uriForDefinition = createDefinitionURI( rsm, ru, uriForSpec, template ); 
        Set<String> formatNames = spec.getRendererFactoryTable().formatNames();
        URI pageBase = URIUtils.changeFormatSuffix(ru, formatNames, format);
        Resource thisPage = adjustPageParameter( rsm, pageBase, page );
        rs.setRoot(thisPage);
    //
		EndpointMetadata em = new EndpointMetadata( thisPage, isListEndpoint(), "" + page, cc, pageBase, formatNames );
		createOptionalMetadata(rs, query, em);   
	//
	// important this comes AFTER metadata creation.
	//
		thisPage.addProperty( API.definition, uriForDefinition );
    //
        URI emv_uri = URIUtils.replaceQueryParam( URIUtils.newURI(thisPage.getURI()), "_metadata", "all" );
        thisPage.addProperty( API.extendedMetadataVersion, rsm.createResource( emv_uri.toString() ) );
    //
        URI unPagedURI = withoutPageParameters( pageBase );
        if (isListEndpoint()) {
        	RDFList content = rsm.createList( rs.getResultList().iterator() );
        	thisPage
	        	.addProperty( RDF.type, API.Page )
	        	.addLiteral( API.page, page )
	        	.addLiteral( OpenSearch.itemsPerPage, perPage )
	        	.addLiteral( OpenSearch.startIndex, perPage * page + 1 )
	        	;
        	thisPage.addProperty( API.items, content );
    		thisPage.addProperty( XHV.first, adjustPageParameter( rsm, pageBase, 0 ) );
    		if (!rs.isCompleted) thisPage.addProperty( XHV.next, adjustPageParameter( rsm, pageBase, page+1 ) );
    		if (page > 0) thisPage.addProperty( XHV.prev, adjustPageParameter( rsm, pageBase, page-1 ) );
			Resource listRoot = rsm.createResource( unPagedURI.toString() );
    		thisPage
	    		.addProperty( DCTerms.isPartOf, listRoot )
	    		;
    		listRoot
	    		.addProperty( DCTerms.hasPart, thisPage )
	    		.addProperty( API.definition, uriForDefinition ) 
	    		.addProperty( RDF.type, API.ListEndpoint )
	    		;
    		rs.setContentLocation( pageBase );
        } else if (rs.isEmpty()) {
        	EldaException.NoItemFound();
        } else {
        	Resource content = rs.getResultList().get(0);
        	thisPage.addProperty( FOAF.primaryTopic, content );
        	content.addProperty( FOAF.isPrimaryTopicOf, thisPage );
        	rs.setContentLocation( unPagedURI );
        }
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
		Model rsm = rs.getModel();
		Resource exec = rsm.createResource();
		Model versions = ModelFactory.createDefaultModel();
		Model formats = ModelFactory.createDefaultModel();
		Model bindings = ModelFactory.createDefaultModel();
		Model execution = ModelFactory.createDefaultModel();
	//	
		em.addVersions( versions, spec.getExplicitViewNames() );
		em.addFormats( formats, spec.getRendererFactoryTable() );
		em.addBindings( rsm, bindings, exec, spec.getAPISpec().getShortnameService().nameMap() );
		em.addExecution( execution, exec );
		em.addQueryMetadata( execution, exec, query, rs.getDetailsQuery(), spec.getAPISpec(), isListEndpoint() );
	//
        if (query.wantsMetadata( "versions" )) rsm.add( versions ); else rs.setMetadata( "versions", versions );
        if (query.wantsMetadata( "formats" )) rsm.add( formats );  else rs.setMetadata( "formats", formats );
        if (query.wantsMetadata( "bindings" )) rsm.add( bindings ); else rs.setMetadata( "bindings", bindings );
        if (query.wantsMetadata( "execution" )) rsm.add( execution ); else rs.setMetadata( "execution", execution );
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

	private Renderer configure(RendererFactory s) {
		return s == null ? null : s.buildWith( this, getSpec().getAPISpec().getShortnameService() );
	}
}

