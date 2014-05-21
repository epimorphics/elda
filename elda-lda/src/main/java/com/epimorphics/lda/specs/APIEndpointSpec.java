/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.specs;

import static com.epimorphics.util.RDFUtils.*;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.bindings.VariableExtractor;
import com.epimorphics.lda.core.*;
import com.epimorphics.lda.exceptions.APIException;
import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.query.APIQuery;
import com.epimorphics.lda.renderers.Factories;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.support.RendererFactoriesSpec;
import com.epimorphics.lda.textsearch.TextSearchConfig;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.lda.vocabularies.EXTRAS;
import com.epimorphics.util.RDFUtils;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Encapsulates the specification of the particular List/Set within
 * a particular API. 
 */
public class APIEndpointSpec implements EndpointDetails, NamedViews, APIQuery.QueryBasis {
	
	private final APISpec apiSpec;
	
    protected final APISpec parentApi;
    protected final String name;
    protected final Resource endpointResource;
    
    protected final TextSearchConfig textSearchConfig;
    
    protected APIQuery baseQuery;
    protected String uriTemplate ;
    
    protected final Map<String, View> views;
    protected final String defaultLanguage;
    
    protected final String itemTemplate;
    
    protected final String graphTemplate;
    
    protected final boolean wantsContext;
    
    protected final String cachePolicyName;
    
    protected final int describeThreshold;
    
    public final int defaultPageSize;
    public final int maxPageSize;

    protected final Bindings bindings = new Bindings();
    
    protected final Set<String> explicitViewNames = new HashSet<String>();
    
    protected final Factories factoryTable;
    
    final Boolean enableCounting;
    
    final long cacheExpiryMilliseconds;
    
    static Logger log = LoggerFactory.getLogger(APIEndpointSpec.class);
    
    public APIEndpointSpec( APISpec apiSpec, APISpec parent, Resource endpoint ) {
    	checkEndpointType( endpoint );
    	this.apiSpec = apiSpec;
    	wantsContext = endpoint.hasLiteral( EXTRAS.wantsContext, true );
    	bindings.putAll( apiSpec.bindings );
        bindings.putAll( VariableExtractor.findAndBindVariables( bindings, endpoint ) );
        defaultLanguage = getStringValue(endpoint, API.lang, apiSpec.getDefaultLanguage());
    	defaultPageSize = getIntValue( endpoint, API.defaultPageSize, apiSpec.defaultPageSize );
		maxPageSize = getIntValue( endpoint, API.maxPageSize, apiSpec.maxPageSize );
		cachePolicyName = getStringValue( endpoint, EXTRAS.cachePolicyName, apiSpec.getCachePolicyName() );
		parentApi = parent;
        name = endpoint.getLocalName();
        itemTemplate = getStringValue( endpoint, API.itemTemplate, null );
        graphTemplate = getStringValue( endpoint, EXTRAS.graphTemplate, apiSpec.getGraphTemplate() );
        uriTemplate = createURITemplate( endpoint );
        endpointResource = endpoint;
        describeThreshold = getIntValue( endpoint, EXTRAS.describeThreshold, apiSpec.describeThreshold );
    //
        textSearchConfig = apiSpec.getTextSearchConfig().overlay( endpoint );
        enableCounting = RDFUtils.getOptionalBooleanValue(endpoint, EXTRAS.enableCounting, apiSpec.getEnableCounting() );
        cacheExpiryMilliseconds = PropertyExpiryTimes.getSecondsValue
        	( endpoint
        	, EXTRAS.cacheExpiryTime
        	, apiSpec.getCacheExpiryMilliseconds() / 1000
        	) * 1000
        	;
    //        
        instantiateBaseQuery( endpoint ); 
        views = extractViews( endpoint );
        factoryTable = RendererFactoriesSpec.createFactoryTable( endpoint, apiSpec.getRendererFactoryTable() );
    }

	public String createURITemplate( Resource endpoint ) {
		Resource spec = specForEndpoint(endpoint);
		String ut = getStringValue(endpoint, API.uriTemplate, null);
        if (ut == null) EldaException.NoDeploymentURIFor( name );
        String prefix = getStringValue( spec, EXTRAS.uriTemplatePrefix, "" );
        if (!ut.startsWith("/") && !ut.startsWith("http")) ut = "/" + ut;
        return prefix + ut;
	}

