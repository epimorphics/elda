/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$

    File:        APISpec.java
    Created by:  Dave Reynolds
    Created on:  31 Jan 2010
 */

package com.epimorphics.lda.specs;
import static com.epimorphics.util.RDFUtils.getStringValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.bindings.Lookup;
import com.epimorphics.lda.bindings.MapLookup;
import com.epimorphics.lda.bindings.VariableExtractor;
import com.epimorphics.lda.core.ModelLoader;
import com.epimorphics.lda.exceptions.APIException;
import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.query.APIQuery;
import com.epimorphics.lda.query.QueryParameter;
import com.epimorphics.lda.rdfq.Value;
import com.epimorphics.lda.renderers.Factories;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.shortnames.StandardShortnameService;
import com.epimorphics.lda.sources.*;
import com.epimorphics.lda.sources.Source.ResultSetConsumer;
import com.epimorphics.lda.support.ModelPrefixEditor;
import com.epimorphics.lda.support.PrefixLogger;
import com.epimorphics.lda.support.RendererFactoriesSpec;
import com.epimorphics.lda.textsearch.TextSearchConfig;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.lda.vocabularies.ELDA_API;
import com.epimorphics.util.RDFUtils;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.util.FileManager;
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
public class APISpec extends SpecCommon {

    static Logger log = LoggerFactory.getLogger(APISpec.class);
    
    protected final List<APIEndpointSpec> endpoints = new ArrayList<APIEndpointSpec>();
    
    protected final PrefixMapping prefixes;
    protected final ShortnameService sns;
    
    protected final Source dataSource;
    protected final String primaryTopic;
    protected final String specificationURI;
    protected final String defaultLanguage;
    protected final String base;
    
    protected final TextSearchConfig textSearchConfig;
    
    public final int defaultPageSize;
    public final int maxPageSize;
    
    protected final Factories factoryTable;
    protected final boolean hasParameterBasedContentNegotiation;
    protected final List<Source> describeSources;
    public final Bindings bindings;
    
    protected final MapLookup mapLookup;
    
    protected final String prefixPath;
	
	public final String cachePolicyName;
	
	protected final ModelPrefixEditor modelPrefixEditor = new ModelPrefixEditor();
	
	protected final Boolean enableCounting;
	
	protected final long cacheExpiryMilliseconds;
	
	protected final PropertyExpiryTimes propertyExpiryTimes;
	
	protected final String graphTemplate;

	protected final boolean purging;
    
	public APISpec( FileManager fm, Resource specification, ModelLoader loader ) {
		this( "", fm, specification, loader );
	}

    public APISpec( String prefixPath, FileManager fm, Resource specification, ModelLoader loader ) {
    	this(prefixPath, "APP", fm, specification, loader ); 
    }
    
    public APISpec( String prefixPath, String appName, FileManager fm, Resource root, ModelLoader loader ) {
    	super(root);
    	AuthMap am = loadAuthMap(root, appName);
    	reportObsoleteDescribeThreshold(root);
    	this.purging = RDFUtils.getBooleanValue(root, ELDA_API.purgeFilterValues, false);
    	this.prefixPath = prefixPath;
    	this.specificationURI = root.getURI();
    	this.defaultPageSize = RDFUtils.getIntValue( root, API.defaultPageSize, QueryParameter.DEFAULT_PAGE_SIZE );
		this.maxPageSize = RDFUtils.getIntValue( root, API.maxPageSize, QueryParameter.MAX_PAGE_SIZE );
		this.prefixes = ExtractPrefixMapping.from(root);
        this.sns = loadShortnames(root, loader);
        this.dataSource = GetDataSource.sourceFromSpec( fm, root, am );
        this.textSearchConfig = dataSource.getTextSearchConfig().overlay(root);
        this.describeSources = extractDescribeSources( fm, am, root, dataSource );
        this.primaryTopic = getStringValue(root, FOAF.primaryTopic, null);
        this.graphTemplate = getStringValue(root, ELDA_API.graphTemplate, null);
        this.defaultLanguage = getStringValue(root, API.lang, null);
        this.base = getStringValue( root, API.base, null );
        this.mapLookup = createMapLookup(root, this.dataSource);
        this.bindings = new Bindings(mapLookup);
        VariableExtractor.findAndBindVariables(this.bindings, root);
        this.factoryTable = RendererFactoriesSpec.createFactoryTable( root );
        this.hasParameterBasedContentNegotiation = root.hasProperty( API.contentNegotiation, API.parameterBased ); 
		this.cachePolicyName = getStringValue( root, ELDA_API.cachePolicyName, "default" );
		this.cacheExpiryMilliseconds = PropertyExpiryTimes.getSecondsValue( root, ELDA_API.cacheExpiryTime, -1) * 1000;
        this.enableCounting = RDFUtils.getOptionalBooleanValue( root, ELDA_API.enableCounting, Boolean.FALSE );        
		this.propertyExpiryTimes = PropertyExpiryTimes.assemble( root.getModel() );
	//
		setDefaultSuffixName(bindings, root);      
		extractEndpointSpecifications( root );
        extractModelPrefixEditor( root );
        }
    
