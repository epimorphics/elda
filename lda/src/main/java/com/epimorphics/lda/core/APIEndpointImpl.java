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
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.lda.cache.Cache;
import com.epimorphics.lda.cache.Cache.Registry;
import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.renderers.*;
import com.epimorphics.lda.shortnames.NameMap;
import com.epimorphics.lda.shortnames.NameMap.Stage2NameMap;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.vocabularies.ELDA;
import com.epimorphics.lda.vocabularies.EXTRAS;
import com.epimorphics.lda.vocabularies.OpenSearch;
import com.epimorphics.lda.vocabularies.XHV;
import com.epimorphics.util.Couple;
import com.epimorphics.util.MediaType;
import com.epimorphics.util.MediaTypes;
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
        View view = viewAndFormat.a; String format = viewAndFormat.b;
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

    private Resource resourceForView( Model m, CallContext context, String name ) {
    	URI req = context.getRequestURI();
    	return m.createResource( replaceQueryParam( req, QueryParameter._VIEW, name ) );
    }

	private String replaceQueryParam(URI ru, String key, String... values) {
		try {
			String q = ru.getQuery();
			String qa = q == null ? "" : strip( q, key );
			String qb = qa.isEmpty() ? "" : qa + "?";
			String newq = "";
			for (String value: values) {
				newq = newq + qb + key + "=" + quoteForValue(value);
				qb = "&";
			}
			return new URI
				(
				ru.getScheme(), 
				ru.getAuthority(), 
				ru.getPath(),
				(newq.isEmpty() ? null : newq), 
				ru.getFragment() 
				).toASCIIString();
		} catch (URISyntaxException e) {			
			throw new EldaException( "created a broken URI", "", EldaException.SERVER_ERROR, e );
		}
	}

	private String quoteForValue(String value) {
		return value.replace( "&", "%??" );
	}

	private String strip( String query, String key ) {
		return query.replaceAll( "(^|[&])" + key + "=[^&]*[&]?", "" );
	}

	private void addFormats(Model m, CallContext c, Resource thisPage) {
		for (Map.Entry<String, MediaType> e: MediaTypes.createMediaExtensions().entrySet()) {
			String formatName = e.getKey();
			Resource v = resourceForFormat( m, c, formatName );
			Resource format = m.createResource().addProperty( RDFS.label, e.getValue().toString() );
			thisPage.addProperty( DCTerms.hasFormat, v );
			v.addProperty( DCTerms.isFormatOf, thisPage );
			v.addProperty( DCTerms.format, format );
			v.addProperty( RDFS.label, formatName );
		}
	}
	
	private Resource resourceForFormat( Model m, CallContext c, String formatName ) {
		URI ru = c.getRequestURI();
		try {
			URI x = new URI
				( ru.getScheme()
				, ru.getAuthority()
				, replaceSuffix( formatName, ru.getPath() )
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
	private Resource resourceForPage(Model m, CallContext context, int page) {
		URI ru = context.getRequestURI();
		String newURI = replaceQueryParam( ru, QueryParameter._PAGE, Integer.toString(page) );
		return m.createResource( newURI );
    }
    
    private Resource resourceForList(Model m, CallContext context) {		
    	URI ru = context.getRequestURI();
    	String rqp1 = replaceQueryParam( ru, QueryParameter._PAGE );
    	String rqp2 = replaceQueryParam( Util.newURI(rqp1), QueryParameter._PAGE_SIZE );
    	return m.createResource( rqp2 );
    }

    private Resource resourceForMetaList(Model m, CallContext context) {
    	URI ru = context.getRequestURI();
    	String rqp1 = replaceQueryParam( ru, QueryParameter._PAGE );
    	String rqp2 = replaceQueryParam( Util.newURI(rqp1), QueryParameter._PAGE_SIZE );    	
    	return m.createResource( rqp2 );
    }

	private void addVersions( Model m, CallContext c, Resource thisPage ) {
		for (String viewName: spec.viewNames()) {
    		Resource v = resourceForView( m, c, viewName );
			thisPage.addProperty( DCTerms.hasVersion, v	);
			v.addProperty( DCTerms.isVersionOf, thisPage );
			v.addProperty( RDFS.label, viewName );
    	}
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
        if (query.wantsMetadata( "versions" )) addVersions( rsm, context, thisPage );
        if (query.wantsMetadata( "formats" )) addFormats( rsm, context, thisPage );
        if (query.wantsMetadata( "bindings" )) addBindings( rsm, exec, context, thisPage );
        if (query.wantsMetadata( "execution" )) addExecution( rsm, exec, context, thisPage );
        addQueryMetadata( rsm, exec, context, thisPage, query );
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
    
    private void addQueryMetadata( Model rsm, Resource exec, CallContext context, Resource thisPage, APIQuery q ) {
    	String SPARQL = "http://purl.org/net/opmv/types/sparql#";
    	Resource QueryResult = rsm.createResource( SPARQL + "QueryResult" );
    	Property query = rsm.createProperty( SPARQL + "query" );
    	Property viewingResult = rsm.createProperty( API.NS, "viewingResult" );
    	Property selectionResult = rsm.createProperty( API.NS, "selectionResult" );
    //
    	Resource sr = rsm.createResource();
    	sr.addProperty( RDF.type, QueryResult );    	
    	sr.addProperty( query, inValue( rsm, q.getQueryString( spec.getAPISpec(), context ) ) );
    //
    	Resource vr = rsm.createResource();
    	vr.addProperty( RDF.type, QueryResult );
    	vr.addProperty( query, inValue( rsm, q.getQueryString( spec.getAPISpec(), context ) ) ); // WRONG ONE
    //
		exec.addProperty( viewingResult, vr );
		exec.addProperty( selectionResult, sr );
	}

	private Resource inValue(Model rsm, String s) {
		Resource v = rsm.createResource();
		v.addProperty( RDF.value, s );
		return v;
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

    // following the Puelia model.
    private void addExecution( Model rsm, Resource exec, CallContext cc, Resource thisPage ) {
		exec.addProperty( RDF.type, FIXUP.Execution );
		Resource P = rsm.createResource();
		ELDA.addEldaMetadata( P );
		exec.addProperty( FIXUP.processor, P );
		thisPage.addProperty( FIXUP.wasResultOf, exec );
	}

	private void addBindings( Model rsm, Resource exec, CallContext cc, Resource thisPage ) {
		exec.addProperty( RDF.type, FIXUP.Execution );
	//
		addVariableBindings(rsm, exec, cc);
	//
		NameMap nm = spec.getAPISpec().getShortnameService().nameMap();
		Stage2NameMap s2 = nm.stage2(false);
		MultiMap<String, String> mm = s2.result();
		for (String uri: mm.keySet()) {
    		Resource term = rsm.createResource( uri );
    		if (rsm.containsResource( term )) {
    			Set<String> shorties = mm.getAll( uri );
    			String shorty = shorties.iterator().next();
    			if (shorties.size() > 1) {
    				log.warn( "URI <" + uri + "> has several short names, viz: " + shorties + "; picked " + shorty );
    			}
	    		Resource tb = rsm.createResource();
	    		exec.addProperty( FIXUP.TB, tb );
				tb.addProperty( FIXUP.label, shorty );
				tb.addProperty( API.property, term );
    		}
    	}
		
	//
		if (false) addTermBindings(rsm, exec);
	//
		thisPage.addProperty( FIXUP.wasResultOf, exec );
	}

	private void addVariableBindings(Model rsm, Resource exec, CallContext cc) {
		for (Iterator<String> names = cc.parameters.keyIterator(); names.hasNext();) {
			String name = names.next();
			Resource vb = rsm.createResource();
			exec.addProperty( FIXUP.VB, vb );
			vb.addProperty( FIXUP.label, name );
			vb.addProperty( FIXUP.value, cc.getStringValue( name ) );
		}
	}

	private void addTermBindings(Model rsm, Resource exec) {
		Context c = spec.getAPISpec().getShortnameService().asContext();
    	for (String name: c.allNames()) {
    		Resource term = rsm.createResource( c.getURIfromName( name ) );
    		if (rsm.containsResource( term )) {
	    		Resource tb = rsm.createResource();
	    		exec.addProperty( FIXUP.TB, tb );
	    		tb.addProperty( FIXUP.label, name );
				tb.addProperty( API.property, term );
    		}
    	}
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