	public boolean isListEndpoint() {
    	return endpointResource.hasProperty( RDF.type, API.ListEndpoint );
    }
	
	@Override public boolean isItemEndpoint() {
    	return endpointResource.hasProperty( RDF.type, API.ItemEndpoint );
    }
    
    private void checkEndpointType(Resource endpoint) {
    	boolean isList = endpoint.hasProperty( RDF.type, API.ListEndpoint );
    	boolean isItem = endpoint.hasProperty( RDF.type, API.ItemEndpoint );
    	if (isList || isItem) return;
    	log.warn( "endpoint " + endpoint + " is not declared as ListEndpoint or ItemEndpoint -- unexpected behaviour may result." );
	}

    public boolean wantsContext() {
    	return wantsContext;
    }
    
    public String getCachePolicyName() {
    	return cachePolicyName;
    }
    
	public String getURI() {
        return endpointResource.getURI();
    }
    
    public Resource getResource() {
        return endpointResource;
    }
    
    public Set<String> getExplicitViewNames() {
    	return new HashSet<String>( explicitViewNames );
    }
    
    /**
        Answer a map from view names to views as defined by the
        endpoint specification. The map includes the magic key
        APIQuery.SHOW_DEFAULT_INTERNAL for the default view;
        if no default view is supplied, View is used.
        The map also includes an entry for APIQuery.SHOW_ALL (ie,
        the _view key used in the API URL) mapping to View.ALL;
        this provides a minor simplification in that code.        
    */
    private Map<String, View> extractViews( Resource endpoint ) {
    	Resource parent = specForEndpoint( endpoint );
    	
    	Map<String, View> result = new HashMap<String, View>(); 
    	
		result.put( View.SHOW_ALL, View.ALL );
		result.put( View.SHOW_BASIC, View.BASIC );
		result.put( View.SHOW_DESCRIPTION, View.DESCRIBE );
		
		View a = getDefaultView( parent, View.DESCRIBE );
		View b = getDefaultView( endpoint, a );
		
		result.put( View.SHOW_DEFAULT_INTERNAL, b );
		
		result.put( a.name(), a );
		result.put( b.name(), b );
		
		Map<String, View> views = result;
		addViewers( parent, views );
    	addViewers( endpoint, views );
    	
        return views;
    }
    
    public Map<String, View> extractViews() {
    	return extractViews( endpointResource );
    }

	public void addViewers(Resource root, Map<String, View> views) {
		Model m = root.getModel();
				
        for (NodeIterator ni =  m.listObjectsOfProperty( root, API.viewer ); ni.hasNext();) {
            RDFNode tNode = ni.next();
            if (!tNode.isResource()) 
                throw new APIException("Found literal " + tNode + " when expecting a view resource");
            View v = getView( (Resource) tNode );
            views.put( v.name(), v );
            explicitViewNames.add( v.name() );
        }
	}
    
	/**
	    Extract a view based on the resource <code>v</code>. v may
	    name a builtin view, in which case it is returned unchanged
	    (any properties are ignored). Otherwise a view is constructed,
	    given a name, installed into the view table, and returned.
	
	*/
    private View getView( Resource v ) {
    	View builtin = View.getBuiltin( v );
        if (builtin == null) {
        	String viewName = getNameWithFallback( v );
        	if (v.hasProperty( API.template )) {
        		String t = v.getProperty( API.template ).getString();
        		return View.newTemplateView( viewName, t );
        	} else {
        		return getViewByProperties( v.getModel(), viewName, v );
        	}
        } else 
        	return builtin;
    }

	private String getNameWithFallback(Resource tRes) {
		String s = getStringValue( tRes, API.name );
		return s == null ? getNameFor(tRes) : s;
	}
    
    private View getDefaultView( Resource root, View ifAbsent ) {
    	if (root.hasProperty( API.defaultViewer )) {
    		Resource x = getResourceValue( root, API.defaultViewer );
    		return getView( x );   		
    	} else
    		return ifAbsent;
    }

