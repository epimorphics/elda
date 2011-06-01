/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.specs;

import static com.epimorphics.util.RDFUtils.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.bindings.VarValues;
import com.epimorphics.lda.bindings.VariableExtractor;
import com.epimorphics.lda.core.APIException;
import com.epimorphics.lda.core.APIQuery;
import com.epimorphics.lda.core.NamedViews;
import com.epimorphics.lda.core.Param;
import com.epimorphics.lda.core.View;
import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.renderers.Factories;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.support.RendererFactoriesSpec;
import com.epimorphics.lda.vocabularies.EXTRAS;
import com.epimorphics.vocabs.API;
import com.epimorphics.vocabs.FIXUP;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Encapsulates the specification of the particular List/Set within
 * a particular API. 
 */
public class APIEndpointSpec implements NamedViews, APIQuery.QueryBasis {

	private final APISpec apiSpec;
	
    protected final APISpec parentApi;
    protected final String name;
    protected final Resource endpointResource;
    
    protected APIQuery baseQuery;
    protected String uriTemplate ;
    
    protected final Map<String, View> views;
    protected final String defaultLanguage;
    
    protected final String itemTemplate;
    
    protected final boolean wantsContext;
    
    protected final String cachePolicyName;
    
    public final int defaultPageSize;
    public final int maxPageSize;

    protected final VarValues bindings = new VarValues();
    
    protected final Factories factoryTable;
    
    static Logger log = LoggerFactory.getLogger(APIEndpointSpec.class);
    
    protected final Set<String> metadataOptions = new HashSet<String>();
    
    public APIEndpointSpec( APISpec apiSpec, APISpec parent, Resource endpoint ) {
    	checkEndpointType( endpoint );
    	this.apiSpec = apiSpec;
    	wantsContext = endpoint.hasLiteral( EXTRAS.wantsContext, true );
    	bindings.putAll( apiSpec.bindings );
        bindings.putAll( VariableExtractor.findAndBindVariables( bindings, endpoint ) );
        defaultLanguage = getStringValue(endpoint, FIXUP.lang, apiSpec.getDefaultLanguage());
    	defaultPageSize = getIntValue( endpoint, API.defaultPageSize, apiSpec.defaultPageSize );
		maxPageSize = getIntValue( endpoint, API.maxPageSize, apiSpec.maxPageSize );
		cachePolicyName = getStringValue( endpoint, EXTRAS.cachePolicyName, "default" );
		parentApi = parent;
        name = endpoint.getLocalName();
        uriTemplate = getStringValue(endpoint, API.uriTemplate, null);
        itemTemplate = getStringValue( endpoint, API.itemTemplate, null );
        if (uriTemplate == null) EldaException.NoDeploymentURIFor( name ); 
        if (!uriTemplate.startsWith("/") && !uriTemplate.startsWith("http")) uriTemplate = "/" + uriTemplate;
        endpointResource = endpoint;
        extractMetadataOptions( endpoint );
        instantiateBaseQuery( endpoint ); 
        views = extractViews( endpoint );
        factoryTable = RendererFactoriesSpec.createFactoryTable( endpoint, apiSpec.getRendererFactoryTable() );
    }
    
    private void extractMetadataOptions(Resource endpoint) {
    	metadataOptions.addAll( apiSpec.metadataOptions );
    	for (StmtIterator it = endpoint.listProperties( EXTRAS.metadataOptions ); it.hasNext();)
    		for (String option: it.next().getString().split(",")) 
    			metadataOptions.add( option.toLowerCase() );
    }

	public boolean isListEndpoint() {
    	return endpointResource.hasProperty( RDF.type, API.ListEndpoint );
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
    	Model m = endpoint.getModel();
        Map<String, View> result = new HashMap<String, View>(); 
        for (NodeIterator ni =  m.listObjectsOfProperty( endpoint, API.viewer ); ni.hasNext();) {
            RDFNode tNode = ni.next();
            if ( ! tNode.isResource()) 
                throw new APIException("Found literal " + tNode + " when expecting a template resource");
            Resource tView = (Resource) tNode;
            String viewName = getNameWithFallback( tView );
			result.put( viewName, extractView( m, tView ) );
        }
        result.put( View.SHOW_ALL, View.ALL );
        result.put( View.SHOW_BASIC, View.BASIC );
        result.put( View.SHOW_DESCRIPTION, View.DESCRIBE );
        result.put( View.SHOW_DEFAULT_INTERNAL, getDefaultView( endpoint ) );
        return result;
    }

