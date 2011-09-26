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

import com.epimorphics.lda.bindings.VarValues;
import com.epimorphics.lda.cache.Cache;
import com.epimorphics.lda.cache.Cache.Registry;
import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.params.Decode;
import com.epimorphics.lda.query.APIQuery;
import com.epimorphics.lda.query.ContextQueryUpdater;
import com.epimorphics.lda.query.QueryArgumentsImpl;
import com.epimorphics.lda.query.QueryParameter;
import com.epimorphics.lda.renderers.*;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.vocabularies.EXTRAS;
import com.epimorphics.lda.vocabularies.OpenSearch;
import com.epimorphics.lda.vocabularies.XHV;
import com.epimorphics.util.Couple;
import com.epimorphics.util.MediaType;
import com.epimorphics.util.RDFUtils;
import com.epimorphics.util.Triad;
import com.epimorphics.util.Util;
import com.epimorphics.vocabs.API;
import com.epimorphics.vocabs.FIXUP;
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
    
    @Override public Triad<APIResultSet, String, VarValues> call( URI reqURI, VarValues given ) {
    	long origin = System.currentTimeMillis();
    	wantsContext = specWantsContext;
    	VarValues cc = given.copyWithDefaults( spec.getBindings() );
        // HERE log.debug("API " + spec + " called on " + cc + " from " + cc.getRequestURI());
    //
        new Decode(true).handleQueryParameters( cc.parameterNames() ).reveal();
    //
        APIQuery query = spec.getBaseQuery();
        Couple<View, String> viewAndFormat = buildQueryAndView( cc, query );
        long timeAfterBuild = System.currentTimeMillis();
        View view = viewAndFormat.a; 
        String format = viewAndFormat.b;
        APIResultSet unfiltered = query.runQuery( spec.getAPISpec(), cache, cc, view );
        long timeAfterRun = System.currentTimeMillis();
        APIResultSet filtered = filterByView( view, query.getDefaultLanguage(), unfiltered );
        filtered.setNsPrefixes( spec.getAPISpec().getPrefixMap() );
        insertResultSetRoot(filtered, reqURI, cc, query);
        long timeAfterMetadata = System.currentTimeMillis();
        log.debug( "TIMING: build query: " + (timeAfterBuild - origin)/1000.0 + "s" );
        log.debug( "TIMING: run query:   " + (timeAfterRun - timeAfterBuild)/1000.0 + "s" );
        log.debug( "TIMING: view query:  " + (timeAfterMetadata - timeAfterRun)/1000.0 );
        return new Triad<APIResultSet, String, VarValues>( filtered, format, cc );
    }

	protected boolean wantsContext = false;
    
    @Override public boolean wantContext() {
    	return wantsContext;
    }

    // Filter by any views
    // In simple cases can combine view props into select then
    // run a construct on the results of the select and do less server traffic.
    // HOWEVER, there are two problems with that:
    // (1) where one (or more) props in view are multi- valued then limit/offset 
    //     doesn't work and the paging code gets complicated
    // (2) where return value is a bNode and we want automatic bNode closure.
    // Current solution get full description from endpoint and post filter
	
    private APIResultSet filterByView( View view, String defaultLanguage, APIResultSet rs ) {
		if (view == null) {
			log.warn( "somehow, filterByTemplate got a null view." );
			return rs;			
		}
		else {			
			log.debug("Applying view: " + view.toString());
			return rs.getFilteredSet( view, defaultLanguage );
		}
	}

    private Couple<View, String> buildQueryAndView( VarValues context, APIQuery query ) {
    	ShortnameService sns = spec.getAPISpec().getShortnameService();
    	QueryArgumentsImpl qa = new QueryArgumentsImpl(query);
    	int endpointType = isListEndpoint() ? ContextQueryUpdater.ListEndpoint : ContextQueryUpdater.ItemEndpoint;
    	ContextQueryUpdater cq = new ContextQueryUpdater( endpointType, context, spec, sns, query, qa );
		try { 
			Couple<View, String> result = cq.updateQueryAndConstructView( query.deferredFilters);
			String format = result.b.equals( "" ) ? context.getValueString( "_suffix") : result.b;
			if (context.getValueString( "callback" ) != null && !"json".equals( format ))
				EldaException.BadRequest( "callback specified but format '" + format + "' is not JSON." );
			qa.updateQuery();
			return result; 
		}
		catch (APIException e) { throw new QueryParseException( "query construction failed", e ); }
    }
    
    /**
     * Return a metadata description for the query that would be run by this endpoint
     */
    @Override public Resource getMetadata(VarValues context, URI ru, Model metadata) {
        APIQuery query = spec.getBaseQuery();
        buildQueryAndView(context, query);
        metadata.setNsPrefix("api", API.getURI());
		Resource meta = resourceForMetaList(metadata, ru );
        APISpec aSpec = spec.getAPISpec();
        meta.addProperty(EXTRAS.sparqlQuery, query.getQueryString(aSpec, context));
        aSpec.getDataSource().addMetadata(meta);
        meta.addProperty( EXTRAS.listURL, resourceForList(metadata, ru) );
        meta.addProperty( RDFS.comment, "Metadata describing the query and source for endpoint " + spec.getURITemplate() );
        return meta;
    }
    
    private boolean isListEndpoint() {
    	return spec.isListEndpoint();
    }

	private Resource resourceForPage( Resource notThisPlease, Model m, URI ru, int page) {
		String newURI = isListEndpoint()
			? EndpointMetadata.replaceQueryParam( ru, QueryParameter._PAGE, Integer.toString(page) )
			: EndpointMetadata.replaceQueryParam( ru, QueryParameter._PAGE );
		Resource thisPage = m.createResource( newURI );
		// System.err.println( ">> changed '" + ru + "' to '" + newURI + "'" );
		// MAGICAL HACK
//		if (thisPage.equals( notThisPlease ) && !isListEndpoint()) {
//			thisPage = m.createResource( newURI + "?" );
//		}
		// System.err.println( ">> Perhaps casting a spell ..." );
		if (!isListEndpoint() && newURI.indexOf('?') < 0) {
			// System.err.println( ">> ... Magical Hack." );
			newURI += "?";
			thisPage = m.createResource( newURI );
		}
		return thisPage;
    }
    
    private Resource createDefinitionURI( Model rsm, URI ru, Resource uriForSpec, String template ) {
    	String quasiTemplate = template.replaceAll( "\\{", "(" ).replaceAll( "\\}", ")");
    	if (template.startsWith("http:")) {
    		// nasty hackery to avoid nasty hackery in the TestAPI uriTemplates, qv.
    		return rsm.createResource( quasiTemplate + "/meta" );
    	}
		String argPattern = "\\{[-._A-Za-z]+\\}";
		String pattern = template.replaceAll( argPattern, "[^/]*" );
		String replaced = ru.toASCIIString().replaceAll( pattern, "/meta" + quasiTemplate );
		return rsm.createResource( replaced );
	}

    private Resource resourceForList( Model m, URI ru ) {
    	String rqp1 = EndpointMetadata.replaceQueryParam( ru, QueryParameter._PAGE );
    	String rqp2 = EndpointMetadata.replaceQueryParam( Util.newURI(rqp1), QueryParameter._PAGE_SIZE );
    	return m.createResource( rqp2 );
    }

    private Resource resourceForMetaList( Model m, URI ru ) {
    	String rqp1 = EndpointMetadata.replaceQueryParam( ru, QueryParameter._PAGE );
    	String rqp2 = EndpointMetadata.replaceQueryParam( Util.newURI(rqp1), QueryParameter._PAGE_SIZE );    	
    	return m.createResource( rqp2 );
    }
    
	private void insertResultSetRoot( APIResultSet rs, URI ru, VarValues cc, APIQuery query ) {
    	Model rsm = rs.getModel();
        int page = query.getPageNumber();
        int perPage = query.getPageSize();
        Resource uriForSpec = rsm.createResource( spec.getSpecificationURI() ); 
        String template = spec.getURITemplate();
        Resource uriForDefinition = createDefinitionURI( rsm, ru, uriForSpec, template ); 
        Resource thisPage = resourceForPage(uriForDefinition, rsm, ru, page);
        rs.setRoot(thisPage);
    //
		thisPage.addProperty( FIXUP.definition, uriForDefinition );
		Set<String> formatNames = spec.getRendererFactoryTable().formatNames();
		EndpointMetadata em = new EndpointMetadata( thisPage, isListEndpoint(), "" + page, cc, ru, formatNames );
		createOptionalMetadata(rs, query, em);   
    //
        String emv_uri = EndpointMetadata.replaceQueryParam( Util.newURI(thisPage.getURI()), "_metadata", "all" );
        thisPage.addProperty( FIXUP.extendedMetadata, rsm.createResource( emv_uri ) );
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
    		thisPage.addProperty( XHV.first, resourceForPage( uriForDefinition, rsm, ru, 0 ) );
    		if (!rs.isCompleted) thisPage.addProperty( XHV.next, resourceForPage( uriForDefinition, rsm, ru, page+1 ) );
    		if (page > 0) thisPage.addProperty( XHV.prev, resourceForPage( uriForDefinition, rsm, ru, page-1 ) );
    		Resource listRoot = resourceForList(rsm, ru);
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
        } else if (rs.isEmpty()) {
        	EldaException.NoItemFound();
        } else {
        	Resource content = rs.getResultList().get(0);
        	thisPage.addProperty( FOAF.primaryTopic, content );
        	content.addProperty( FOAF.isPrimaryTopicOf, thisPage );
        	// rs.setContentLocation( query.getSubject() );
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