    private View getViewByProperties( Model m, String name, Resource tRes ) {
        return addViewProperties( m, new HashSet<Resource>(), tRes, new View( name ) );
	}

    /**
        Add properties to the view, setting the property chains and possibly
        the labelled-describe label property URI.
    */
	private View addViewProperties( Model m, Set<Resource> seen, Resource tRes, View v ) {
		setDescribeLabelIfPresent( tRes, v );
		setDescribeThreshold( tRes, v );
		addViewPropertiesByString( v, m.listObjectsOfProperty( tRes, API.properties ).toList() );
		addViewPropertiesByResource( v, m.listObjectsOfProperty( tRes, API.property ).toList() );
		for (RDFNode n: tRes.listProperties( API.include ).mapWith( Statement.Util.getObject ).toList()) {
			if (n.isResource() && seen.add( (Resource) n ))
				addViewProperties( m, seen, (Resource) n, v );
		}
		return v;
	}

	private void setDescribeThreshold(Resource tRes, View v) {
		if (tRes.hasProperty( EXTRAS.describeThreshold ))
			v.setDescribeThreshold( getIntValue( tRes, EXTRAS.describeThreshold, describeThreshold ) );
	}

	private void setDescribeLabelIfPresent(Resource tRes, View v) {
		if (tRes.hasProperty( EXTRAS.describeAllLabel )) 
			v.setDescribeLabel( getStringValue( tRes, EXTRAS.describeAllLabel, RDFS.label.getURI() ) );
	}

	private void addViewPropertiesByString( View v, List<RDFNode> items ) {
		ShortnameService sns = apiSpec.getShortnameService();
		for (RDFNode pNode: items) {
            if (pNode.isLiteral()) {
            	for(String dotted : pNode.asNode().getLiteralLexicalForm().split(" *, *")) {
					v.addViewFromParameterValue(dotted, sns);
	        	}
	        } else {
	        	EldaException.BadSpecification( "object of api:properties not a literal: " + pNode );
	        }
	    }
	}   

	private void addViewPropertiesByResource( View v, List<RDFNode> items ) {
		ShortnameService sns = apiSpec.getShortnameService();
		for (RDFNode pNode: items) {
            if (pNode.isResource()) {
                v.addViewFromRDFList((Resource)pNode, sns);
            } else 
	        	EldaException.BadSpecification( "object of api:property is a literal: " + pNode );
	
	    }
	}    

	@Override public ShortnameService sns() {
		return apiSpec.getShortnameService();
	}
	
	@Override public int getMaxPageSize() {
		return maxPageSize;
	}
	
	@Override public int getDefaultPageSize() {
		return defaultPageSize;
	}
	
	// may be subclassed
    protected void instantiateBaseQuery( Resource endpoint ) {
        baseQuery = new APIQuery( this );
        baseQuery.setEnableETags( enableETags( endpoint ) );
        setAllowedReserved( endpoint, baseQuery );
        addSelectors(endpoint);
    }

    // may be subclassed
    protected boolean enableETags( Resource ep ) {
		Statement s = ep.getProperty(EXTRAS.enableETags);
		if (s == null) s = specForEndpoint(ep).getProperty(EXTRAS.enableETags);
		return s != null && s.getBoolean();
	}

    protected void setAllowedReserved( Resource endpoint, APIQuery q ) {
		setAllowedReservedFrom( endpoint, q );
		setAllowedReservedFrom( specForEndpoint( endpoint ), q );
	}

	private Resource specForEndpoint(Resource endpoint) {
		return endpoint.getModel().listStatements( null, API.endpoint, endpoint ).next().getSubject();
	}

	private void setAllowedReservedFrom( Resource r, APIQuery q ) {
		for (Statement s: r.listProperties( EXTRAS.allowReserved ).toSet()) {
			q.addAllowReserved( s.getString() );
		}
	}

	// may be subclassed
	protected void addSelectors(Resource endpoint) {
		Resource s = getResourceValue( endpoint, API.selector );
        if (s != null) {
	        StmtIterator i = s.listProperties( API.parent );
	        while (i.hasNext()) {
	            RDFNode parentN = i.next().getObject();
	            if (parentN instanceof Resource) {
	                addSelectorInfo( (Resource)parentN );
	            } else {
	                APISpec.log.error("Parent view must be a resource, found a literal: " + parentN);
	            }
	        }
	        addSelectorInfo(s);
        }
	}
    