	private String getNameWithFallback(Resource tRes) {
		String s = getStringValue( tRes, API.name );
		return s == null ? getNameFor(tRes) : s;
	}
    
    private View getDefaultView( Resource endpoint ) {
        Model model = endpoint.getModel();
		return endpoint.hasProperty( API.defaultViewer )
        	? extractView( model, getResourceValue( endpoint, API.defaultViewer ) )
        	: View.DESCRIBE
        	;
    }

    /**
        both API.property and .properties until TODO the ambiguity gets resolved.
    */
    private View extractView( Model m, Resource tRes ) {
    	if (tRes.equals( API.describeViewer )) {
    		return View.DESCRIBE;
    	} else if (tRes.equals( API.labelledDescribeViewer )) {
    		return View.ALL;
    	} else if (tRes.equals( API.basicViewer )){
    		return View.BASIC;
    	} else {
	        View v = new View(false);
			addViewProperties( v, m.listObjectsOfProperty( tRes, API.properties ).toList() );
			addViewProperties( v, m.listObjectsOfProperty( tRes, API.property ).toList() );
	        return v;
    	}
    }

	private void addViewProperties( View v, List<RDFNode> items ) {
		ShortnameService sns = apiSpec.getShortnameService();
		for (RDFNode pNode: items) {
            if (pNode.isResource()) {
                v.addViewFromRDFList((Resource)pNode, sns);
            } else if (pNode.isLiteral()) {
            	for(String dotted : pNode.asNode().getLiteralLexicalForm().split(" *, *")) {
					v.addViewFromParameterValue(dotted, baseQuery, sns);
	        	}
	        }
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
	
    private void instantiateBaseQuery( Resource endpoint ) {
        baseQuery = new APIQuery( this );
        baseQuery.addMetadataOptions( metadataOptions );
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
        if (s.hasProperty(FIXUP.type)) {
			Resource ty = sns.normalizeResource( s.getProperty(FIXUP.type).getObject() );
            baseQuery.setTypeConstraint( ty );
        }
        for (NodeIterator ni = m.listObjectsOfProperty(s, API.filter); ni.hasNext();) {
            String q = getLexicalForm( ni.next() );
            for (String query : q.split("[,&]")) { // TODO -- remove this compatability HACK
	            String[] paramValue = query.split("=");
	            if (paramValue.length == 2) {
	                baseQuery.deferrableAddFilter( Param.make( sns, paramValue[0] ), paramValue[1] );
	            } else {
	                APISpec.log.error("View specification contained unintepretable query string: " + q);
	            }
            }
        }
        if (s.hasProperty(API.where)) {
            String where = getStringValue(s, API.where);
            baseQuery.addWhere(where);
        }
        if (s.hasProperty(API.orderBy)) {
            baseQuery.setExplicitOrderBy( getStringValue( s, API.orderBy ) );
        }
        if (s.hasProperty(API.sort)) {
            baseQuery.setOrderBy( getStringValue( s, API.sort ) );
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
        return baseQuery.clone();
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
     * Printable summary for debugging
     */
    @Override public String toString() {
        return "[Endpoint " + name + " for " + this.apiSpec.specificationURI + "]";
    }

    /**
     * Return the view template definition for the given named view
     */
    @Override public View getView(String viewname) {
        return views.get(viewname);
    }

    /**
     * Return the default view template definition, if any
     */
    @Override public View getDefaultView() {
        return views.get( View.SHOW_DEFAULT_INTERNAL );
    }

	public VarValues getBindings() {
		return bindings;
	}

	@Override public String getItemTemplate() {
		return itemTemplate;
	}
	
	public Factories getRendererFactoryTable() {
		return factoryTable;
	}

	/**
	    Answer (a copy of) the set of names of views in this
	    EndpointSpec.
	*/
	public Set<String> viewNames() {
		return new HashSet<String>( views.keySet() );
	}

	/**
	    Answer the specification URI for this Endpoint, which is
	    the specification URI for its parent APISpec.
	*/
	public String getSpecificationURI() {
		return getAPISpec().specificationURI;
	}
	
}