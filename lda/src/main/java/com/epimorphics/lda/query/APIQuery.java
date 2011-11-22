/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$

    File:        APIQuery.java
    Created by:  Dave Reynolds
    Created on:  31 Jan 2010
*/

package com.epimorphics.lda.query;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.cache.Cache;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.core.Param;
import com.epimorphics.lda.core.VarSupply;
import com.epimorphics.lda.core.View;
import com.epimorphics.lda.core.Param.Info;
import com.epimorphics.lda.exceptions.APIException;
import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.rdfq.*;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.sources.Source;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.support.Controls;
import com.epimorphics.lda.support.LARQManager;
import com.epimorphics.lda.support.MultiMap;
import com.epimorphics.lda.support.PrefixLogger;
import com.epimorphics.lda.support.QuerySupport;
import com.epimorphics.lda.support.Times;

import com.epimorphics.util.CollectionUtils;
import com.epimorphics.util.Couple;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 	Query abstraction that supports assembling multiple filter/order/view
 	specifications into a set of working sparql queries.
  
 	@author <a href="mailto:der@epimorphics.com">Dave Reynolds</a>
 	@version $Revision: $
*/
public class APIQuery implements Cloneable, VarSupply {
    
    public static final Variable SELECT_VAR = RDFQ.var( "?item" );
    
    public static final String PREFIX_VAR = "?___";
    
    // Partial elements of the SELECT pattern 
    //  It would more elegant to use ARQ syntax tree Element/Expr
    //  but not sure how well that plays with JDO persistance for GAE
    //  so stick to string bashing for ease of debug and persistence
    
    protected StringBuffer whereExpressions = new StringBuffer();
    
    private StringBuffer orderExpressions = new StringBuffer();
    
    /**
        List of pseudo-triples which form the basic graph pattern element
        of this query.
    */
    protected List<RDFQ.Triple> basicGraphTriples = new ArrayList<RDFQ.Triple>();
    
    protected List<List<RDFQ.Triple>> optionalGraphTriples = new ArrayList<List<RDFQ.Triple>>(); 
    
    public List<RDFQ.Triple> getBasicGraphTriples() {
    	// FOR TESTING ONLY
    	return basicGraphTriples;
    }
    
    public List<List<RDFQ.Triple>> getOptionalGraphTriples() {
    	// FOR TESTING ONLY
    	return optionalGraphTriples;
    }
    
    /**
        List of little infix expressions (operands must be RDFQ.Any's) which
        are SPARQL filters for this query. 
    */
    protected List<RenderExpression> filterExpressions = new ArrayList<RenderExpression>();
    
    public List<RenderExpression> getFilterExpressions() {
    	// FOR ETSTING ONLY
    	return filterExpressions;
    }
    
    public void addFilterExpression( RenderExpression e ) {
    	filterExpressions.add( e );
    }
    
    protected String viewArgument = null;
    
    protected final ShortnameService sns;
    
    protected ValTranslator vt;
    
    protected String defaultLanguage = null;
    
    protected int varcount = 0;
    
    protected final int defaultPageSize;
    protected int pageSize = QueryParameter.DEFAULT_PAGE_SIZE;
    protected final int maxPageSize;
    
    protected int pageNumber = 0;
        
    protected Map<Variable, Info> varInfo = new HashMap<Variable, Info>();
    
    protected Resource subjectResource = null;
    
    protected String itemTemplate;
    protected String fixedSelect = null;
    
