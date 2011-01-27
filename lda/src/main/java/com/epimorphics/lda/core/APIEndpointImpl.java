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


import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.cache.Cache;
import com.epimorphics.lda.cache.Cache.Registry;
import com.epimorphics.lda.renderers.*;
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
    
    private void insertResultSetRoot(APIResultSet rs, CallContext context, APIQuery query) {
        int page = query.getPageNumber();
        int perPage = query.getPageSize();
        Resource uriForSpec = rs.createResource( spec.getAPISpec().specificationURI ); 
        
        if (query.isFixedSubject()) {
//        	System.err.println( ">> query isFixedSubject " + query.getSubject() );
            rs.setContentLocation( query.getSubject() );
        } else {   
            Resource thisPage = resourceForPage(rs, context, page);
            rs.setRoot(thisPage);
        //
            RDFList content = rs.createList( rs.getResultList().iterator() );
            thisPage
                .addProperty( RDF.type, FIXUP.Page )
                .addProperty( FIXUP.items, content )
                .addLiteral( FIXUP.page, page )
                .addLiteral( OpenSearch.itemsPerPage, perPage )
                .addLiteral( OpenSearch.startIndex, perPage * page + 1 )
                ;            
            Resource listRoot = resourceForList(rs, context);
            listRoot
            	.addProperty( DCTerms.hasPart, thisPage )
            	.addProperty( FIXUP.definition, uriForSpec ) 
            	.addProperty( RDF.type, API.ListEndpoint )
            	.addProperty( RDFS.label, "should be a description of this list" )
            	;
            thisPage
            	.addProperty( DCTerms.isPartOf, listRoot )
            	.addProperty( FIXUP.definition, uriForSpec )
            	;
            String topic = spec.getAPISpec().getPrimaryTopic();
            if (topic != null) {
                listRoot.addProperty(FOAF.primaryTopic, rs.createResource(topic));
            }
    
            thisPage.addProperty(XHV.first, resourceForPage(rs, context, 0));
            
            if (!rs.isCompleted) {
                thisPage.addProperty(XHV.next, resourceForPage(rs, context, page+1) );
            }
            
            if (page > 0) {
                thisPage.addProperty(XHV.prev, resourceForPage(rs, context, page-1));
            }
            rs.setContentLocation( listRoot.getURI() );
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
        if (mimetype.equals( "text/plain" )) return new JSONRenderer(this, "text/plain");
        if (mimetype.equals( "text/turtle" )) return new TurtleRenderer();
        if (mimetype.equals( "application/rdf+xml" )) return new RDFXMLRenderer();
        if (mimetype.equals( JSONRenderer.JSON_MIME )) return new JSONRenderer( this );
        if (mimetype.equals( XMLRenderer.XML_MIME )) return new XMLRenderer();
        if (mimetype.equals( "text/html" )) return new HTMLRenderer();
        return null;
    }

	public String getSelectQuery() {
		PrefixMapping noPrefixes = PrefixMapping.Factory.create();
		return spec.getBaseQuery().assembleSelectQuery( noPrefixes );
	}
}

