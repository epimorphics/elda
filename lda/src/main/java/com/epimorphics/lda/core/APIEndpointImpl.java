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
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.lda.cache.Cache;
import com.epimorphics.lda.cache.Cache.Registry;
import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.renderers.*;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.vocabularies.EXTRAS;
import com.epimorphics.lda.vocabularies.OpenSearch;
import com.epimorphics.lda.vocabularies.XHV;
import com.epimorphics.util.Couple;
import com.epimorphics.util.MediaTypes;
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
    }
    
    public APIEndpointImpl( APIEndpointSpec spec, Cache cache ) {
        this.spec = spec;
        this.cache = cache;
        this.specWantsContext = spec.wantsContext();
    }
    
    @Override public String toString() {
    	return spec.toString();
    }
    
    @Override public Couple<APIResultSet, String> call( CallContext given ) {
    	wantsContext = specWantsContext;
    	CallContext context = new CallContext( spec.getBindings(), given );
        log.debug("API " + spec + " called on " + context + " from " + context.getRequestURI());
        APIQuery query = spec.getBaseQuery();
        Couple<View, String> viewAndFormat = buildQueryAndView( context, query );
        View view = viewAndFormat.a; String format = viewAndFormat.b;
        APIResultSet unfiltered = query.runQuery( spec.getAPISpec(), cache, context, view );
        APIResultSet filtered = filterByView( view, unfiltered );
        filtered.setNsPrefixes( spec.getAPISpec().getPrefixMap() );
        insertResultSetRoot(filtered, context, query);
        return new Couple<APIResultSet, String>( filtered, format );
    }

	protected boolean wantsContext = false;
    
    @Override public boolean wantContext() {
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
	
    private APIResultSet filterByView( View view, APIResultSet rs ) {
		if (view == null) {
			log.warn( "somehow, filterByTemplate got a null view." );
			return rs;			
		}
		else {			
			log.debug("Applying view: " + view.toString());
			return rs.getFilteredSet( view, spec.getDefaultLanguage() );
		}
	}

    private Couple<View, String> buildQueryAndView( CallContext context, APIQuery query ) {
    	ShortnameService sns = spec.getAPISpec().getShortnameService();
    	ContextQueryUpdater cq = new ContextQueryUpdater( context, spec, sns, query );
		try { return cq.updateQueryAndConstructView(); }
		catch (APIException e) { throw new QueryParseException( "query construction failed", e ); }
    }
    
    /**
     * Return a metadata description for the query that would be run by this endpoint
     */
    @Override public Resource getMetadata(CallContext context, Model metadata) {
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
    	URI req = context.getRequestURI();
    	String alt = replaceQueryParam( req, QueryParameter._VIEW, name );
//        UriBuilder ub = context.getURIBuilder();
//        String uri = ub
//        	.replaceQueryParam( QueryParameter._VIEW, name )
//            .build()
//            .toASCIIString();
//        if (!uri.equals(alt)) {
//        	System.err.println( ">> exp:   " + uri );
//        	System.err.println( ">> got:   " + alt );
//        }
        return m.createResource( alt );
    }

	private String replaceQueryParam(URI ru, String key, String value) {
		try {
			String q = ru.getQuery();
			// System.err.println( ">> query: " + q );
			String qa = q == null ? "" : strip( q, key );
			// System.err.println( ">> qa:    " + qa );
			String qb = qa.isEmpty() ? "" : qa + "&";
			String newq = qb + key + "=" + value;
			// System.err.println( ">> res:   " + newq );
			return new URI
				(
				ru.getScheme(), 
				ru.getAuthority(), 
				ru.getPath(),
				newq, 
				ru.getFragment() 
				).toASCIIString();
		} catch (URISyntaxException e) {			
			throw new EldaException( "created a broken URI", "", EldaException.SERVER_ERROR, e );
		}
	}

	private String strip(String query, String key) {
		return query.replaceAll( "(^|[&])" + key + "=[^&]*", "" );
	}

	private void addFormats(Model m, CallContext c, Resource thisPage) {
		for (Map.Entry<String, MediaType> e: MediaTypes.createMediaExtensions().entrySet()) {
			Resource v = resourceForFormat( m, c, e.getKey() );
			Resource format = m.createResource().addProperty( RDFS.label, e.getValue().toString() );
			thisPage.addProperty( DCTerms.hasFormat, v );
			v.addProperty( DCTerms.isFormatOf, thisPage );
			v.addProperty( DCTerms.format, format );
		}
	}
	
	private Resource resourceForFormat( Model m, CallContext c, String key ) {
		URI ru = c.getRequestURI();
		try {
			URI x = new URI
				( ru.getScheme()
				, ru.getAuthority()
				, replaceSuffix( key, ru.getPath() )
				, ru.getQuery()
				, ru.getFragment() 
				);
			return m.createResource( x.toASCIIString() );
		} catch (URISyntaxException e) {
			throw new EldaException( "created a broken URI", "", EldaException.SERVER_ERROR, e );
		}
    }

	// TODO should only substitute .foo if it's a renderer or language
	private String replaceSuffix( String key, String oldPath ) {
		int dot_pos = oldPath.lastIndexOf( '.' ), slash_pos = oldPath.lastIndexOf( '/' );
		return dot_pos > -1 && dot_pos > slash_pos
			? oldPath.substring(0, dot_pos + 1) + key
			: oldPath + "." + key
			;
	}

	private void addVersions( Model m, CallContext c, Resource thisPage ) {
		for (String viewName: spec.viewNames()) {
    		Resource v = resourceForView( m, c, viewName );
			thisPage.addProperty( DCTerms.hasVersion, v	);
			v.addProperty( DCTerms.isVersionOf, thisPage );
			v.addProperty( RDFS.label, viewName );
    	}
	}
    
    private void insertResultSetRoot(APIResultSet rs, CallContext context, APIQuery query) {
    	Model rsm = rs.getModel();
        int page = query.getPageNumber();
        int perPage = query.getPageSize();
        Resource uriForSpec = rsm.createResource( spec.getSpecificationURI() ); 
        Resource thisPage = resourceForPage(rsm, context, page);
        rs.setRoot(thisPage);
    //
		thisPage.addProperty( FIXUP.definition, uriForSpec );
        if (query.wantsMetadata( "versions" )) addVersions( rsm, context, thisPage );
        if (query.wantsMetadata( "formats" )) addFormats( rsm, context, thisPage );
        if (query.wantsMetadata( "bindings" ) || true) addBindings( rsm, context, thisPage );
        // also: execution, bindings
    //
        if (isListEndpoint()) {
        	RDFList content = rsm.createList( rs.getResultList().iterator() );
        	thisPage
	        	.addProperty( RDF.type, FIXUP.Page )
	        	.addLiteral( FIXUP.page, page )
	        	.addLiteral( OpenSearch.itemsPerPage, perPage )
	        	.addLiteral( OpenSearch.startIndex, perPage * page + 1 )
	        	;
        	thisPage.addProperty( FIXUP.items, content );
    		thisPage.addProperty( XHV.first, resourceForPage( rsm, context, 0 ) );
    		if (!rs.isCompleted) thisPage.addProperty( XHV.next, resourceForPage( rsm, context, page+1 ) );
    		if (page > 0) thisPage.addProperty( XHV.prev, resourceForPage( rsm, context, page-1 ) );
    		Resource listRoot = resourceForList(rsm, context);
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
        } else if (rs.getResultList().isEmpty()) {
        	EldaException.NoItemFound();
        } else {
        	Resource content = rs.getResultList().get(0);
        	thisPage.addProperty( FOAF.primaryTopic, content );
        	content.addProperty( FOAF.isPrimaryTopicOf, thisPage );     
        	// rs.setContentLocation( query.getSubject() );
        }
    }
    
    private void addBindings(Model rsm, CallContext cc, Resource thisPage) {
		Resource exec = rsm.createResource();
		Property VB = rsm.createProperty( API.NS + "variableBinding" );
		Property TB = rsm.createProperty( API.NS + "termBinding" );
		Property wasResultOf = rsm.createProperty( API.NS + "wasResultOf" );
		exec.addProperty( RDF.type, rsm.createResource( API.NS + "Execution" ) );
	//
		for (Iterator<String> names = cc.parameters.keyIterator(); names.hasNext();) {
			String name = names.next();
			Resource vb = rsm.createResource();
			exec.addProperty( VB, vb );
			vb.addProperty( FIXUP.label, name );
			vb.addProperty( FIXUP.value, cc.getStringValue( name ) );
		}
	//
    	Context c = spec.getAPISpec().getShortnameService().asContext();
    	for (String name: c.allNames()) {
    		Resource tb = rsm.createResource();
    		exec.addProperty( TB, tb );
    		tb.addProperty( FIXUP.label, name );
    		tb.addProperty( API.property, rsm.createResource( c.getURIfromName( name ) ) );
    	}
	//
		thisPage.addProperty( wasResultOf, exec );
	}

	private Resource resourceForPage(Model m, CallContext context, int page) {
        UriBuilder ub = context.getURIBuilder();
        String uri = ub
            .replaceQueryParam(QueryParameter._PAGE, Integer.toString(page))
//            .replacePath( ub.build().getPath() + context.getMediaSuffix() )
            .build()
            .toASCIIString();
        return m.createResource( uri );
    }
    
    private Resource resourceForList(Model m, CallContext context) {
        UriBuilder ub = context.getURIBuilder();
        String uri = ub
            .replaceQueryParam( QueryParameter._PAGE )
            .replaceQueryParam( QueryParameter._PAGE_SIZE )
//            .replacePath( ub.build().getPath() + context.getMediaSuffix() )
            .build().toASCIIString();
        uri = uri.replaceFirst("/meta/", "/api/");
        return m.createResource( uri );
    }

    private Resource resourceForMetaList(Model m, CallContext context) {
        UriBuilder ub = context.getURIBuilder();
        String uri = ub
            .replaceQueryParam(QueryParameter._PAGE)
            .replaceQueryParam(QueryParameter._PAGE_SIZE)
//            .replacePath( ub.build().getPath() + context.getMediaSuffix() )
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
    @Override public APIEndpointSpec getSpec() {
        return spec;
    }

	public String getSelectQuery() {
		PrefixMapping noPrefixes = PrefixMapping.Factory.create();
		return spec.getBaseQuery().assembleSelectQuery( noPrefixes );
	}
	
	@Override public Renderer getRendererNamed( String name ) {
		RendererFactory s = spec.getRendererFactoryTable().getFactoryByName( name );
		if (s == null) return null;
		return s.buildWith( this, getSpec().getAPISpec().getShortnameService() );		
	}
}

