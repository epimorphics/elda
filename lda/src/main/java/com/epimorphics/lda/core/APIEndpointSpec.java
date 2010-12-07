/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) copyright Epimorphics Limited 2010
    $Id$
*/

package com.epimorphics.lda.core;

import static com.epimorphics.util.RDFUtils.*;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.core.APIQuery.Param;
import com.epimorphics.lda.shortnames.ShortnameService;
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
	
	protected String uriTemplate ;
    protected APISpec parentApi;
    protected String name;
//        protected String endpointURI;
    protected Resource endpointResource;
    protected APIQuery baseQuery;
    protected Map<String, View> views;
    protected String defaultLanguage;
    
    protected String fixedSelect;
    protected String fixedWhere;  
    protected String itemTemplate;
    
    public final int defaultPageSize;
    public final int maxPageSize;

    protected final Map<String, RDFNode> bindings = new HashMap<String, RDFNode>();
    
    static Logger log = LoggerFactory.getLogger(APIEndpointSpec.class);
    
    public APIEndpointSpec( APISpec apiSpec, APISpec parent, Resource endpoint ) {
    	checkEndpointType( endpoint );
    	this.apiSpec = apiSpec;
    	bindings.putAll( apiSpec.bindings );
        bindings.putAll( VariableExtractor.findAndBindVariables( bindings, endpoint ) );
        fixedSelect = getStringValue( endpoint, API.select, null );
        fixedWhere = getStringValue( endpoint, API.where, null );
        defaultLanguage = getStringValue(endpoint, FIXUP.language, apiSpec.getDefaultLanguage());
    	defaultPageSize = getIntValue( endpoint, API.defaultPageSize, apiSpec.defaultPageSize );
		maxPageSize = getIntValue( endpoint, API.maxPageSize, apiSpec.maxPageSize );
		parentApi = parent;
        name = endpoint.getLocalName();
        uriTemplate = getStringValue(endpoint, API.uriTemplate, null);
        itemTemplate = getStringValue( endpoint, API.itemTemplate, null );
        if (uriTemplate == null) throw new APIEndpointException("No deployment uri for Endpoint " + name );
        if (!uriTemplate.startsWith("/") && !uriTemplate.startsWith("http")) uriTemplate = "/" + uriTemplate;
        endpointResource = endpoint;
        instantiateBaseQuery(endpoint);
        views = extractViews(endpoint);
    }
    
    private void checkEndpointType(Resource endpoint) {
    	boolean isList = endpoint.hasProperty( RDF.type, API.ListEndpoint );
    	boolean isItem = endpoint.hasProperty( RDF.type, API.ItemEndpoint );
    	if (isList || isItem) return;
    	log.warn( "endpoint " + endpoint + " is not declared as ListEndpoint or ItemEndpoint -- unexpected behaviour may result." );
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
        result.put( View.SHOW_DESCRIPTION, View.DESCRIBE );
        View dv = getDefaultView( endpoint );
//        System.err.println( ">> default view for " + endpoint + " is " + dv );
		result.put( View.SHOW_DEFAULT_INTERNAL, dv );
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
    	} else {
	        View v = new View(false);
			extractView( v, m.listObjectsOfProperty(tRes, API.properties ) );
			extractView( v, m.listObjectsOfProperty(tRes, API.property ) );
	        return v;
    	}
    }

	private void extractView(View v, NodeIterator items) {
		for (NodeIterator nii = items; nii.hasNext();) {
            RDFNode pNode = nii.next();
            if (pNode.isResource()) {
                v.addViewFromRDFList((Resource)pNode, this.apiSpec.sns);
            } else if (pNode.isLiteral()) {
            	for(String dotted : pNode.asNode().getLiteralLexicalForm().split(" *, *")) {
					ShortnameService sns = apiSpec.getShortnameService();
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
	
    private void instantiateBaseQuery(Resource endpoint) {
        baseQuery = new APIQuery( this );
        Resource view = getResourceValue(endpoint, API.selector);
        if (view == null) return;  // Just default view
        StmtIterator i = view.listProperties(API.parent);
        while (i.hasNext()) {
            RDFNode parentN = i.next().getObject();
            if (parentN instanceof Resource) {
                addView( (Resource)parentN );
            } else {
                APISpec.log.error("Parent view must be a resource, found a literal: " + parentN);
            }
        }
        addView(view);
    }

    private void addView(Resource view) {
        Model m = view.getModel();
        if (view.hasProperty(FIXUP.type)) {
            Resource ty = this.apiSpec.sns.normalizeResource( view.getProperty(FIXUP.type).getObject() );
            baseQuery.setTypeConstraint( ty );
        }
        for (NodeIterator ni = m.listObjectsOfProperty(view, API.filter); ni.hasNext();) {
            String q = getLexicalForm( ni.next() );
            for (String query : q.split("[,&]")) { // TODO -- remove this compatability HACK
	            String[] paramValue = query.split("=");
	            if (paramValue.length == 2) {
	                baseQuery.deferrableAddFilter( Param.make( paramValue[0] ), paramValue[1] );
	            } else {
	                APISpec.log.error("View specification contained unintepretable query string: " + q);
	            }
            }
        }
        if (view.hasProperty(API.where)) {
            String where = getStringValue(view, API.where);
            baseQuery.addWhere(where);
        }
        if (view.hasProperty(API.orderBy)) {
            String orderSpec = getStringValue(view, API.orderBy);
            baseQuery.setDefaultOrdering(orderSpec);
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
    public String getDefaultLanguage() {
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
    public String toString() {
        return "[Endpoint " + getURITemplate() + " on API " + this.apiSpec.specificationURI + "]";
    }

    /**
     * Return the view template definition for the given named view
     */
    public View getView(String viewname) {
        return views.get(viewname);
    }

    /**
     * Return the default view template definition, if any
     */
    public View getDefaultView() {
        return views.get( View.SHOW_DEFAULT_INTERNAL );
    }

	public Map<String, RDFNode> getBindings() {
		return bindings;
	}

	/**
	    Answer the variable bindings name->RDFNode of this EndpointSpec
	    converted to the same form as used by parameters, using the
	    short name service.
	    
	    WARNING: under development. chris doesn't fully understand what the
	    mapping is supposed to be: not convinced that the SNS conversion is
	    enough. Typed literals are a concern.
	*/
	public Map<String, String> getParameterBindings() {
		ShortnameService sns = apiSpec.getShortnameService();
		Map<String, String> result = new HashMap<String, String>();
		for (Map.Entry<String, RDFNode> e: bindings.entrySet()) {
			result.put( e.getKey(), sns.shorten( sns.normalizeResource( e.getValue() ).getURI() ) );
		}
		return result;
	}

	/**
	    Answer the fixed select string provided by api:select, or null if there
	    wasn't one.
	*/
	public String getFixedSelect() {
		return fixedSelect;
	}

	public String getWhere() {
		return fixedWhere;
	}

	public String getItemTemplate() {
		return itemTemplate;
	}

}