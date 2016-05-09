/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
    
    File:        APIEndpointImpl.java
    Created by:  Dave Reynolds
    Created on:  31 Jan 2010
*/

package com.epimorphics.lda.core;

import java.net.URI;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.jsonrdf.ContextPropertyInfo;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.cache.*;
import com.epimorphics.lda.cache.Cache.Registry;
import com.epimorphics.lda.cache.LimitedCacheBase.TimedThing;
import com.epimorphics.lda.core.APIResultSet.MergedModels;
import com.epimorphics.lda.exceptions.*;
import com.epimorphics.lda.exceptions.QueryParseException;
import com.epimorphics.lda.query.*;
import com.epimorphics.lda.renderers.Factories.FormatNameAndType;
import com.epimorphics.lda.renderers.*;
import com.epimorphics.lda.shortnames.CompleteContext;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.sources.Source;
import com.epimorphics.lda.sources.Source.ResultSetConsumer;
import com.epimorphics.lda.specs.*;
import com.epimorphics.lda.support.NoteBoard;
import com.epimorphics.lda.support.panel.Switches;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.lda.vocabularies.ELDA_API;
import com.epimorphics.util.*;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.util.StringUtils;
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
    
    /**
        Return this endpoints prefix path, which is the prefix path of its
        parent API spec.
    */
    @Override public String getPrefixPath() {
    	return getSpec().getAPISpec().getPrefixPath();
    }
	
	static final Bindings defaults = new Bindings().put( "_resourceRoot", "{_APP}/lda-assets/" );
	
    @Override public Bindings defaults() {
    	return defaults;
    }
            
    @Override public ResponseResult call(Request r, NoteBoard nb) {
		URI key = Switches.stripCacheKey(r.bindings) ? r.getURIplain() : r.getURIwithFormat();
		
	    	Bindings b = r.bindings.copyWithDefaults( spec.getBindings() );
	    	APIQuery query = spec.getBaseQuery();
	    	
		    if (b.getValueString( "callback" ) != null && !"json".equals( r.format ))
				EldaException.BadRequest( "callback specified but format '" + r.format + "' is not JSON." );  
		    
		    View view = buildQueryAndView( b, query );
		    b.put("_selectedView", view.nameWithoutCopy());
	        
		    Source dataSource = spec.getAPISpec().getDataSource();

		    nb.expiresAt = query.viewSensitiveExpiryTime(spec.getAPISpec(), view);
			nb.totalResults = query.requestTotalCount(nb.expiresAt, r.c, cache, dataSource, b, spec.getAPISpec().getPrefixMap());	    
			
			ResponseResult toReturn = null;
			
	    	TimedThing<ResponseResult> fromCache = cache.fetch(key);
	        if (fromCache == null || r.c.allowCache == false) {
	        	// must construct and cache a new response-result
	//        	System.err.println(">>  Fresh.");
	        	ResponseResult fresh = freshResponse(b, query, view, r, nb);
	    		cache.store(key, fresh, nb.expiresAt);
	        	toReturn = decorate(false, b, query, fresh, r, nb);
	        } else {
	//        	System.err.println(">>  Re-use.");
	        	// re-use the existing response-result
	        	nb.expiresAt = fromCache.expiresAt;
	        	toReturn = decorate(true, b, query, fromCache.thing, r, nb);
	        }
	        
	        List<Resource> items = toReturn.resultSet.results;
			String licenceQuery = getLicences(items);
			runLicenceQuery(dataSource, licenceQuery);
	        return toReturn;
    }

	private Set<String> runLicenceQuery(Source dataSource, String licenceQuery) {
		final Set<String> licences = new HashSet<String>();
		if (licenceQuery != null) {
			ResultSetConsumer consume = new ResultSetConsumer() {
				
				@Override public void setup(QueryExecution qe) {
					
				}
				
				@Override public void consume(ResultSet rs) {
					while (rs.hasNext()) {
						QuerySolution next = rs.next();
						System.err.println(">> query solution: " + next);
						Iterator<String> it = next.varNames();
						System.err.println(">> it is " + it);
						while (it.hasNext()) {
							System.err.println(">> next name --------------------------");
							System.err.println(">> name: " + it.next());
						}
						RDFNode rdfNode = next.get("license");
						System.err.println(">> binding for license is " + rdfNode);
						String l = rdfNode.asResource().getURI();
						licences.add(l);
					}
				}
			};
			dataSource.executeSelect(QueryFactory.create(licenceQuery), consume);
		}
		return licences;
	}

	private String getLicences(List<Resource> items) {
		System.err.println(">> == handling licences ==================");
		
		ShortnameService sns = spec.getAPISpec().getShortnameService();
		
		Set<Resource> licences = new HashSet<Resource>();
		Set<String> paths = new HashSet<String>();
		
		for (RDFNode l: spec.getLicenceNodes()) {
			if (l.isResource()) 
				licences.add(l.asResource());
			else
				paths.add(l.asLiteral().getLexicalForm());
		}
		
		
		if (paths.size() > 0) {
			List<String> queryLines = new ArrayList<String>();
			queryLines.add("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>");
			queryLines.add("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>");
			queryLines.add("SELECT DISTINCT ?license WHERE {");
			queryLines.add("VALUES ?item {");
			
			for (Resource item: items) {
				queryLines.add("<" + item.getURI() + ">");
			}
			
			queryLines.add("  }");
			queryLines.add("  {");
			
			boolean first = true;
			String currentVar = "item";
			String prefix = "";
			
			for (String path: paths) {
				if (!first) queryLines.add("    UNION");
				first = false;
				String prev = "";
				// handle a path
				String [] elements = path.split("\\.");
				int remaining = elements.length;
				for (String element: elements) {
					remaining -= 1;
					boolean last = remaining == 0;
					String newPrev = prev + "_" + predicateName(sns, element);
					String nextVar = (last ? "license" : newPrev);
					prev = newPrev;
					String S = "?" + currentVar;
					String P = "<" + predicateURI(sns, element) + ">";
					String O = "?" + nextVar;
					if (isInverse(element)) {
						queryLines.add("  " + O + " " + P + " " + S + " .");
					} else {
						queryLines.add("  " + S + " " + P + " " + O + " .");
					}
					prefix = prefix + "_" + nextVar;
					currentVar = nextVar;
				}
			}
			
			queryLines.add("  }");
			
			queryLines.add("}");
			
			
			String queryString = org.apache.commons.lang.StringUtils.join(queryLines, "\n");
			System.err.println(">>\n" + queryString);
			return queryString;
		}
		return null;
	}
    
    private String predicateURI(ShortnameService sns, String element) {
    	if (element.startsWith("~")) element = element.substring(1);
    	ContextPropertyInfo i = sns.getPropertyByName(element);
    	System.err.println(">> predicateURI: expand " + element + " yields " + sns.expand(element));
    	if (true) return sns.expand(element);
    	if (i == null) {
    		System.err.println(">> predicateURI: asking about " + element + " (" + sns.expand(element) + ")");
    		throw new UnknownShortnameException(element);
    	}
		return i.getURI();
	}
    
    private String predicateName(ShortnameService sns, String element) {
    	if (element.startsWith("~")) element = element.substring(1);
    	return element;
	}
    
    private boolean isInverse(String element) {
    	return element.startsWith("~");
    }

	protected ResponseResult freshResponse(Bindings b, APIQuery query, View view, Request r, NoteBoard nb) {
	//    
    	APIResultSet unfiltered = query.runQuery(nb, r.c, spec.getAPISpec(), cache, b, view );	    
	    APIResultSet filtered = unfiltered.getFilteredSet( view, query.getDefaultLanguage() );
	    filtered.setNsPrefixes( spec.getAPISpec().getPrefixMap() );
	//
	    return new ResponseResult(false, filtered, null, b);
    }
    
    protected ResponseResult decorate(boolean isFromCache, Bindings b, APIQuery query, ResponseResult basis, Request r, NoteBoard nb) {
    //
	    APIResultSet rs = new APIResultSet(basis.resultSet);
		CompleteContext cc = createMetadata(r, nb, b, query, rs);
		return new ResponseResult(isFromCache, rs, cc.Do(), b);
    }

	private CompleteContext createMetadata(Request r, NoteBoard nb, Bindings b, APIQuery query, APIResultSet filtered) {
		Context context = spec.getAPISpec().getShortnameService().asContext();
		CompleteContext cc = new CompleteContext( r.mode, context, filtered.getModelPrefixes() );   
	    createMetadata( r, cc, nb.totalResults, filtered, b, query );
	    cc.include( filtered.getMergedModel() );
		return cc;
	}

    private View buildQueryAndView( Bindings context, APIQuery query ) {
    	ShortnameService sns = spec.getAPISpec().getShortnameService();
    	int endpointType = isListEndpoint() ? ContextQueryUpdater.ListEndpoint : ContextQueryUpdater.ItemEndpoint;
    	ContextQueryUpdater cq = new ContextQueryUpdater( endpointType, context, spec, sns, query );
		try { 
			return cq.updateQueryAndConstructView( query.deferredFilters ); 
		} catch (APIException e) { 
			throw new QueryParseException( "query construction failed", e ); 
		}
    }
    
    /**
     * Return a metadata description for the query that would be run by this endpoint
     */
    @Override public Resource getMetadata(Bindings context, URI ru, String formatName, Model metadata) {
        APIQuery query = spec.getBaseQuery();
        @SuppressWarnings("unused") View _ignored = buildQueryAndView(context, query);
        metadata.setNsPrefix("api", API.getURI());
		Resource meta = metadata.createResource( URIUtils.withoutPageParameters( ru ).toString() );
        APISpec aSpec = spec.getAPISpec();
        meta.addProperty(ELDA_API.sparqlQuery, query.getQueryString(aSpec, context));
        aSpec.getDataSource().addMetadata(meta);
        meta.addProperty( ELDA_API.listURL, meta );
        meta.addProperty( RDFS.comment, "Metadata describing the query and source for endpoint " + spec.getURITemplate() );
        return meta;
    }
    
    private boolean isListEndpoint() {
    	return spec.isListEndpoint();
    }

    // template -- the unexpanded uri template for this endpoint
    // expanded -- that same template but with all possible variables expanded
	private String createDefinitionURI( URI ru, Resource uriForSpec, String template, String expanded ) {
		// the pseudo-template converts {name} to _name so that it's a legal
		// URI component for feeding back into a request URI.
    	String pseudoTemplate = template.replaceAll( "\\{([A-Za-z0-9]+)\\}", "_$1" );
    //
       	if (pseudoTemplate.startsWith("http:")) {
    		// Avoid special case from the TestAPI uriTemplates, qv.
    		return pseudoTemplate + "/meta";
    	}
    //
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

    private void createMetadata( APIEndpoint.Request r, CompleteContext cc, Integer totalResults, APIResultSet rs, Bindings bindings, APIQuery query ) {

    	URI uriWithFormat = r.getURIwithFormat();
		boolean suppress_IPTO = bindings.getAsString( "_suppress_ipto", "no" ).equals( "yes" );
//		boolean exceptionIfEmpty = bindings.getAsString( "_exceptionIfEmpty", "yes" ).equals( "yes" );
	//
		// if (rs.isEmpty() && exceptionIfEmpty) EldaException.NoItemFound();
	//
		MergedModels mergedModels = rs.getModels();		
		Model metaModel = mergedModels.getMetaModel();
		cc.include( metaModel );
	//
		// Resource thisMetaPage = metaModel.createResource( r.getURIwithFormat().toString() ); // requestURI.toString() ); 
		Resource thisMetaPage = metaModel.createResource( uriWithFormat.toString() ); 
		Resource uriForSpec = metaModel.createResource( spec.getSpecificationURI() ); 
	//
        int page = query.getPageNumber();
        int perPage = query.getPageSize();
    //
        String template = spec.getURITemplate();
        rs.setRoot(thisMetaPage);
    //
        List<Resource> resultList = rs.getResultList();
        boolean hasMorePages = !rs.isCompleted;
        SetsMetadata setsMeta = (SetsMetadata) rs;
        WantsMetadata wantsMeta = (WantsMetadata) query;
        String viewQuery = rs.getDetailsQuery();
        APISpec apiSpec = spec.getAPISpec();
        Source source = apiSpec.getDataSource();
        String selectQuery = query.getQueryString( apiSpec, bindings );
        Map<String, View> views = spec.extractViews();
        EndpointDetails details = (EndpointDetails) spec;
        Set<FormatNameAndType> formats = spec.getRendererFactoryTable().getFormatNamesAndTypes();
        URI uriForList = URIUtils.withoutPageParameters( uriWithFormat );
    //     
        Resource uriForDefinition = metaModel.createResource( createDefinitionURI( uriForList, uriForSpec, template, bindings.expandVariables( template ) ) ); 
        EndpointMetadata.addAllMetadata
        	( mergedModels
        	, uriForList
        	, uriForDefinition
        	, bindings
        	, cc
        	, suppress_IPTO
        	, thisMetaPage
        	, page
        	, perPage
        	, totalResults
        	, hasMorePages
        	, resultList
        	, setsMeta
        	, wantsMeta
        	, selectQuery
        	, viewQuery
        	, source
        	, views
        	, formats
        	, details
        	);   
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
        state of this endpoint to find the items of interest.
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