	public static MapLookup createMapLookup(Resource root, final Source ds) {
		
		final Map<String, String> maps = new HashMap<String, String>();
		
		// System.err.println(">> creating map lookup");
		
		for (Statement decl: root.listProperties(ELDA_API.sparqlMap).toList()) {
			// System.err.println(">> ... another one: " + decl);
			Resource map = decl.getResource();
			String mapName = map.getURI(); 
			String queryString = getStringValue(map,ELDA_API.mapQuery);
			maps.put(mapName, queryString);
		}
		
		return new MapLookup() {
			
			@Override public String toString() {
				return "SourceMap";
			}
			
			@Override public String getValueString(String mapName, Bindings b, Lookup expander) {
				
//				System.err.println(">> getValueString(" + mapName + ")");
				
				String configuredQuery = queryExpand(maps.get(mapName), b);
//				System.err.println(">> configured query: " + configuredQuery);
				
				String expandedQuery = expander.getValueString(configuredQuery);
//				System.err.println(">> expandedQuery:    " + expandedQuery);
				
				String bracedQueryString = expandedQuery.replace("((", "{").replace("))", "}");
//				System.err.println(">> bracedQuery:      " + bracedQueryString);
				
				String [] result = new String[] {""};
				
				ResultSetConsumer rsc = new ResultSetConsumer() {
					
					@Override public void setup(QueryExecution qe) {						
					}

					@Override public void consume(ResultSet rs) {
						while (rs.hasNext()) {
							QuerySolution qs = rs.next();
							result[0] = qs.get("x").toString();
						}
						
					}
					
				};			

				Query query = QueryFactory.create(bracedQueryString);
				ds.executeSelect(query, rsc);
				
				return result[0];
			}

			private String queryExpand(String q, Bindings b) {
				Matcher m = APIQuery.varPattern.matcher(q);
				StringBuilder sb = new StringBuilder();
				PrefixLogger pl = new PrefixLogger();
				int start = 0;
				
				while (m.find(start)) {
					String leader = q.substring(start,  m.start());
					sb.append(leader);
					
					String name = m.group().substring(1);
					
					Value v = b.get(name); 
					if (v == null || v.spelling().equals("")) {
						sb.append(m.group());
					} else {
						String term = v.asSparqlTerm(pl);
						sb.append(term);
					}
					start = m.end();
				}
				
				sb.append(q.substring(start));
				return sb.toString();
			}
			
		};
	}

	public static void setDefaultSuffixName(Bindings b, Resource ep) {
		if (ep.hasProperty( API.defaultFormatter)) {
			Resource r = ep.getProperty( API.defaultFormatter ).getObject().asResource();
			if (r.hasProperty( API.name )) {
				String name = r.getProperty( API.name ).getString();
				b.put("_defaultSuffix", name);
			} 
		}
	}

	protected void reportObsoleteDescribeThreshold(Resource endpoint) {
		if (endpoint.hasProperty(ELDA_API.describeThreshold)) {
			log.warn("endpoint '{}': elda:describeThreshold is no longer required/used.", endpoint);
		}
	}
    
	private AuthMap loadAuthMap(Resource root, String appName) {
		Resource endpoint = RDFUtils.getResourceValue(root, ELDA_API.sparqlEndpoint);
		if (endpoint == null) {
			throw new EldaException("no SPARQL endpoint specified for " + root);
		}
		StmtIterator maps = endpoint.listProperties(ELDA_API.authFile);
		if (maps.hasNext()) {
			AuthMap am = new AuthMap();
			while (maps.hasNext()) {
				Statement s = maps.nextStatement();
				String fileNames = s.getString();
				AuthMap.readAuthMapFromPaths(am, appName, fileNames);
			}
			return am;
		}	
		return AuthMap.loadAuthMapFromPaths(appName, AuthMap.USUAL_AUTH_PATHS);
	}

