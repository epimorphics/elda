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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.epimorphics.util.MediaType;
import com.epimorphics.util.Triad;
import com.epimorphics.util.Util;
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
    
    @Override public Triad<APIResultSet, String, CallContext> call( CallContext given ) {
    	wantsContext = specWantsContext;
    	CallContext cc = new CallContext( spec.getBindings(), given );
        log.debug("API " + spec + " called on " + cc + " from " + cc.getRequestURI());
        APIQuery query = spec.getBaseQuery();
        Couple<View, String> viewAndFormat = buildQueryAndView( cc, query );
        View view = viewAndFormat.a; 
        String format = viewAndFormat.b;
        APIResultSet unfiltered = query.runQuery( spec.getAPISpec(), cache, cc, view );
        APIResultSet filtered = filterByView( view, unfiltered );
        filtered.setNsPrefixes( spec.getAPISpec().getPrefixMap() );
        insertResultSetRoot(filtered, cc, query);
        return new Triad<APIResultSet, String, CallContext>( filtered, format, cc );
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


	private Resource resourceForPage(Model m, CallContext context, int page) {
		URI ru = context.getRequestURI();
		String newURI = EndpointMetadata.replaceQueryParam( ru, QueryParameter._PAGE, Integer.toString(page) );
		// System.err.println( ">> changed '" + ru + "' to '" + newURI + "'" );
		return m.createResource( newURI );
    }
    
    private Resource resourceForList(Model m, CallContext context) {		
    	URI ru = context.getRequestURI();
    	String rqp1 = EndpointMetadata.replaceQueryParam( ru, QueryParameter._PAGE );
    	String rqp2 = EndpointMetadata.replaceQueryParam( Util.newURI(rqp1), QueryParameter._PAGE_SIZE );
    	return m.createResource( rqp2 );
    }

    private Resource resourceForMetaList( Model m, CallContext context ) {
    	URI ru = context.getRequestURI();
    	String rqp1 = EndpointMetadata.replaceQueryParam( ru, QueryParameter._PAGE );
    	String rqp2 = EndpointMetadata.replaceQueryParam( Util.newURI(rqp1), QueryParameter._PAGE_SIZE );    	
    	return m.createResource( rqp2 );
    }
    
	private void insertResultSetRoot( APIResultSet rs, CallContext context, APIQuery query ) {
    	Model rsm = rs.getModel();
        int page = query.getPageNumber();
        int perPage = query.getPageSize();
        Resource uriForSpec = rsm.createResource( spec.getSpecificationURI() ); 
        String template = spec.getURITemplate();
        URI ru = context.getRequestURI();
        Resource uriForDefinition = createDefinitionURI( rsm, ru, uriForSpec, template ); 
        Resource thisPage = resourceForPage(rsm, context, page);
        rs.setRoot(thisPage);
        Resource exec = rsm.createResource();
    //
		thisPage.addProperty( FIXUP.definition, uriForDefinition );
		Model versions = ModelFactory.createDefaultModel();
		Model formats = ModelFactory.createDefaultModel();
		Model bindings = ModelFactory.createDefaultModel();
		Model execution = ModelFactory.createDefaultModel();
		
		EndpointMetadata.addVersions( versions, spec.viewNames(), context, thisPage );
        if (query.wantsMetadata( "versions" )) rsm.add( versions );
        
        if (query.wantsMetadata( "formats" )) EndpointMetadata.addFormats( rsm, context, thisPage, spec.getRendererFactoryTable() );
        if (query.wantsMetadata( "bindings" )) EndpointMetadata.addBindings( rsm, exec, spec.getAPISpec().getShortnameService().nameMap(), context, thisPage );
        if (query.wantsMetadata( "execution" )) EndpointMetadata.addExecution( rsm, exec, context, thisPage );
        if (query.wantsMetadata( "execution" )) EndpointMetadata.addQueryMetadata( rsm, exec, context, query, rs.getDetailsQuery(), spec.getAPISpec(), isListEndpoint() );
    //
        String and = thisPage.getURI().indexOf("?") < 0 ? "?" : "&";
        String emv_uri = thisPage.getURI() + and + "_metadata=all";
        Resource emv = rsm.createResource( emv_uri );
        thisPage.addProperty( FIXUP.extendedMetadata, emv );
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
	    		.addProperty( FIXUP.definition, uriForDefinition ) 
	    		.addProperty( RDF.type, API.ListEndpoint )
	    		// .addProperty( RDFS.label, "should be a description of this list" )
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
    
    private Resource createDefinitionURI( Model rsm, URI ru, Resource uriForSpec, String template ) {
    	if (template.startsWith("http:")) {
    		// nasty hackery to avoid nasty hackery in the TestAPI uriTemplates, qv.
    		return rsm.createResource( template + "/meta" );
    	}
		String remove = "/?" + template.replace("{", "\\{" ).replace( "}", "\\}" ) + "(\\.[-A-Za-z]+)?";
		String result = ru.toASCIIString().replaceAll( remove, "/meta" + template );
		return rsm.createResource( result );
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
		PrefixMapping noPrefixes = PrefixMapping.Factory.create();
		return spec.getBaseQuery().assembleSelectQuery( noPrefixes );
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

