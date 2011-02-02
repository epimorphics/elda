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


import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.cache.Cache;
import com.epimorphics.lda.cache.Cache.Registry;
import com.epimorphics.lda.renderers.*;
import com.epimorphics.lda.restlets.support.SDX_URI_ConnegFilter;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.vocabularies.EXTRAS;
import com.epimorphics.lda.vocabularies.OpenSearch;
import com.epimorphics.lda.vocabularies.XHV;
import com.epimorphics.vocabs.API;
import com.epimorphics.vocabs.FIXUP;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Implements a single endpoint for an API.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public class APIEndpointImpl implements APIEndpoint {

    protected final APIEndpointSpec spec;
    protected final boolean specWantsContext;
    protected final Cache cache;
    
    public static final Resource NO_PRIMARY_TOPIC = ResourceFactory.createResource
    	( API.getURI() + "NO_PRIMARY_TOPIC_PROVIDED" );
    
    static Logger log = LoggerFactory.getLogger(APIEndpointImpl.class);
    
    public APIEndpointImpl( APIEndpointSpec spec ) {
    	this( spec, Registry.cacheFor( spec.getCachePolicyName(), spec.getAPISpec().getDataSource() ) );
    }
    
    public APIEndpointImpl( APIEndpointSpec spec, Cache cache ) {
        this.spec = spec;
        this.cache = cache;
        this.specWantsContext = spec.wantsContext();
    }
    
    @Override public APIResultSet call( CallContext given ) {
    	wantsContext = specWantsContext;
    	CallContext context = new CallContext( spec.getParameterBindings(), given );
        log.debug("API " + spec + " called on " + context + " from " + context.getUriInfo());
        APIQuery query = spec.getBaseQuery();
        View view = buildQueryAndView(context, query);
        APIResultSet unfiltered = query.runQuery( spec.getAPISpec(), cache, context, view );
        APIResultSet filtered = filterByView(view, unfiltered);
        filtered.setNsPrefixes( spec.getAPISpec().getPrefixMap() );
        insertResultSetRoot(filtered, context, query);
        return filtered;
    }
    
    protected boolean wantsContext = false;
    
    public boolean wantContext() {
    	return wantsContext;
    }

    // Filter by any views
    // In simple cases can combine view props into select then
    // run a construct on the results of the select and do less server traffic.
    // HOWEVER, there are two problems with that:
    // (1) where one (or more) props in view are multi valued then limit/offset 
    //     doesn't work and the paging code gets complicated
    // (2) where return value is a bNode and we want automatic bNode closure.
    // Current solution get full description from endpoint and post filter
	
    private APIResultSet filterByView(View view, APIResultSet rs) {
		if (view == null) {
			log.warn( "somehow, filterByTemplate got a null view." );
			return rs;			
		}
		else {			
			log.debug("Applying view: " + view.toString());
			return rs.getFilteredSet( view );
		}
	}

    private View buildQueryAndView( CallContext context, APIQuery query ) {
    	ShortnameService sns = spec.getAPISpec().getShortnameService();
    	ContextQueryUpdater cq = new ContextQueryUpdater( context, spec, sns, query );
		try { return cq.updateQueryAndConstructView(); }
		catch (APIException e) { throw new QueryParseException( "query construction failed", e ); }
    }
    
    /**
     * Return a metadata description for the query that would be run by this endpoint
     */
    public Resource getMetadata(CallContext context, Model metadata) {
        APIQuery query = spec.getBaseQuery();
        buildQueryAndView(context, query);
        metadata.setNsPrefix("api", API.getURI());
        Resource meta = resourceForMetaList(metadata, context);
        APISpec aSpec = spec.getAPISpec();
        meta.addProperty(EXTRAS.sparqlQuery, query.getQueryString(aSpec, context));
        aSpec.getDataSource().addMetadata(meta);
        meta.addProperty(EXTRAS.listURL, resourceForList(metadata, context));
        meta.addProperty(RDFS.comment, "Metadata describing the query and source for endpoint " + spec.getURITemplate());
        return meta;
    }
    
    private boolean isListEndpoint() {
    	return spec.isListEndpoint();
    }

    private Resource resourceForView( Model m, CallContext context, String name ) {
        UriBuilder ub = context.getURIBuilder();
        String uri = ub
        	.replaceQueryParam( APIQuery.SHOW_PARAM, name )
            .build()
            .toASCIIString();
        return m.createResource( uri );
    }

	private void addFormats(Model m, CallContext c, Resource thisPage) {
		for (Map.Entry<String, MediaType> e: SDX_URI_ConnegFilter.createMediaExtensions().entrySet()) {
			Resource v = resourceForFormat( m, c, e.getKey() );
			Resource format = m.createResource().addProperty( RDFS.label, e.getValue().toString() );
			thisPage.addProperty( DCTerms.hasFormat, v );
			v.addProperty( DCTerms.isFormatOf, thisPage );
			v.addProperty( DCTerms.format, format );
		}
	}

	private Resource resourceForFormat( Model m, CallContext c, String key ) {
		String oldPath = c.getUriInfo().getBaseUri().getPath() + c.getUriInfo().getPath();
        UriBuilder ub = c.getURIBuilder();
        String uri = ub
        	.replacePath( oldPath + "." + key )
            .build()
            .toASCIIString();
        return m.createResource( uri );
    }

	private void addVersions( Model m, CallContext c, Resource thisPage ) {
		for (Map.Entry<String, View> e: spec.views.entrySet()) {
    		Resource v = resourceForView( m, c, e.getKey() );
			thisPage.addProperty( DCTerms.hasVersion, v	);
			v.addProperty( DCTerms.isVersionOf, thisPage );
			v.addProperty( RDFS.label, e.getKey() );
    	}
	}
    
    private void insertResultSetRoot(APIResultSet rs, CallContext context, APIQuery query) {
        int page = query.getPageNumber();
        int perPage = query.getPageSize();
        Resource uriForSpec = rs.createResource( spec.getAPISpec().specificationURI ); 
        Resource thisPage = resourceForPage(rs, context, page);
        rs.setRoot(thisPage);
    //
		thisPage.addProperty( FIXUP.definition, uriForSpec );
        addVersions( rs, context, thisPage );
		addFormats( rs, context, thisPage );
    //
        if (isListEndpoint()) {
        	RDFList content = rs.createList( rs.getResultList().iterator() );
        	thisPage
	        	.addProperty( RDF.type, FIXUP.Page )
	        	.addLiteral( FIXUP.page, page )
	        	.addLiteral( OpenSearch.itemsPerPage, perPage )
	        	.addLiteral( OpenSearch.startIndex, perPage * page + 1 )
	        	;
        	thisPage.addProperty( FIXUP.items, content );
    		thisPage.addProperty( XHV.first, resourceForPage( rs, context, 0 ) );
    		if (!rs.isCompleted) thisPage.addProperty( XHV.next, resourceForPage( rs, context, page+1 ) );
    		if (page > 0) thisPage.addProperty( XHV.prev, resourceForPage( rs, context, page-1 ) );
    		Resource listRoot = resourceForList(rs, context);
    		thisPage
	    		.addProperty( DCTerms.isPartOf, listRoot )
	    		;
    		listRoot
	    		.addProperty( DCTerms.hasPart, thisPage )
	    		.addProperty( FIXUP.definition, uriForSpec ) 
	    		.addProperty( RDF.type, API.ListEndpoint )
	    		.addProperty( RDFS.label, "should be a description of this list" )
	    		;
    		rs.setContentLocation( listRoot.getURI() );
        } else {
        	Resource content = rs.getResultList().get(0);
        	thisPage.addProperty( FOAF.primaryTopic, content );
        	content.addProperty( FOAF.isPrimaryTopicOf, thisPage );     
        	rs.setContentLocation( query.getSubject() );
        }
    }
    
    private Resource resourceForPage(Model m, CallContext context, int page) {
        UriBuilder ub = context.getURIBuilder();
        String uri = ub
            .replaceQueryParam(APIQuery.PAGE_PARAM, Integer.toString(page))
            .replacePath( ub.build().getPath() + context.getMediaSuffix() )
            .build()
            .toASCIIString();
        return m.createResource( uri );
    }
    
    private Resource resourceForList(Model m, CallContext context) {
        UriBuilder ub = context.getURIBuilder();
        String uri = ub
            .replaceQueryParam( APIQuery.PAGE_PARAM )
            .replaceQueryParam( APIQuery.PAGE_SIZE_PARAM )
            .replacePath( ub.build().getPath() + context.getMediaSuffix() )
            .build().toASCIIString();
        uri = uri.replaceFirst("/meta/", "/api/");
        return m.createResource( uri );
    }

    private Resource resourceForMetaList(Model m, CallContext context) {
        UriBuilder ub = context.getURIBuilder();
        String uri = ub
            .replaceQueryParam(APIQuery.PAGE_PARAM)
            .replaceQueryParam(APIQuery.PAGE_SIZE_PARAM)
            .replacePath( ub.build().getPath() + context.getMediaSuffix() )
            .build().toASCIIString();
        uri = uri.replaceFirst("/api/", "/meta/");
        return m.createResource( uri );
    }

    /**
     * The URI template at which this APIEndpoint should be attached
     */
    @Override
    public String getURITemplate() {
        return spec.getURITemplate();
    }
    
    
    /**
     * Return the specification for this endpoint
     */
    public APIEndpointSpec getSpec() {
        return spec;
    }
    
    /**
     * Return a render appropriate for the given mimetype
     */
    public Renderer getRendererFor( String mimetype ) {
    	ShortnameService sns = getSpec().getAPISpec().getShortnameService();
        if (mimetype.equals( "text/plain" )) return new JSONRenderer(this, "text/plain");
        if (mimetype.equals( "text/turtle" )) return new TurtleRenderer();
        if (mimetype.equals( "application/rdf+xml" )) return new RDFXMLRenderer();
        if (mimetype.equals( JSONRenderer.JSON_MIME )) return new JSONRenderer( this );
        if (mimetype.equals( XMLRenderer.XML_MIME )) return new XMLRenderer( sns, XMLRenderer.As.XML );
        if (mimetype.equals( "text/html" )) return new XMLRenderer( sns, XMLRenderer.As.HTML );
        if (mimetype.equals( "text/html" )) return new HTMLRenderer();
        return null;
    }

	public String getSelectQuery() {
		PrefixMapping noPrefixes = PrefixMapping.Factory.create();
		return spec.getBaseQuery().assembleSelectQuery( noPrefixes );
	}
}