	private void extractModelPrefixEditor(Resource specification) {
		StmtIterator eps = specification.listProperties( ELDA_API.rewriteResultURIs );
		while (eps.hasNext()) extractSingleModelprefixFromTo( eps.next() );
	}

	private void extractSingleModelprefixFromTo( Statement s ) {
		Resource S = s.getSubject();
		if (s.getObject().isLiteral())
			throw new EldaException( "Object of editPrefix property of " + S + " is a literal." );
		Resource edit = s.getResource();
		String from = getStringValue( edit, ELDA_API.ifStarts );
		if (from == null) throw new EldaException( "Missing from for " + S );
		String to = getStringValue( edit, ELDA_API.replaceStartBy );
		if (to == null) throw new EldaException( "Missing elda:to for " + S );
		modelPrefixEditor.set(from, to);
	}

	private StandardShortnameService loadShortnames( Resource specification, ModelLoader loader ) {
		return new StandardShortnameService(specification, prefixes, loader);
	}

	/**
        Answer the list of sources that may be used to enhance the view of
        the selected items. Always contains at least the given source.
    */
    private List<Source> extractDescribeSources( FileManager fm, AuthMap am, Resource specification, Source dataSource ) {
        List<Source> result = new ArrayList<Source>();
        result.add( dataSource );
        result.addAll( specification.listProperties( ELDA_API.enhanceViewWith ).mapWith( toSource( fm, am ) ).toList() ); 
        return result;
    }

    private static final Map1<Statement, Source> toSource( final FileManager fm, final AuthMap am ) {
    	return new Map1<Statement, Source>() {
    		@Override public Source map1( Statement o ) { 
    			return GetDataSource.sourceFromSpec( fm, o.getResource(), am ); 
    		}
    	};
    };
    
    private void extractEndpointSpecifications( Resource specification ) {
        NodeIterator ni = specification.getModel().listObjectsOfProperty(specification, API.endpoint);
        while (ni.hasNext() ) {
            RDFNode n = ni.next();
            if ( ! (n instanceof Resource)) {
                throw new APIException("Bad specification file, non-resource definition of Endpoint. " + n);
            }
            Resource endpoint = (Resource) n;
            // endpoints.add( new APIEndpointSpec( this, this, endpoint ) );
            endpoints.add( getAPIEndpointSpec( endpoint) );
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
        Return list of individual instances which make up this API.
    */
    public List<APIEndpointSpec> getEndpoints() {
        return endpoints;
    }
    
    /**
        Return the data source (remote or local) which this 
        API wraps.
    */
    public Source getDataSource() {
        return dataSource;
    }
    
    /**
        Return the primary topic of this list/set, or null if none is specified
    */
    public String getPrimaryTopic() {
        return primaryTopic;
    }
    
    /**
        The URI for the RDF resource which specifies this API 
    */
    public String getSpecURI() {
        return specificationURI;
    }
    
    /**
        The default language for encoding plain literals (null if no default).
    */
    public String getDefaultLanguage() {
    	return defaultLanguage;
    }
    
    /**
        Printable representation for debugging
    */
    @Override public String toString() {
        return "API-" + specificationURI;
    }
    
    public List<Source> getDescribeSources() {
        return describeSources;
    }

    /**
        Answer the bindings of variables for this API configuration.
        Never null, but may be empty.
    */
	public Bindings getBindings() {
		return bindings;
	}

	/**
	    Answer the value of api:base for this configuration, or null if
	    no api:base was provided.
	*/
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
	
	public String getCachePolicyName() {
		return cachePolicyName;
	}
	
	/**
		Returns a new APIEndpointSpec for this APISpec and the given endpoint
	*/
	protected APIEndpointSpec getAPIEndpointSpec( Resource endpoint ) {
		return new APIEndpointSpec ( this, this, endpoint );
	}
	
	public boolean hasParameterBasedContentNegotiation() {
		return hasParameterBasedContentNegotiation;
	}

	public ModelPrefixEditor getModelPrefixEditor() {
		return modelPrefixEditor;
	}

	public TextSearchConfig getTextSearchConfig() {
		return textSearchConfig;
	}
	
	public String getPrefixPath() {
		return prefixPath;
	}

	public long getCacheExpiryMilliseconds() {
		return cacheExpiryMilliseconds;
	}

	public Boolean getEnableCounting() {
		return enableCounting;
	}

	public PropertyExpiryTimes getPropertyExpiryTimes() {
		return propertyExpiryTimes;
	}

	public String getGraphTemplate() {
		return graphTemplate;
	}
}

