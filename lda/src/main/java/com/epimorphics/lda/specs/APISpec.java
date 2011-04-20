/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$

    File:        APISpec.java
    Created by:  Dave Reynolds
    Created on:  31 Jan 2010
 */

package com.epimorphics.lda.specs;
import static com.epimorphics.util.RDFUtils.*;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.bindings.VarValues;
import com.epimorphics.lda.bindings.VariableExtractor;
import com.epimorphics.lda.core.APIException;
import com.epimorphics.lda.core.ModelLoaderI;
import com.epimorphics.lda.core.QueryParameter;
import com.epimorphics.lda.renderers.Factories;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.shortnames.StandardShortnameService;
import com.epimorphics.lda.sources.GetDataSource;
import com.epimorphics.lda.sources.Source;
import com.epimorphics.lda.support.RendererFactoriesSpec;
import com.epimorphics.lda.vocabularies.EXTRAS;
import com.epimorphics.util.RDFUtils;
import com.epimorphics.vocabs.API;
import com.epimorphics.vocabs.FIXUP;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.util.iterator.Map1;

/**
 * Encapsulates a specification of a single API instance.
 * API specification is transported via RDF but this object state
 * is self contained to make it easier to migrate to GAE-JDO 
 * storage for persisting specs.
 * 
 * @author <a href="mailto:der@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public class APISpec {

    static Logger log = LoggerFactory.getLogger(APISpec.class);
    
    protected List<APIEndpointSpec> endpoints = new ArrayList<APIEndpointSpec>();
    
    protected final PrefixMapping prefixes;
    protected final ShortnameService sns;
    
    protected final Source dataSource;
    protected final String primaryTopic;
    protected final String specificationURI;
    protected final String defaultLanguage;
    protected final String base;
    
    public final int defaultPageSize;
    public final int maxPageSize;
    
    protected final Factories factoryTable;
    protected final boolean hasParameterBasedContentNegotiation;
    protected final List<Source> describeSources;
    protected final VarValues bindings = new VarValues();
    
    public APISpec(Resource specification, ModelLoaderI loader) {
    	specificationURI = specification.getURI();
    	defaultPageSize = RDFUtils.getIntValue( specification, API.defaultPageSize, QueryParameter.DEFAULT_PAGE_SIZE );
		maxPageSize = RDFUtils.getIntValue( specification, API.maxPageSize, QueryParameter.MAX_PAGE_SIZE );
        prefixes = ExtractPrefixMapping.from(specification);
        sns = new StandardShortnameService(specification, prefixes, loader);
        dataSource = GetDataSource.sourceFromSpec( specification );
        describeSources = extractDescribeSources( specification, dataSource );
        primaryTopic = getStringValue(specification, FOAF.primaryTopic, null);
        defaultLanguage = getStringValue(specification, FIXUP.lang, null);
        base = getStringValue( specification, API.base, null );
        bindings.putAll( VariableExtractor.findAndBindVariables(specification) );
        factoryTable = RendererFactoriesSpec.createFactoryTable( specification );
        hasParameterBasedContentNegotiation = specification.hasProperty( API.contentNegotiation, API.parameterBased ); 
        extractEndpointSpecifications( specification );
    }

	/**
        Answer the list of sources that may be used to enhance the view of
        the selected items. Always contains at least the given source.
    */
    private List<Source> extractDescribeSources( Resource specification, Source dataSource ) {
//        System.err.println( ">> extracting enhancements from " + specification );
        List<Source> result = new ArrayList<Source>();
        result.add( dataSource );
        result.addAll( specification.listProperties( EXTRAS.enhanceViewWith ).mapWith( toSource ).toList() ); 
//        System.err.println( ">> describe sources: " + result );
        return result;
    }

    private static final Map1<Statement, Source> toSource = new Map1<Statement, Source>() {
        @Override public Source map1( Statement o ) { 
            return GetDataSource.sourceFromSpec( o.getResource() ); 
        }
    };
    
    private void extractEndpointSpecifications( Resource specification ) {
        NodeIterator ni = specification.getModel().listObjectsOfProperty(specification, API.endpoint);
        while (ni.hasNext() ) {
            RDFNode n = ni.next();
            if ( ! (n instanceof Resource)) {
                throw new APIException("Bad specification file, non-resource definition of Endpoint. " + n);
            }
            Resource endpoint = (Resource) n;
            endpoints.add( new APIEndpointSpec( this, this, endpoint ) );
        }
    }
    
    /**
     * Return the prefix mapping, applies to whole API
     */
    public PrefixMapping getPrefixMap() {
        return prefixes;
    }
    
    /**
     * Return a utility for mapping names to short names as
     * configured for this API. 
     */
    public ShortnameService getShortnameService() {
        return sns;
    }
    
    /**
     * Return list of individual instances which make up this API.
     * An 
     */
    public List<APIEndpointSpec> getEndpoints() {
        return endpoints;
    }
    
    /**
     * Return the data source (remote or local) which this 
     * API wraps.
     */
    public Source getDataSource() {
        return dataSource;
    }
    
    /**
     * Return the primary topic of this list/set, or null if none is specified
     */
    public String getPrimaryTopic() {
        return primaryTopic;
    }
    
    /**
     * The URI for the RDF resource which specifies this API 
     */
    public String getSpecURI() {
        return specificationURI;
    }
    
    /**
     * The default language for encoding plain literals (null if no default).
     */
    public String getDefaultLanguage() {
    	return defaultLanguage;
    }
    
    /**
     * Printable representation for debugging
     */
    @Override public String toString() {
        return "API-" + specificationURI;
    }
    
    public List<Source> getDescribeSources() {
        return describeSources;
    }

	public VarValues getBindings() {
		return bindings;
	}

	public String getBase() {
		return base;
	}
	
	/**
	    Answer a copy of the renderer factory map. (It's a copy so
	    that the caller can freely mutate it afterwards.)
	*/
	public Factories getRendererFactoryTable() {
		return factoryTable.copy();
	}
}