	private void addSelectorInfo( Resource s ) {
        Model m = s.getModel();
        ShortnameService sns = this.apiSpec.sns;
        if (s.hasProperty(API.type)) {
			Resource ty = sns.asResource( s.getProperty(API.type).getObject() );
            baseQuery.setTypeConstraint( ty );
        }
        for (NodeIterator ni = m.listObjectsOfProperty(s, API.filter); ni.hasNext();) {
            String q = getLexicalForm( ni.next() );
            for (String query : q.split("[&]")) { 
	            String[] paramValue = query.split("=");
	            if (paramValue.length == 2) {
	                baseQuery.deferrableAddFilter( Param.make( sns, paramValue[0] ), paramValue[1] );
	            } else {
	                APISpec.log.error("Filter specification contained unintepretable query string: " + q );
	            }
            }
        }
        for (Statement where: s.listProperties( API.where ).toList()) {
        	baseQuery.addWhere( where.getString() );        	
        }
        if (s.hasProperty(API.orderBy)) {
            baseQuery.setOrderBy( getStringValue( s, API.orderBy ) );
        }
        if (s.hasProperty(API.sort)) {
            baseQuery.setSortBy( getStringValue( s, API.sort ) );
        }
        if (s.hasProperty( API.select)) {
        	baseQuery.setFixedSelect( getStringValue( s, API.select ) );
        }
    }
    
    /**
     * Return a base query for this endpoint. This query object will
     * be a clone of an internal one and so can be freely updated
     * with call-specific information to build the query
     */
    public APIQuery getBaseQuery() {
        return baseQuery.copy();
    }
    
    /** 
     * Return the overall APISpec of which this instance is a part.
     */
    public APISpec getAPISpec() {
        return parentApi;
    }
    
    /**
     * The default language for encoding plain literals (null if no default).
     */
    @Override public String getDefaultLanguage() {
    	return defaultLanguage;
    }

    /**
     * Return the URI template at which this instance should
     * be located
     */
    public String getURITemplate() {
        return uriTemplate;
    }
    
    /**
        Printable summary for debugging
    */
    @Override public String toString() {
        return "[Endpoint " + name + " for " + this.apiSpec.specificationURI + "]";
    }

    /**
        Return the view template definition for the given named view
    */
    @Override public View getView(String viewname) {
        return views.get(viewname);
    }

    /**
        Return the default view template definition, if any
    */
    @Override public View getDefaultView() {
        return views.get( View.SHOW_DEFAULT_INTERNAL );
    }

    /**
        Return the variable bindings of this endpoint. Never
        null, but of course the bindings may be empty.
    */
	public Bindings getBindings() {
		return bindings;
	}

	/**
	    Return the item template of this endpoint, or null if there
	    isn't one (eg it's a list endpoint).
	*/
	@Override public String getItemTemplate() {
		return itemTemplate;
	}
	
	/**
	    Return the graph template string associated with this endpoint,
	    or null if none was specified.
	*/
	public String getGraphTemplate() {
		return graphTemplate;
	}
	
	public Factories getRendererFactoryTable() {
		return factoryTable;
	}

	/**
	    Return (a copy of) the set of names of views in this
	    EndpointSpec.
	*/
	public Set<String> viewNames() {
		return new HashSet<String>( views.keySet() );
	}

	/**
	    Return the specification URI for this Endpoint, which is
	    the specification URI for its parent APISpec.
	*/
	public String getSpecificationURI() {
		return getAPISpec().specificationURI;
	}

	@Override public boolean hasParameterBasedContentNegotiation() {
		return getAPISpec().hasParameterBasedContentNegotiation();
	}

	@Override public TextSearchConfig getTextSearchConfig() {
		return textSearchConfig;
	}

	public Boolean getEnableCounting() {
		return enableCounting;
	}

	public long getCacheExpiryMilliseconds() {
		return cacheExpiryMilliseconds;
	}
	
}