    protected Set<String> metadataOptions = new HashSet<String>();

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
    		@Override public String getItemTemplate() { return null; }
    		@Override public final int getMaxPageSize() { return QueryParameter.MAX_PAGE_SIZE; }
    		@Override public final int getDefaultPageSize() { return QueryParameter.DEFAULT_PAGE_SIZE; }
    	};
    }

	/**
        The parameters that form the basis of an API Query.
     
     	@author chris
    */
    public interface QueryBasis {
    	ShortnameService sns();
    	String getDefaultLanguage();
    	int getMaxPageSize();
    	int getDefaultPageSize();
		String getItemTemplate();
    }

    protected static class FilterExpressions implements ValTranslator.Filters {
    	
    	public FilterExpressions(List<RenderExpression> expressions ) {
    		this.expressions = expressions;
    	}
    	
    	protected final List<RenderExpression> expressions;
    	
		@Override public void add(RenderExpression e) {	expressions.add( e ); }
	}
	
    public APIQuery( QueryBasis qb ) {
        this.sns = qb.sns();
        this.vt = new ValTranslator( this, new FilterExpressions( filterExpressions ), qb.sns() );
        this.defaultLanguage = qb.getDefaultLanguage();
        this.pageSize = qb.getDefaultPageSize();
        this.defaultPageSize = qb.getDefaultPageSize();
        this.maxPageSize = qb.getMaxPageSize();
        this.itemTemplate = qb.getItemTemplate();
    }

    @Override public APIQuery clone() {
        try {
            APIQuery clone = (APIQuery) super.clone();
            clone.basicGraphTriples = new ArrayList<RDFQ.Triple>( basicGraphTriples );
            clone.optionalGraphTriples = new ArrayList<List<RDFQ.Triple>>( optionalGraphTriples );
            clone.filterExpressions = new ArrayList<RenderExpression>( filterExpressions );
            clone.orderExpressions = new StringBuffer( orderExpressions );
            clone.whereExpressions = new StringBuffer( whereExpressions );
            clone.varInfo = new HashMap<Variable, Info>( varInfo );
            clone.deferredFilters = new ArrayList<PendingParameterValue>( deferredFilters );
            clone.metadataOptions = new HashSet<String>( metadataOptions );
            clone.varsForPropertyChains = new HashMap<String, Variable>( varsForPropertyChains );
            clone.seenParamVariables = new HashMap<String, Variable>( seenParamVariables );
            clone.vt = new ValTranslator( clone, new FilterExpressions( clone.filterExpressions ), sns );
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
        this.pageSize = (pageSize > maxPageSize ? defaultPageSize : pageSize);
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
        subjectResource = sns.asResource(subj);
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
    
    private Map<String, String> languagesFor = new HashMap<String, String>();
        
    /**
        Set the default language, discarding any existing default language.
    */
    public void setDefaultLanguage( String defaults ) {
    	defaultLanguage = defaults;
    	}
    
    /**
        Answer the (current) default language string.
    */
    public String getDefaultLanguage() {
    	return defaultLanguage;
    }
    
    public void clearLanguages() {
    	languagesFor.clear();
    }
    
    public void setLanguagesFor( String fullParamName, String languages ) {
    	languagesFor.put( fullParamName, languages );    
    }
    
	private String languagesFor( Param param ) {
		String languages = languagesFor.get( param.toString() );
	    if (languages == null) languages = defaultLanguage;
		return languages;
	}
 
	public void addMetadataOptions( Set<String> options ) {
		metadataOptions.addAll( options );
	}
	
	public void addMetadataOptions( String [] options ) {
		for (String option: options) metadataOptions.add( option.toLowerCase() );
	}
    
    public List<PendingParameterValue> deferredFilters = new ArrayList<PendingParameterValue>();
    
    public void deferrableAddFilter( Param param, String val ) {
    	deferredFilters.add( new PendingParameterValue( param, val ) );
    }
    
    public void setViewByTemplateClause( String clause ) {
    	viewArgument = clause;
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
    
    protected Map<String,Variable> seenParamVariables = new HashMap<String, Variable>();
    
    protected void addRangeFilter( Param param, String val, String op ) {
    	Variable already = seenParamVariables.get(param.asString());
    	if (already == null) {
    		already = addPropertyHasValue_REV( param );
	        seenParamVariables.put( param.asString(), already );
    	}
	    Info inf = param.fullParts()[param.fullParts().length - 1];
	    Any r = objectForValue( inf, val, getDefaultLanguage() );
	    addInfixSparqlFilter( already, op, r );
    }    
	
	private void addInfixSparqlFilter(Variable already, String op, Any r) {
		addFilterExpression( RDFQ.infix( already, op, r ) );
	}

	public void addNumericRangeFilter( Variable v, double x, double dx ) {
		addInfixSparqlFilter( RDFQ.literal( x - dx ), "<", v );
		addInfixSparqlFilter( v, "<", RDFQ.literal( x + dx) );
	}
    
    private void addInfixSparqlFilter( Any v, String op, Any literal ) {
    	addFilterExpression( RDFQ.infix( v, op, literal ) );
	}

	private void addTriplePattern( Variable varname, Resource P, Any O ) {
        basicGraphTriples.add( RDFQ.triple( varname, RDFQ.uri( P.getURI() ), O ) );
    }
    
    /**
         Update this query-generator with a bunch of basic graph triples to
         use. Note: the argument is a list; the order is preserved apart from
         the special re-ordering rules.
    */
    public void addTriplePatterns( List<RDFQ.Triple> triples ) {
    	basicGraphTriples.addAll( triples );
    }   

	protected void addPropertyHasValue( Param param ) {
    	addPropertyHasValue_REV( param );
    }
    
    protected Variable addPropertyHasValue_REV( Param param ) {
    	Param.Info [] infos = param.fullParts();
		return expandParameterPrefix( infos );
    }

	private Variable expandParameterPrefix( Param.Info[] infos ) {
		StringBuilder chainName = new StringBuilder();
		String dot = "";
		Variable var = SELECT_VAR;
	    int i = 0;
	    while (i < infos.length) {
	    	Param.Info inf = infos[i];
	    	chainName.append( dot ).append( inf.shortName );
	    	Variable v = varsForPropertyChains.get( chainName.toString() );
	    	if (v == null) {
	    		v = RDFQ.var( PREFIX_VAR + chainName.toString().replaceAll( "\\.", "_" ) + "_" + varcount++ );
	    		varsForPropertyChains.put( chainName.toString(), v );
				varInfo.put( v, inf );
				basicGraphTriples.add( RDFQ.triple( var, inf.asURI, v ) );
	    	}
	    	dot = ".";
	    	var = v;
	        i += 1;
	    }
		return var;
	}
    
    protected Map<String, Variable> varsForPropertyChains = new HashMap<String, Variable>();
    
    protected void addPropertyHasValue( Param param, String val ) {
    	if (val.startsWith( "?" )) {
    		throw new EldaException( "property cannot be given variable as value", val, EldaException.SERVER_ERROR );
    		// addPropertyHasValue_REV( param, RDFQ.var( val ) );
    	}
    	else {
			Param.Info [] infos = param.fullParts();
			Variable var = expandParameterPrefix( allButLast( infos ) );
		//
		    Info inf = infos[infos.length - 1];
		    Any o = objectForValue( inf, val, languagesFor(param) );
		    if (o instanceof Variable) varInfo.put( (Variable) o, inf );
		    addTriplePattern( var, inf.asResource, o );
    	}
    }

    private Info[] allButLast(Info[] infos) {
    	int n = infos.length;
    	Info [] result = new Info[n - 1];
    	System.arraycopy( infos, 0, result, 0, n - 1 );
		return result;
	}

	/**
        Answer the RDFQ item which is the appropriate object to use in
        a triple with predicate defined by <code>inf</code> and with
        lexical form <code>val</code>. Any language codes that should be
        used appear in <code>languages</code>. May update this APIQuery's
        filter expressions.
    */
	private Any objectForValue(Info inf, String val, String languages) {
		return vt.objectForValue( inf, val, languages );
	}
	
	/**
	    Generate triples to bind <code>var</code> to the value of the
	    <code>param</code> property chain if it exists (ie all of the
	    triples are OPTIONAL).
	*/
	protected void optionalProperty( Variable startFrom, Param param, Variable var ) {
		Param.Info [] infos = param.fullParts();
		Variable s = startFrom;
		int remaining = infos.length;
		List<RDFQ.Triple> chain = new ArrayList<RDFQ.Triple>(infos.length);
	//
		for (Param.Info inf: infos) {
			remaining -= 1;
			Variable o = remaining == 0 ? var : newVar();
			onePropertyStep( chain, s, inf, o );
			s = o;
		}
		optionalGraphTriples.add( chain );
    }
	
	protected void addPropertyHasntValue( Param param ) {
    	Variable var = newVar();
    	optionalProperty( SELECT_VAR, param, var );
    	filterExpressions.add( RDFQ.apply( "!", RDFQ.apply( "bound", var ) ) );
    }  	  

	private void onePropertyStep( List<RDFQ.Triple> chain, Variable subject, Info prop, Variable var ) {
		Resource np = prop.asResource;
		varInfo.put( var, prop );
		chain.add( RDFQ.triple( subject, RDFQ.uri( np.getURI() ), var ) ); 
	}

    /**
        Discard any existing order expressions (a string that
        may appear after SPARQL's ORDER BY). Add <code>orderBy</code>
        as the new order expressions.
    */
    public void setOrderBy( String orderBy ) {
    	orderExpressions.setLength(0);
    	orderExpressions.append( orderBy );
    }
    
    public void setFixedSelect( String fixedSelect ) {
    	this.fixedSelect = fixedSelect;
    }
    
    /**
        Discard any existing order expressions. Decode
        <code>orderSpec</code> to produce a new order expression.
        orderSpec is a comma-separated list of sort fields,
        each optionally proceeded by - for DESC. Each field is 
        a property chain used to bind a new variable v when is used
        as the ORDER BY field.
    */
    public void setSortBy( String orderSpecs ) {
    	if (sortByOrderSpecsFrozen) 
    		EldaException.Broken( "Elda attempted to set a sort order after generating the select query." );
    	sortByOrderSpecs = orderSpecs;
    }
    
    protected String sortByOrderSpecs = "";
    
    protected boolean sortByOrderSpecsFrozen = false;
    
    static class Bool { boolean value; public Bool(boolean value) {this.value = value; }}
    
    protected void unpackSortByOrderSpecs() {
    	if (sortByOrderSpecsFrozen) 
    		EldaException.Broken( "Elda attempted to unpack the sort order after generating the select query." );
    	if (sortByOrderSpecs.length() > 0) {
    		orderExpressions.setLength(0);
    		Bool mightBeUnbound = new Bool(false);
	    	for (String spec: sortByOrderSpecs.split(",")) {
	    		if (spec.length() > 0) {
			        boolean descending = spec.startsWith("-"); 
			        if (descending) spec = spec.substring(1);
			        Variable v = generateSortVariable( spec, mightBeUnbound );
			        if (descending) {
			        	orderExpressions.append(" DESC(" + v.name() + ") ");
			        } else {
			            orderExpressions.append(" " + v.name() + " ");
			        }
	    		}
	    	}
	    	if (true) orderExpressions.append( " ?item" );
    	}
   	sortByOrderSpecsFrozen = true;
    }

	private Variable generateSortVariable( String spec, Bool mightBeUnbound ) {
		return generateSortVariable( SELECT_VAR, spec + ".", 0, mightBeUnbound );
	}
    
    private Variable generateSortVariable( Variable anchor, String spec, int where, Bool mightBeUnbound ) {
    	if (where == spec.length()) return anchor;
    //
    	int dot = spec.indexOf( '.', where );
    	String thing = spec.substring(0, dot);
    	Variable v = varsForPropertyChains.get( thing );
    	if (v == null) {
    		v = newVar();
    		optionalProperty( anchor, Param.make( sns, spec.substring( where ) ), v );
    		mightBeUnbound.value = true;
    		return v;
    	} else {
    		return generateSortVariable( v, spec, dot + 1, mightBeUnbound );
    	}
    }
    
    @Override public Variable newVar() {
        return RDFQ.var( PREFIX_VAR + varcount++ );
    }
    
    /**
        Answer the number of variables allocated so far (used
        for testing).
    */
    public int countVarsAllocated() {
    	return varcount;
    }

    protected void addNameProp(Param param, String literal) {
        Variable newvar = addPropertyHasValue_REV( param );
        addTriplePattern( newvar, RDFS.label, RDFQ.literal( literal ) );
    }
    
    private static final Pattern varPattern = Pattern.compile("\\?[a-zA-Z]\\w*");
    
	public void addWhere( String whereClause ) {
		log.debug( "TODO: check the legality of the where clause: " + whereClause );
        if (whereExpressions.length() > 0) whereExpressions.append(" ");
        whereExpressions.append(whereClause);
    }

    public String assembleSelectQuery( Bindings cc, PrefixMapping prefixes ) {  	
    	PrefixLogger pl = new PrefixLogger( prefixes );   
    	return assembleRawSelectQuery( pl, cc );
    }

    public String assembleSelectQuery( PrefixMapping prefixes ) {     	
    	PrefixLogger pl = new PrefixLogger( prefixes );
    	Bindings cc = Bindings.createContext( new Bindings(), new MultiMap<String, String>() );
    	return assembleRawSelectQuery( pl, cc );
    }
    
    public String assembleRawSelectQuery( PrefixLogger pl, Bindings cc ) { 
    	if (!sortByOrderSpecsFrozen) unpackSortByOrderSpecs();
    	if (fixedSelect == null) {
	        StringBuilder q = new StringBuilder();
	        q.append("SELECT ");
	        if (orderExpressions.length() > 0) q.append("DISTINCT "); // Hack to work around lack of _select but seems a common pattern
	        q.append( SELECT_VAR.name() );
	        q.append("\nWHERE {\n");
	        String bgp = constructBGP( pl );
	        if (whereExpressions.length() > 0) {
	        	q.append( whereExpressions ); 
	        	pl.findPrefixesIn( whereExpressions.toString() );
	        } else {
		        if (basicGraphTriples.isEmpty()) bgp = SELECT_VAR.name() + " ?__p ?__v .\n" + bgp; 
	        }
	        q.append( bgp );
	        appendFilterExpressions( pl, q );
	        q.append( "} " );
	        if (orderExpressions.length() > 0) {
	            q.append(" ORDER BY ");
	            q.append( orderExpressions );
	        	pl.findPrefixesIn( orderExpressions.toString() );
	        }
	        appendOffsetAndLimit( q );
//	         System.err.println( ">> QUERY IS: \n" + q.toString() );
	        String bound = bindDefinedvariables( pl, q.toString(), cc );
	        StringBuilder x = new StringBuilder();
	        pl.writePrefixes( x );
			x.append( bound );
	        return x.toString();
    	} else {
    		// TODO add code for LIMIT/OFFSET when tests exist.
    		pl.findPrefixesIn( fixedSelect );
    		String bound = bindDefinedvariables( pl, fixedSelect, cc );
    		StringBuilder sb = new StringBuilder();
    		pl.writePrefixes( sb );
			sb.append( bound );
    		appendOffsetAndLimit( sb );
    		return sb.toString();
    	}
    }

	private void appendOffsetAndLimit(StringBuilder q) {
		q.append(" OFFSET " + (pageNumber * pageSize));
		q.append(" LIMIT " + pageSize);
	}

	public void appendFilterExpressions(PrefixLogger pl, StringBuilder q ) {
		for (RenderExpression i: filterExpressions) {
			q.append( " FILTER (" );
			i.render( pl, q );
			q.append( ")\n" );
		}				
	}

	public String constructBGP( PrefixLogger pl ) {
		StringBuilder sb = new StringBuilder();
		for (RDFQ.Triple t: QuerySupport.reorder( basicGraphTriples ))
			sb
				.append( t.asSparqlTriple( pl ) )
				.append( " .\n" )
				;
		for (List<RDFQ.Triple> optional: optionalGraphTriples) {
			sb.append( "OPTIONAL { " );
			for (RDFQ.Triple t: optional) {
				sb.append( t.asSparqlTriple( pl ) ).append( " . " );
			}
			sb.append( "}\n" );
		}
		return sb.toString();
	}

	/**
	    Add SPARQL prefix declarations for all the prefixes in
	    <code>pm</code> to the StringBuilder <code>q</code>.
	*/
	private void appendPrefixes( StringBuilder q, PrefixMapping pm ) {
		for (String prefix: pm.getNsPrefixMap().keySet()) {
			q
				.append( "PREFIX " )
				.append( prefix )
				.append( ": <" )
				.append( pm.getNsPrefixURI(prefix).trim() ) // !! TODO
				.append( ">\n" );
		}
	}
    
	/**
	    Take the SPARQL query string <code>query</code> and replace any ?SPOO
	    where SPOO is a variable bound in <code>cc</code> with the SPARQL
	    representation of that variable's value. Note that this will include
	    <i>any</i> occurrences of ?SPOO, including those inside SPARQL quotes.
	    Fixing this probably should happen earlier, but note that bits of query
	    are mashed together from strings in the config file, ie, without going
	    through RDFQ.	    
	*/
    protected String bindDefinedvariables( PrefixLogger pl, String query, Bindings cc ) {
//    	System.err.println( ">> query is: " + query );
//    	System.err.println( ">> VarValues is: " + cc );
    	StringBuilder result = new StringBuilder( query.length() );
    	Matcher m = varPattern.matcher( query );
    	int start = 0;
    	while (m.find( start )) {
    		result.append( query.substring( start, m.start() ) );
    		String name = m.group().substring(1);
    		Value v = cc.get( name );
//    		System.err.println( ">> value of " + name + " is " + v );
    		if (v == null) {
    			result.append( m.group() );
    		} else {
	    		Info prop = varInfo.get( RDFQ.var( "?" + name ) );
	            String val = cc.getValueString( name );
	            String normalizedValue = 
	        		(prop == null) 
	        		    ? valueAsSparql( "<not used>", v )
	        		    : objectForValue( prop, val, defaultLanguage ).asSparqlTerm(pl);
	    		result.append( normalizedValue );
    		}
    		start = m.end();
    	}
    	result.append( query.substring( start ) );
    	return result.toString();
    }

    private String valueAsSparql( String OTHER, Value v ) {
    	String type = v.type();
    	if (type.equals( "" )) return "\"" + protect(v.spelling()) + "\"";
    	if (type.equals( RDFS.Resource.getURI() )) return "<" + v.spelling() + ">";
    	throw new RuntimeException( "valueAsSparql: cannot handle type: " + type + "; maybe try " + OTHER + "?" );
    }

	private String protect(String valueString) {
		return valueString
			.replaceAll( "\\\\", "\\\\" )
			.replaceAll( "'", "\\'" )
			;
	}

	/**
     * Return the select query that would be run or a plain string for the resource
     */
    public String getQueryString(APISpec spec, Bindings call) {
        return isFixedSubject()
            ? "<" + subjectResource.getURI() + ">"
            : assembleSelectQuery( call, spec.getPrefixMap() )
            ;
    }
    
    /**
        Run the defined query against the datasource
    */
    public APIResultSet runQuery( Controls c, APISpec spec, Cache cache, Bindings call, View view ) {
        Source source = spec.getDataSource();
        try {
        	return runQueryWithSource( c, spec, cache, call, view, source );
        } catch (QueryExceptionHTTP e) {
            EldaException.ARQ_Exception( source, e );
            return /* NEVER */ null;
        }
    }

	private APIResultSet runQueryWithSource( Controls c, APISpec spec, Cache cache, Bindings call, View view, Source source ) {
		Times t = c.times;
		long origin = System.currentTimeMillis();
		Couple<String, List<Resource>> queryAndResults = selectResources( c, cache, spec, call, source );
		long afterSelect = System.currentTimeMillis();
		
		t.setSelectionDuration( afterSelect - origin );
		String outerSelect = queryAndResults.a;
		List<Resource> results = queryAndResults.b;
		
		APIResultSet already = cache.getCachedResultSet( results, view.toString() );
		if (c.allowCache && already != null) 
		    {
			t.usedViewCache();
		    log.debug( "re-using cached results for " + results );
		    return already.clone();
		    }
		
		APIResultSet rs = fetchDescriptionOfAllResources(c, outerSelect, spec, view, results);
		
		long afterView = System.currentTimeMillis();
		t.setViewDuration( afterView - afterSelect );		
		rs.setSelectQuery( outerSelect );
	    cache.cacheDescription( results, view.toString(), rs.clone() );
		return rs;
	}

	private APIResultSet fetchDescriptionOfAllResources( Controls c, String select, APISpec spec, View view, List<Resource> results) {
		int count = results.size();
		Model descriptions = ModelFactory.createDefaultModel();
		Graph gd = descriptions.getGraph();
		String detailsQuery = fetchDescriptionsFor( c, select, results, view, descriptions, spec );
		return new APIResultSet(gd, results, count < pageSize, detailsQuery );
	}

    /** Find all current values for the given property on the results and fetch a description of them */
    private void expandResourcesOf( Controls c, Property exp, APIResultSet rs, View view, APISpec spec ) {
    	Model rsm = rs.getModel();
        List<Resource> toExpand = new ArrayList<Resource>();
        for (Resource root : rs.getResultList()) {
            NodeIterator ni = rsm.listObjectsOfProperty(root, exp);
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
        System.err.println( "property.* not implemented at the moment." );
        if (true) throw new UnsupportedOperationException( "property.* not implemented at the moment." ); // TODO
        fetchDescriptionsFor( c, "\nSELECT ?item\n WHERE {}", toExpand, view, rsm, spec);
    }
    
    // let's respect property chains ...
    private String fetchDescriptionsFor( Controls c, String select, List<Resource> roots, View view, Model m, APISpec spec ) {
        if (roots.isEmpty() || roots.get(0) == null) return "# no results, no query.";
        List<Source> sources = spec.getDescribeSources();
        m.setNsPrefixes( spec.getPrefixMap() );
        return viewArgument == null
        	? view.fetchDescriptions( c, new View.State( select, roots, m, sources, this ) )
        	: viewByTemplate( roots, m, spec, sources )
        	;
    }

	private String viewByTemplate(List<Resource> roots, Model m, APISpec spec, List<Source> sources) {
		StringBuilder clauses = new StringBuilder();
		for (Resource root: roots)
			clauses
				.append( "  " )
				.append( viewArgument.replaceAll( "\\?item", "<" + root.getURI() + ">" ) )
				.append( "\n" )
				;
		StringBuilder query = new StringBuilder( clauses.length() * 2 + 17 );
		appendPrefixes( query, spec.getPrefixMap() );
		query
			.append( "CONSTRUCT {\n" )
			.append( clauses )
			.append( "} where {" )
			.append( clauses )
			.append( "}\n" )
			;
		String qq = query.toString();
		Query cq = QueryFactory.create( qq );
		for (Source x: sources) m.add( x.executeConstruct( cq ) );		
		return qq;
	}

	/**
	    Answer the select query (if any; otherwise, "") and list of resources obtained by
	    running that query.
	*/
    private Couple<String, List<Resource>> selectResources( Controls c, Cache cache, APISpec spec, Bindings call, Source source ) {
    	log.debug( "fetchRequiredResources()" );
        final List<Resource> results = new ArrayList<Resource>();
        if (itemTemplate != null) setSubject( call.expandVariables( itemTemplate ) );
        if ( isFixedSubject() )
            return new Couple<String, List<Resource>>( "", CollectionUtils.list( subjectResource ) );
        else
        	return runGeneralQuery( c, cache, spec, call, source, results );
    }

	private Couple<String, List<Resource>> runGeneralQuery( Controls c, Cache cache, APISpec spec, Bindings cc, Source source, final List<Resource> results) {
		String selectQuery = assembleSelectQuery( cc, spec.getPrefixMap() );
		c.times.setSelectQuerySize( selectQuery );
		List<Resource> already = cache.getCachedResources( selectQuery );
		if (c.allowCache && already != null)
		    {
			c.times.usedSelectionCache();
		    log.debug( "re-using cached results for query " + selectQuery );
		    return new Couple<String, List<Resource>>(selectQuery, already);
		    }
		Query q = createQuery( selectQuery );
		log.debug( "Running query: " + selectQuery.replaceAll( "\n", " " ) );
		source.executeSelect( q, new ResultResourcesReader( results, needsLARQindex ) );
		cache.cacheSelection( selectQuery, results );
		return new Couple<String, List<Resource>>( selectQuery, results );
	}

	private Query createQuery( String selectQuery ) {
		try 
			{ return QueryFactory.create(selectQuery); } 
		catch (Exception e) {
		    throw new APIException("Internal error building query: " + selectQuery, e);
		}
	}
	
	private static final class ResultResourcesReader implements Source.ResultSetConsumer {
		
		private final List<Resource> results;
		private final boolean needsLARQindex;

		private ResultResourcesReader( List<Resource> results, boolean needsLARQindex ) {
			this.results = results;
			this.needsLARQindex = needsLARQindex;
		}

		@Override public void setup( QueryExecution qe ) {
		    if (needsLARQindex) LARQManager.setLARQIndex( qe );				
		}

		@Override public void consume( ResultSet rs ) {
			try {
				while (rs.hasNext()) {
					Resource item = rs.next().getResource( SELECT_VAR.name() );
					if (item == null) {
						EldaException.BadSpecification
						( "<br>Oops. No binding for " + SELECT_VAR.name() + " in successful SELECT.\n"
								+ "<br>Perhaps ?item was mis-spelled in an explicit api:where clause.\n"
								+ "<br>It's not your fault; contact the API provider."                			
						);                		
					}
					results.add( withoutModel( item ) );
				}
			} catch (APIException e) {
				throw e;
			} catch (Throwable t) {
				throw new APIException("Query execution problem on query: " + t, t);
			}				
		}

		private Resource withoutModel( Resource item ) {
			return ResourceFactory.createResource( item.getURI() );
		}
	}

	public boolean wantsMetadata( String name ) {
		return metadataOptions.contains( name ) || metadataOptions.contains( "all" );
	}

}

