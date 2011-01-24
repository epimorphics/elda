/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$

    File:        APIQuery.java
    Created by:  Dave Reynolds
    Created on:  31 Jan 2010
*/

package com.epimorphics.lda.core;

import java.util.*;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.cache.Cache;
import com.epimorphics.lda.rdfq.*;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.sources.Source;
import com.epimorphics.lda.support.LARQManager;
import com.epimorphics.util.RDFUtils;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Query abstraction that supports assembling multiple filter/order/view
 * specifications into a set of working sparql queries.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public class APIQuery implements Cloneable, VarSupply, ClauseConsumer, ExpansionPoints {
    
	public static final int DEFAULT_PAGE_SIZE = 10;
	
	public static final int MAX_PAGE_SIZE = 250;
    
    public static final String PAGE_PARAM = "_page";
    public static final String PAGE_SIZE_PARAM = "_pageSize";

    public static final String NAME_PREFIX = "name-";
    public static final int NAME_LEN = NAME_PREFIX.length();
    
    public static final String LANG_PREFIX = "lang-";
    public static final int LANG_LEN = LANG_PREFIX.length();
    
    public static final String MIN_PREFIX = "min-";
    public static final int MIN_LEN = MIN_PREFIX.length();
    
    public static final String MAX_PREFIX = "max-";
    public static final int MAX_LEN = MAX_PREFIX.length();
    
    public static final String MIN_OPEN_PREFIX = "minEx-";
    public static final int MINO_LEN = MIN_OPEN_PREFIX.length();
    
    public static final String MAX_OPEN_PREFIX = "maxEx-";
    public static final int MAXO_LEN = MAX_OPEN_PREFIX.length();
    
    public static final String EXISTS_PREFIX = "exists-";
    public static final int EXISTS_LEN = EXISTS_PREFIX.length();
    
	public static final String NEAR_LAT = "near-lat";
	
	public static final String NEAR_LONG = "near-long";
    
	public static final String DISTANCE = "_distance";
    
    public static final String SEARCH = "_search";
    
    public static final String SORT = "_sort";
    
    public static final String SUBJECT_PARAM = "_subject";
    
    public static final String WHERE_PARAM = "_where";
    
    private static final String _SELECT_PARAM = "_select";
    
    public static final String _TEMPLATE = "_template";
    
    public static final String TEMPLATE_PARAM = "_properties";
    
    public static final String SHOW_PARAM = "_view";
    
    public static final String SELECT_VARNAME = "item";
    
    public static final Variable SELECT_VAR = RDFQ.var( "?" + SELECT_VARNAME );
    
    public static final String PREFIX_VAR = "?___";
    
    // Partial elements of the SELECT pattern 
    //  It would more elegant to use ARQ syntax tree Element/Expr
    //  but not sure how well that plays with JDO persistance for GAE
    //  so stick to string bashing for ease of debug and persistence
    
    protected StringBuffer propertyChains = new StringBuffer();
    protected StringBuffer whereExpressions = new StringBuffer();
    
    private StringBuffer orderExpressions = new StringBuffer();
    
    /**
        List of pseudo-triples which form the basic graph pattern element
        of this query.
    */
    protected List<RDFQ.Triple> basicGraphTriples = new ArrayList<RDFQ.Triple>();
    
    /**
        List of little infix expressions (operands must be RDFQ.Any's) which
        are SPARQL filters for this query. 
    */
    protected List<Infix> filterExpressions = new ArrayList<Infix>();
    
    protected String viewArgument = null;
    
    protected final ShortnameService sns;
    protected String defaultLanguage = null;
    
    protected int varcount = 0;
    
    protected final int defaultPageSize;
    protected int pageSize = DEFAULT_PAGE_SIZE;
    protected final int maxPageSize;
    
    protected int pageNumber = 0;
    protected Set<String> bindableVars = new HashSet<String>();
    protected Map<String, String> varProps= new HashMap<String, String>();   // Property names for bindableVars
    protected Resource subjectResource = null;
    
    protected String itemTemplate;
    protected String fixedQueryString = null;
    
    // TODO replace this by full property chain descriptions
    protected Set<Property> expansionPoints = new HashSet<Property>();

    /**
        Set to true to switch on LARQ indexing for this query, ie when an
        _search wossname is being used.
    */
    protected boolean needsLARQindex = false;
    
    static Logger log = LoggerFactory.getLogger( APIQuery.class );

    public APIQuery( ShortnameService sns ) {
        this( fakeQB(sns) );
    }
    
    private static final QueryBasis fakeQB( final ShortnameService sns ) {
    	return new QueryBasis() {
    		@Override public final ShortnameService sns() { return sns; }
    		@Override public final String getDefaultLanguage() { return null; }
    		@Override public final String getFixedSelect() { return null; }
    		@Override public final String getWhere() { return null; }
    		@Override public String getItemTemplate() { return null; }
    		@Override public final int getMaxPageSize() { return MAX_PAGE_SIZE; }
    		@Override public final int getDefaultPageSize() { return DEFAULT_PAGE_SIZE; }
    	};
    }
    
    /**
        The parameters that form the basis of an API Query.
     
     	@author chris
    */
    public interface QueryBasis {
    	ShortnameService sns();
    	String getDefaultLanguage();
    	String getFixedSelect();
    	String getWhere();
    	int getMaxPageSize();
    	int getDefaultPageSize();
		String getItemTemplate();
    }

    public APIQuery( QueryBasis qb ) {
        this.sns = qb.sns();
        this.defaultLanguage = qb.getDefaultLanguage();
        this.pageSize = qb.getDefaultPageSize();
        this.defaultPageSize = qb.getDefaultPageSize();
        this.maxPageSize = qb.getMaxPageSize();
        this.fixedQueryString = qb.getFixedSelect();
        this.itemTemplate = qb.getItemTemplate();
        String fw = qb.getWhere();
        if (fw != null) whereExpressions.append( fw );
    }

    public APIQuery clone() {
        try {
            APIQuery clone = (APIQuery) super.clone();
            clone.basicGraphTriples = new ArrayList<RDFQ.Triple>( basicGraphTriples );
            clone.filterExpressions = new ArrayList<Infix>( filterExpressions );
            clone.orderExpressions = new StringBuffer( orderExpressions );
            clone.whereExpressions = new StringBuffer( whereExpressions );
            clone.bindableVars = new HashSet<String>( bindableVars );
            clone.propertyChains = new StringBuffer( propertyChains );
            clone.varProps = new HashMap<String, String>( varProps );
            clone.expansionPoints = new HashSet<Property>( expansionPoints );
            clone.deferredFilters = new ArrayList<Deferred>( deferredFilters );
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new APIException("Can't happen :)", e);
        }
    }
    
    /**
     * Set the page size to use when paging through results.
     * If this is not called then a default size will be used.
     */
    public void setPageSize( int pageSize ) {
        this.pageSize = (pageSize > maxPageSize ? defaultPageSize : pageSize); // Math.min(pageSize, maxPageSize );
    }
    
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Set which page should be returned.
     */
    public void setPageNumber(int page) {
        this.pageNumber = page;
    }

    public int getPageNumber() {
        return pageNumber;
    }
    
    public void setTypeConstraint( Resource typeConstraint ) {
        addTriplePattern( SELECT_VAR, RDF.type, RDFQ.uri( typeConstraint.getURI() ) );
    }
    
    /**
     * Sets the query to just describe a single resource, rather than
     * search for a list
     * @param subj the target resource as either a prefix_name string or as a full URI
     */
    public void setSubject(String subj) {
        subjectResource = sns.normalizeResource(subj);
    }
    
    /**
     * Return true if this query is a fixed subject instead of a select
     */
    public boolean isFixedSubject() {
        return subjectResource != null;
    }
    
    /**
     * Return the fixed subject for a subject query, null for a select query
     */
    public String getSubject() {
        return subjectResource.getURI();
    }
    
    /**
     	introduced to try and pull apart the types of arguments to the different filtering
     	functions so that they're not all strings.
    */
    public static class Param
    	{
    	final String p;
    	public Param( String p ) { this.p = p; }
    	public static Param make( String p ) { return new Param( p ); }
    	
    	@Override public String toString() { return p; }
    	
		public String asString() { return p; }
		public boolean is( String thing ) { return p.equals(thing); }
		public boolean hasPrefix(String s) { return p.startsWith(s); }
		public Param substring(int n) { return new Param(p.substring(n)); }
		public String[] parts() { return p.split("\\."); }
		public boolean hasVariable() { return p.indexOf('{') >= 0; }
		public Param expand( CallContext cc ) { return new Param( cc.expand( p ) ); }
    	}
    
    public static class Deferred
    	{
    	final Param param;
    	final String val;
    	
    	public Deferred( Param param, String val ) 
    		{
    		this.param = param;
    		this.val = val;
    		}
    	
    	@Override public String toString() 
    		{ return "<deferred " + param + "=" + val + ">"; }
    	}
    
    private Map<String, String> languagesFor = new HashMap<String, String>();
        
    public void clearLanguages() {
    	languagesFor.clear();
    }
    
    public void setLanguagesFor( String fullParamName, String languages ) {
    	languagesFor.put( fullParamName, languages );    
    }
    
    /**
     * General interface for extending the query with a specified parameter.
     * This parameter types handled include _page, _orderBy, min-, name- and path parameters.
     * @return the name of the final property referencing the val, to allow type sensitive normalization
    */
    public String addFilterFromQuery( Param param, String val ) {
        if (param.is(PAGE_PARAM)) {
            setPageNumber( Integer.parseInt(val) ); 
        } else if (param.is(PAGE_SIZE_PARAM)) {
            setPageSize( Integer.parseInt(val) );
        } else if (param.hasPrefix(NAME_PREFIX)) {
        	param = param.substring(NAME_LEN);
            addNameProp(param, val);
        } else if (param.hasPrefix( LANG_PREFIX )) {
        	param = param.substring( LANG_LEN );
        	
        } else if (param.hasPrefix(MIN_PREFIX)) {
        	param = param.substring(MIN_LEN);
            addRangeFilter(param, val, ">=");
        } else if (param.hasPrefix(MIN_OPEN_PREFIX)) {
        	param = param.substring(MINO_LEN);
            addRangeFilter(param, val, ">");
        } else if (param.hasPrefix(MAX_PREFIX)) {
        	param = param.substring(MAX_LEN);
            addRangeFilter(param, val, "<=");
        } else if (param.hasPrefix(MAX_OPEN_PREFIX)) {
        	param = param.substring(MAXO_LEN);
            addRangeFilter(param, val, "<");
        } else if (param.hasPrefix(EXISTS_PREFIX)) {
        	param = param.substring(EXISTS_LEN);
            // TODO check for true/false value, currently ignores the negative case :)
            addPropertyHasValue( param ); // TODO
        } else if (param.hasPrefix(SEARCH)) {
            addSearchTriple( val );
        } else if (param.hasPrefix(SORT)) {
            setOrderBy( val );
        } else if (param.hasPrefix(_SELECT_PARAM )) {
        	fixedQueryString = val;
        } else if (param.hasPrefix(WHERE_PARAM)) {
        	addWhere( val );
        } else {
            addPropertyHasValue( param, val );
        }
        return lastPropertyOf(param);
    }

	private String lastPropertyOf(Param param) {
		String [] parts = param.asString().split( "\\." );
		return parts[parts.length - 1];
	}
    
    List<Deferred> deferredFilters = new ArrayList<Deferred>();
    
    public void deferrableAddFilter( Param param, String val ) {
    	if (param.hasVariable() || val.indexOf('{') >= 0) {
    		deferredFilters.add( new Deferred( param, val ) );
    	} else {
    		addFilterFromQuery( param, val );
    	}
    }

	public void activateDeferredFilters( CallContext cc ) {
		for (Deferred d: deferredFilters) {
			log.debug( "activating deferred filter " + d );
			addFilterFromQuery( d.param.expand( cc ), cc.expand( d.val ) );
		}
	}
    
    public void setViewByTemplateClause( String clause ) {
    	viewArgument = clause;
    }
    
    public APIQuery addNumericRangeFilter( Variable var, double base, double delta ) {
       addInfixSparqlFilter( RDFQ.literal( base - delta ), "<", var );
       addInfixSparqlFilter( var, "<", RDFQ.literal( base + delta) );
       return this;
    }

    protected void addRangeFilter( Param param, String val, String op ) {
        Variable newvar = newVar(); 
        String prop = addFilterFromQuery( param, newvar.name() );
        addInfixSparqlFilter( newvar, op, sns.normalizeNodeToRDFQ( prop, val, defaultLanguage ) );
    }
    
    private void addInfixSparqlFilter( Any l, String op, Any r ) {
    	filterExpressions.add( RDFQ.infix( l, op, r ) );
    }
            
    /**
     * Record a required expansion point, i.e. a view path prop.*
     * TODO Extend to full paths instead of just single step 
     */
    public void addExpansion(String uri) {
        expansionPoints.add( ResourceFactory.createProperty(uri) );
    }
    
    protected final static Resource PF_TEXT_MATCH = ResourceFactory.createProperty( "http://jena.hpl.hp.com/ARQ/property#textMatch" );
    
    protected void addSearchTriple( String val ) {
    	needsLARQindex = true;
        log.debug( "enabled LARQ indexing to search for " + val );
        addTriplePattern( SELECT_VAR, PF_TEXT_MATCH, RDFQ.literal( val ) );
    }
    
    public APIQuery addSubjectHasProperty( Resource P, Any O ) {
        addTriplePattern( SELECT_VAR, P, O );
        return this;
    }
    
    private void addTriplePattern( Variable varname, Resource P, Any O ) {
        basicGraphTriples.add( RDFQ.triple( varname, RDFQ.uri( P.getURI() ), O ) );
    }
    
    /**
     * Add a filter triple pattern.
     * @param var The subject a string ready to insert (e.g. can be a var name)
     * @param prop  The property as a shortname or URI
     * @param val  The value as a string, shortname. The expansion should take into
     * account the type of the property and created typed literals if necessary.
     */
    private void addTriplePattern( Variable var, String prop, String languages, String val ) {
    	Resource np = sns.normalizeResource(prop);
    	if (languages == null) {
	    	if (val.startsWith("?")) {
	    		// Record property which points to this variable for us in decoding binding values
	    		varProps.put(val.substring(1), prop);   
	    	}
			addTriplePattern( var, np, sns.normalizeNodeToRDFQ(prop, val, defaultLanguage) ); 
    	} else {
    		addLanguagedTriplePattern(var, prop, languages, val);
    	}
    }

	private void addLanguagedTriplePattern(Variable var, String prop, String languages, String val) {
		String[] langArray = languages.split( "," );
		Resource np = sns.normalizeResource(prop);
		if (langArray.length == 1) {
			addTriplePattern( var, np, sns.normalizeNodeToRDFQ( prop, val, langArray[0] ) ); 
		} else {
			Variable v = newVar();
			addTriplePattern( var, np, v );
			Apply stringOf = RDFQ.apply( "str", v );
			Infix equals = RDFQ.infix( stringOf, "=", RDFQ.literal( val ) );
			Infix filter = RDFQ.infix( equals, "&&", someOf( v, langArray ) );
			filterExpressions.add( filter );
		}
	}
    
    private RenderExpression someOf( Variable v, String[] langArray ) 
    	{
    	RenderExpression result = RDFQ.infix( RDFQ.apply( "lang", v ), "=", RDFQ.literal( langArray[0] ) );
    	for (int i = 1; i < langArray.length; i += 1)
    		result = RDFQ.infix( result, "||", RDFQ.infix( RDFQ.apply( "lang", v ), "=", RDFQ.literal( langArray[i] ) ) );
    	return result;
    	}

	private void addTriplePattern( Variable var, String prop, Variable val ) {
   		// Record property which points to this variable for us in decoding binding values
   		varProps.put( val.name().substring(1), prop );   
        addTriplePattern( var, sns.normalizeResource(prop), val ); 
    }

    protected String addPropertyHasValue( Param param ) {
    	return addPropertyHasValue( param, newVar().name() );
    }
    
    protected String addPropertyHasValue( Param param, Variable O ) {
    	return addPropertyHasValue( param, O.name() );    	
    }

    protected String addPropertyHasValue( Param param, String rawValue ) {
    	String languages = languagesFor.get( param.toString() );
    	if (languages == null) languages = defaultLanguage;
    	// System.err.println( ">> addPropertyHasValue for " + param + ", languages '" + languages + "'" );
        String[] path = param.parts();
        Variable var = SELECT_VAR;
        int i = 0;
        while (i <path.length-1) {
            Variable newvar = newVar();
            addTriplePattern(var, path[i], newvar );
            noteBindableVar(path[i]);
            var = newvar;
            i++;
        }
        addTriplePattern(var, path[i], languages, rawValue);
        noteBindableVar( path[i] );
        noteBindableVar( rawValue );
        return path[i];
    }

    /**
        Discard any existing order expressions (a string that
        may appear after SPARQL's ORDER BY). Add <code>orderBy</code>
        as the new order expressions.
    */
    public void setExplicitOrderBy( String orderBy ) {
    	orderExpressions.setLength(0);
    	orderExpressions.append( orderBy );
    }
    
    /**
        Discard any existing order expressions. Decode
        <code>orderSpec</code> to produce a new order expression.
        orderSpec is a comma-separated list of sort fields,
        each optionally proceeded by - for DESC. If the field
        is a variable, it is used as-is, otherwise it is assumed
        to be a short property name and an additional triple
        (?item Property Var) added to the query with the Var
        being the sort field.
    */
    public void setOrderBy( String orderSpecs ) {
    	orderExpressions.setLength(0);
    	for (String spec: orderSpecs.split(",")) {
	        boolean descending = spec.startsWith("-"); 
	        if (descending) spec = spec.substring(1);
	        boolean varOrder = spec.startsWith("?");
	        String var = varOrder ? spec : newVar().name(); // TODO
	        if (descending) {
	        	orderExpressions.append(" DESC(" + var + ") ");
	        } else {
	            orderExpressions.append(" " + var + " ");
	        }
	        if (!varOrder)
	            addPropertyHasValue(Param.make(spec), var); // TODO fix use of make
    	}
    }

    protected void noteBindableVar(Param p) {
    	// TODO fix to work properly
    	noteBindableVar( lastPropertyOf(p) );
    }

    protected void noteBindableVar(String propname) {
        if (propname.startsWith("?")) {
            if ( ! propname.startsWith(PREFIX_VAR) )
                bindableVars.add(propname.substring(1));
        }
    }
    
    public Variable newVar() {
        return RDFQ.var( PREFIX_VAR + varcount++ );
    }

    protected void addNameProp(Param param, String rawObject) {
        noteBindableVar(param);
        noteBindableVar(rawObject);
        Variable newvar = newVar();
        addPropertyHasValue( param, newvar );
        addTriplePattern( newvar, RDFS.label, asRDFQ( rawObject ) );
    }

	public Any asRDFQ( String rawObject ) {
		return rawObject.startsWith("?") ? RDFQ.var( rawObject ) : RDFQ.literal( rawObject );
	}
    
    private Pattern varPattern = Pattern.compile("\\?[a-zA-Z]\\w*");
    
	public void addWhere(String whereClause) {
		log.debug( "TODO: check the legality of the where clause: " + whereClause );
        if (whereExpressions.length() > 0)
            whereExpressions.append(" ");
        whereExpressions.append(whereClause);
        for (String var : RDFUtils.allMatches(varPattern, whereClause)) 
            noteBindableVar(var);
    }

    /**
     * Test if a parameter is supposed to be late-bound
     */
    public boolean isBindable(String var) {
    	if (var.startsWith("?"))
    		return bindableVars.contains(var.substring(1));
    	else
    		return bindableVars.contains(var);
    }
    
    public String assembleSelectQuery(PrefixMapping prefixes) {
//    	System.err.println( ">> ME ME ME" );
//    	new RuntimeException().printStackTrace(System.out);
    	if (fixedQueryString == null) {
	        StringBuffer q = new StringBuffer();
	        appendPrefixes( q, prefixes );
	        q.append("SELECT ");
	        if (orderExpressions.length() > 0) q.append("DISTINCT "); // Hack to work around lack of _select but seems a common pattern
	        q.append( SELECT_VAR.name() );
	        q.append(" WHERE {\n");
	        String bgp = constructBGP();
	        if (whereExpressions.length() > 0) {
	            q.append( whereExpressions );
	        } else {
		        if (bgp.isEmpty()) bgp = SELECT_VAR.name() + " ?__p ?__v ."; 
	        }
	        q.append( bgp );
	        appendFilterExpressions( q );
	        q.append( "} " );
	        if (orderExpressions.length() > 0) {
	            q.append(" ORDER BY ");
	            q.append( orderExpressions );
	        }
	        q.append(" OFFSET " + (pageNumber * pageSize));
	        q.append(" LIMIT " + pageSize);
//	        System.err.println( ">> QUERY IS: \n" + q.toString() );
	        return q.toString();
    	} else {
    		return fixedQueryString;
    	}
    }

	public void appendFilterExpressions( StringBuffer q ) {
		for (Infix i: filterExpressions) {
			q.append( i.asSparqlFilter() );
		}				
	}

	public String constructBGP() {
		StringBuilder sb = new StringBuilder();
		for (RDFQ.Triple t: basicGraphTriples)
			sb
				.append( t.S.asSparqlTerm() )
				.append( " " ).append( t.P.asSparqlTerm() )
				.append( " " ).append( t.O.asSparqlTerm() )
				.append( " .\n" )
				;
		return sb.toString();
	}
    
    private void appendPrefixes(StringBuffer q, PrefixMapping prefixes) {
		for (String prefix: prefixes.getNsPrefixMap().keySet()) {
			q
				.append( "PREFIX " )
				.append( prefix )
				.append( ": <" )
				.append( prefixes.getNsPrefixURI(prefix).trim() ) // !! TODO
				.append( ">\n" );
		}
	}

	public void consumeClause( String clause ) {
    	 propertyChains.append( clause );    	
    }
    
    protected String boundQuery(String query, CallContext call) {
        String bound = query;
        if (!bindableVars.isEmpty()) {
            for (String var : bindableVars) {
                String val = call.getParameterValue(var);
                if (val != null) {
                	String prop = varProps.get(var);
                	String normalizedValue = 
                		(prop == null) 
                		    ? sns.normalizeValue(val, defaultLanguage) 
                		    : sns.normalizeNodeToString(prop, val, defaultLanguage); 
                    bound = bound.replace("?"+var, normalizedValue);
                    // TODO improve this, will fail in case where ? is in nested literals
                    // Right long term answer is to switch to transforming algebra or parse tree
                } else {
                	// Unbound vars are normally OK, might be there to support orderBy or
                	// simply an existence check. Warning might not be necessary
                	if (! var.equals(SELECT_VARNAME))
                		log.debug("Query has unbound variable: " + var);
                }
            }
        }
        return bound;
    }

    /**
     * Return the select query that would be run or a plain string for the resource
     */
    public String getQueryString(APISpec spec, CallContext call) {
        if (isFixedSubject()) {
            return "<" + subjectResource.getURI() + ">";
        } else {
            return boundQuery( assembleSelectQuery(spec.getPrefixMap()), call);
        }
    }
    
    /**
     * Run the defined query against the datasource
     */
    public APIResultSet runQuery( APISpec spec, Cache cache, CallContext call, View view ) {
        Source source = spec.getDataSource();
        try {
            List<Resource> results = fetchRequiredResources( cache, spec, call, source );
            APIResultSet already = cache.getCachedResultSet( results, view.toString() );
            if (already != null && expansionPoints.isEmpty() ) 
                {
                log.debug( "re-using cached results for " + results );
                return already.clone();
                }
            
            APIResultSet rs = fetchDescriptionOfAllResources(spec, view, results);
            
            // Expand the labels of all leaf nodes (make this switchable?)
            new ExpandLabels( this ).expand( source, rs );
            
            // Expand chained views, if present
            if ( ! expansionPoints.isEmpty()) {
                for (Property exp : expansionPoints) {
                    expandResourcesOf(exp, rs, view, spec );
                }
            } else {
                // Can't cache results which use expansion points
                cache.cacheDescription( results, view.toString(), rs.clone() );
            }
            
            return rs;

        } catch (QueryExceptionHTTP ie) {
            throw new APIEndpointException("Cannot reach source SPARQL endpoint for " + source);
        }
    }

	private APIResultSet fetchDescriptionOfAllResources(APISpec spec, View view, List<Resource> results) {
		int count = results.size();
		Model descriptions = ModelFactory.createDefaultModel();
		Graph gd = descriptions.getGraph();
		if (count > 0) {
		    fetchDescriptionsFor( results, view, descriptions, spec );
		    return new APIResultSet(gd, results, count < pageSize);
		} else {
		    return new APIResultSet(gd, results, true);
		}
	}

    /** Find all current values for the given property on the results and fetch a description of them */
    private void expandResourcesOf(Property exp, APIResultSet rs, View view, APISpec spec ) {
        List<Resource> toExpand = new ArrayList<Resource>();
        for (Resource root : rs.getResultList()) {
            NodeIterator ni = rs.listObjectsOfProperty(root, exp);
            while (ni.hasNext()) {
                RDFNode n = ni.next();
                if (n.isAnon()) {
                    if (n.canAs(RDFList.class)) {
                        RDFList list = n.as(RDFList.class);
                        ExtendedIterator<RDFNode> li = list.iterator();
                        while (li.hasNext()) {
                            RDFNode l = li.next();
                            if (l.isURIResource()) toExpand.add( (Resource)l );
                        }
                    }
                } else if (n.isURIResource()) {
                    toExpand.add( (Resource)n );
                }
            }
        }
        fetchDescriptionsFor(toExpand, view, rs, spec);
    }
    
    // let's respect property chains ...
    private void fetchDescriptionsFor( List<Resource> roots, View view, Model m, APISpec spec ) {
        if (roots.isEmpty() || roots.get(0) == null) return;
        List<Source> sources = spec.getDescribeSources();
    //
        if (viewArgument != null) {
        	// TODO: avoid BRUTE FORCE to get things going
        	for (Resource root: roots) {
        		StringBuffer p = new StringBuffer();
        		String ta = viewArgument.replaceAll( "\\?item", "<" + root.getURI() + ">" );
        		appendPrefixes( p, spec.getPrefixMap() );
        		String query = "CONSTRUCT {" + ta + "} where {" + ta + "}\n";
        		Query cq = QueryFactory.create( p.append(query).toString() );
        		for (Source x: sources) m.add( x.execute( cq ).execConstruct() );
        	}
        	return;
        }
        view.fetchDescriptions( m, roots, sources, this );
    }
    
    private List<Resource> fetchRequiredResources( Cache cache, APISpec spec, CallContext call, Source source )
        {
    	log.debug( "fetchRequiredResources()" );
        List<Resource> results = new ArrayList<Resource>();
        if (itemTemplate != null) setSubject( call.expand( itemTemplate ) );
        if ( isFixedSubject() ) {
        	results.add( subjectResource );
        } else {
            String select = boundQuery( assembleSelectQuery(spec.getPrefixMap()), call);
            List<Resource> already = cache.getCachedResources( select );
            if (already != null)
                {
                log.debug( "re-using cached results for query " + select );
                return already;
                }
            Query q = null;
            try {
                q = QueryFactory.create(select);
            } catch (Exception e) {
                throw new APIException("Internal error building query: " + select, e);
            }
            
            log.debug( "Running query: " + select.replaceAll( "\n", " " ) );
            QueryExecution exec = source.execute( q );
            if (needsLARQindex) LARQManager.setLARQIndex( exec );
        
            try {
                // Run the select and find the matches
                ResultSet rs = exec.execSelect();
                while (rs.hasNext()) {
                	Resource item = rs.next().getResource( SELECT_VAR.name() );
                	if (item == null) {
                		throw new RuntimeException
                			( "<br>Oops. No binding for " + SELECT_VAR.name() + " in successful SELECT.\n"
                			+ "<br>Perhaps ?item was mis-spelled in an explicit api:where clause.\n"
                			+ "<br>It's not your fault; contact the API provider."                			
                			);                		
                	}
					results.add( item );
                }
                exec.close();
            } catch (APIException e) {
            	throw e;
            } catch (Throwable t) {
                exec.close();
                throw new APIException("Query execution problem on query: " + t, t);
            }          
            cache.cacheSelection( select, results );
        }
        return results;
        }

}

