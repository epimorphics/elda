/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
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
import com.epimorphics.lda.core.*;
import com.epimorphics.lda.core.Param.Info;
import com.epimorphics.lda.exceptions.APIException;
import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.rdfq.*;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.sources.Source;
import com.epimorphics.lda.sources.Source.ResultSetConsumer;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.support.*;
import com.epimorphics.lda.textsearch.TextSearchConfig;
import com.epimorphics.util.*;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Query abstraction that supports assembling multiple filter/order/view
 * specifications into a set of working sparql queries.
 * 
 * @author <a href="mailto:der@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public class APIQuery implements VarSupply, WantsMetadata {
	
	static final Logger log = LoggerFactory.getLogger(APIQuery.class);

	public static final Variable SELECT_VAR = RDFQ.var("?item");

	public static final String PREFIX_VAR = "?___";

	/**
	 * List of pseudo-triples which form the basic graph pattern element of this
	 * query.
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
	 * List of little infix expressions (operands must be RDFQ.Any's) which are
	 * SPARQL filters for this query.
	 */
	protected final List<RenderExpression> filterExpressions;

	public List<RenderExpression> getFilterExpressions() {
		// FOR ETSTING ONLY
		return filterExpressions;
	}

	public void addFilterExpression(RenderExpression e) {
		filterExpressions.add(e);
	}

	protected final TextSearchConfig textSearchConfig;

	public TextSearchConfig getItemSource() {
		return textSearchConfig;
	}

	private boolean isItemEndpoint = false;

	protected String defaultLanguage = null;

	protected int varcount = 0;

	protected int pageSize = QueryParameter.DEFAULT_PAGE_SIZE;

	protected int pageNumber = 0;

	protected Resource subjectResource = null;

	protected String itemTemplate = null;

	protected String fixedSelect = null;

	protected boolean enableETags = false;

	protected String sortByOrderSpecs = "";

	protected boolean sortByOrderSpecsFrozen = false;

	protected String graphName = null;
	
	protected String graphTemplate = null;

	/**
	 * Pattern for matching SPARQL query variables (including the leading '?').
	 * Used for finding substitution points in static query strings.
	 */
	public static final Pattern varPattern = Pattern.compile("\\?[a-zA-Z]\\w*");

	protected final int defaultPageSize;

	protected final int maxPageSize;

	protected final ShortnameService sns;

	protected final ValTranslator vt;

	protected final StringBuffer whereExpressions;

	private final StringBuffer orderExpressions;

	protected final Map<Variable, Info> varInfo;

	protected final Set<String> allowedReserved;

	protected final Set<String> metadataOptions;

	private final Map<String, String> languagesFor;

	public final List<PendingParameterValue> deferredFilters;
	
	public final long cacheExpiryMilliseconds;
	
	/**
	    Is a total count requested for this query? true, false, or null
	    for optional.
	 */
	private Boolean totalCountRequested = null;
	
	/**
	    If the current total count request is null, set it to <code>b</code>
	    and return true; otherwise return false (meaning "could not set").
	*/
	public boolean setTotalCountRequested(boolean b) {
		if (totalCountRequested == null) {
			totalCountRequested = b;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Map from property chain names (ie dotted strings) to the variable at the
	 * end of that chain. Allows different instances of a property chain (in the
	 * same APIQuery) to share the variable.
	 */
	protected final Map<String, Variable> varsForPropertyChains;

	/**
	 * The parameters that form the basis of an API Query.
	 * 
	 * @author chris
	 */
	public interface QueryBasis {
		
		ShortnameService sns();

		String getDefaultLanguage();

		int getMaxPageSize();

		int getDefaultPageSize();

		String getGraphTemplate();
		
		String getItemTemplate();

		boolean isItemEndpoint();

		TextSearchConfig getTextSearchConfig();
		
		Boolean getEnableCounting();
		
		long getCacheExpiryMilliseconds();
	}

	protected static class FilterExpressions implements ValTranslator.Filters {

		public FilterExpressions(List<RenderExpression> expressions) {
			this.expressions = expressions;
		}

		protected final List<RenderExpression> expressions;

		@Override public void add(RenderExpression e) {
			expressions.add(e);
		}
	}

	public APIQuery(QueryBasis qb) {
		this.sns = qb.sns();
		this.defaultLanguage = qb.getDefaultLanguage();
		this.pageSize = qb.getDefaultPageSize();
		this.defaultPageSize = qb.getDefaultPageSize();
		this.totalCountRequested = qb.getEnableCounting();
		this.maxPageSize = qb.getMaxPageSize();
		this.itemTemplate = qb.getItemTemplate();
		this.isItemEndpoint = qb.isItemEndpoint();
		this.textSearchConfig = qb.getTextSearchConfig();
		this.cacheExpiryMilliseconds = qb.getCacheExpiryMilliseconds();
	//
		this.graphTemplate = qb.getGraphTemplate();
	//
		this.deferredFilters = new ArrayList<PendingParameterValue>();
		this.whereExpressions = new StringBuffer();
		this.varsForPropertyChains = new HashMap<String, Variable>();
		this.allowedReserved = new HashSet<String>();
		this.metadataOptions = new HashSet<String>();
		this.languagesFor = new HashMap<String, String>();
		this.varInfo = new HashMap<Variable, Info>();
		this.orderExpressions = new StringBuffer();
		this.filterExpressions = new ArrayList<RenderExpression>();
		this.vt = new ValTranslator(this, new FilterExpressions(filterExpressions), sns);
	}

	public APIQuery copy() {
		return new APIQuery(this);
	}

	public APIQuery(APIQuery other) {
		this.sns = other.sns;
		this.maxPageSize = other.maxPageSize;
		this.defaultPageSize = other.defaultPageSize;
		this.defaultLanguage = other.defaultLanguage;
		this.totalCountRequested = other.totalCountRequested;
		this.enableETags = other.enableETags;
		this.fixedSelect = other.fixedSelect;
		this.isItemEndpoint = other.isItemEndpoint;
		this.itemTemplate = other.itemTemplate;
		this.pageNumber = other.pageNumber;
		this.pageSize = other.pageSize;
		this.sortByOrderSpecs = other.sortByOrderSpecs;
		this.sortByOrderSpecsFrozen = other.sortByOrderSpecsFrozen;
		this.subjectResource = other.subjectResource;
		this.varcount = other.varcount;
		this.textSearchConfig = other.textSearchConfig;
		this.cacheExpiryMilliseconds = other.cacheExpiryMilliseconds;
	//
		this.graphName = other.graphName;
		this.graphTemplate = other.graphTemplate;
		this.languagesFor = new HashMap<String, String>(other.languagesFor);
		this.basicGraphTriples = new ArrayList<RDFQ.Triple>(other.basicGraphTriples);
		this.optionalGraphTriples = new ArrayList<List<RDFQ.Triple>>(other.optionalGraphTriples);
		this.filterExpressions = new ArrayList<RenderExpression>(other.filterExpressions);
		this.orderExpressions = new StringBuffer(other.orderExpressions);
		this.whereExpressions = new StringBuffer(other.whereExpressions);
		this.varInfo = new HashMap<Variable, Info>(other.varInfo);
		this.deferredFilters = new ArrayList<PendingParameterValue>(other.deferredFilters);
		this.metadataOptions = new HashSet<String>(other.metadataOptions);
		this.varsForPropertyChains = new HashMap<String, Variable>(other.varsForPropertyChains);
		this.vt = new ValTranslator(this, new FilterExpressions(this.filterExpressions), this.sns);
		this.allowedReserved = new HashSet<String>(other.allowedReserved);
	}

	public void setGraphName(String graphName) {
		this.graphName = graphName;
	}
	
	/**
	 * Set the etags enable flag; true -> enabled.
	 */
	public void setEnableETags(boolean e) {
		this.enableETags = e;
	}

	/**
	 * Set the page size to use when paging through results. If this is not
	 * called then a default size will be used.
	 */
	public void setPageSize(int pageSize) {
		this.pageSize = (pageSize > maxPageSize ? maxPageSize : pageSize);
	}

	/**
	 * Answer the currently-set page size.
	 */
	public int getPageSize() {
		return pageSize;
	}

	/**
	 * Set which page should be returned.
	 */
	public void setPageNumber(int page) {
		this.pageNumber = page;
	}

	/**
	 * Answer the currenty-set page number.
	 */
	public int getPageNumber() {
		return pageNumber;
	}

	public void setTypeConstraint(Resource typeConstraint) {
		addTriplePattern(SELECT_VAR, RDF.type,
				RDFQ.uri(typeConstraint.getURI()));
	}

	/**
	 * Sets the query to just describe a single resource, rather than search for
	 * a list
	 * 
	 * @param subj
	 *            the target resource as either a prefix_name string or as a
	 *            full URI
	 */
	public void setSubjectAsItemEndpoint(String subj) {
		subjectResource = sns.asResource(subj);
		isItemEndpoint = true;
	}

	public void setSubject(String subj) {
		subjectResource = sns.asResource(subj);
	}

	public void addAllowReserved(String name) {
		allowedReserved.add(name);
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
	 * Set the default language, discarding any existing default language.
	 */
	public void setDefaultLanguage(String defaults) {
		defaultLanguage = defaults;
	}

	/**
	 * Answer the (current) default language string.
	 */
	public String getDefaultLanguage() {
		return defaultLanguage;
	}

	public void clearLanguages() {
		languagesFor.clear();
	}

	public void setLanguagesFor(String fullParamName, String languages) {
		languagesFor.put(fullParamName, languages);
	}

	private String languagesFor(Param param) {
		return languagesFor(param.toString());
	}

	public String languagesFor(String param) {
		String languages = languagesFor.get(param);
		if (languages == null)
			languages = defaultLanguage;
		return languages;
	}

	public void addMetadataOptions(String[] options) {
		for (String option : options)
			metadataOptions.add(option.toLowerCase());
	}

	public void deferrableAddFilter(Param param, String val) {
		deferredFilters.add(new PendingParameterValue(param, val));
	}

	public void addSearchTriple(String val) {
		Value literal = RDFQ.literal(val);
		Property queryProperty = textSearchConfig.getTextQueryProperty();
		Property contentProperty = textSearchConfig.getTextContentProperty();
		AnyList operand = textSearchConfig.getTextSearchOperand();
		//
		if (operand == null) {
			if (contentProperty.equals(TextSearchConfig.DEFAULT_CONTENT_PROPERTY)) {
				addTriplePattern(SELECT_VAR, queryProperty, literal);
			} else {
				Any cp = RDFQ.uri(contentProperty.getURI());
				AnyList searchOperand = RDFQ.list(cp, literal);
				addTriplePattern(SELECT_VAR, queryProperty, searchOperand);
			}
		} else {
			List<Any> elements = operand.getElements();
			Any[] y = new Any[operand.size()];
			for (int i = 0; i < elements.size(); i += 1)
				y[i] = substitute(elements.get(i), literal);
			AnyList searchOperand = RDFQ.list(y);
			addTriplePattern(SELECT_VAR, queryProperty, searchOperand);
		}
	}

	private static final Value variable_search = RDFQ.literal("?_search");

	private Any substitute(Any any, Value literal) {
		return any.equals(variable_search) ? literal : any;
	}

	public APIQuery addSubjectHasProperty(Resource P, Any O) {
		addTriplePattern(SELECT_VAR, P, O);
		return this;
	}

	protected void addRangeFilter(Param param, String val, String op) {
		Variable already = varsForPropertyChains.get(param.asString());
		if (already == null) {
			already = addPropertyHasValue_REV(param);
			varsForPropertyChains.put(param.asString(), already);
		}
		Info inf = param.fullParts()[param.fullParts().length - 1];
		Any r = objectForValue(inf, val, getDefaultLanguage());
		addInfixSparqlFilter(already, op, r);
	}

	private void addInfixSparqlFilter(Variable already, String op, Any r) {
		addFilterExpression(RDFQ.infix(already, op, r));
	}

	public void addNumericRangeFilter(Variable v, double x, double dx) {
		addInfixSparqlFilter(RDFQ.literal(x - dx), "<", v);
		addInfixSparqlFilter(v, "<", RDFQ.literal(x + dx));
	}

	private void addInfixSparqlFilter(Any v, String op, Any literal) {
		addFilterExpression(RDFQ.infix(v, op, literal));
	}

	private void addTriplePattern(Variable varname, Resource P, Any O) {
		basicGraphTriples.add(RDFQ.triple(varname, RDFQ.uri(P.getURI()), O));
	}

	/**
	 * Update this query-generator with a bunch of basic graph triples to use.
	 * Note: the argument is a list; the order is preserved apart from the
	 * special re-ordering rules.
	 */
	public void addTriplePatterns(List<RDFQ.Triple> triples) {
		basicGraphTriples.addAll(triples);
	}

	protected void addPropertyHasValue(Param param) {
		addPropertyHasValue_REV(param);
	}

	protected Variable addPropertyHasValue_REV(Param param) {
		Param.Info[] infos = param.fullParts();
		return expandParameterPrefix(infos);
	}

	private Variable expandParameterPrefix(Param.Info[] infos) {
		StringBuilder chainName = new StringBuilder();
		String dot = "";
		Variable var = SELECT_VAR;
		int i = 0;
		while (i < infos.length) {
			Param.Info inf = infos[i];
			chainName.append(dot).append(inf.shortName);
			Variable v = varsForPropertyChains.get(chainName.toString());
			if (v == null) {
				v = RDFQ.var(PREFIX_VAR
						+ chainName.toString().replaceAll("\\.", "_") + "_"
						+ varcount++);
				varsForPropertyChains.put(chainName.toString(), v);
				varInfo.put(v, inf);
				basicGraphTriples.add(RDFQ.triple(var, inf.asURI, v));
			}
			dot = ".";
			var = v;
			i += 1;
		}
		return var;
	}

	protected void addPropertyHasValue(Param param, String val) {
		if (val.startsWith("?")) {
			throw new EldaException(
					"property cannot be given variable as value", val,
					EldaException.SERVER_ERROR);
			// addPropertyHasValue_REV( param, RDFQ.var( val ) );
		} else {
			Param.Info[] infos = param.fullParts();
			Variable var = expandParameterPrefix(allButLast(infos));
			//
			Info inf = infos[infos.length - 1];
			Any o = objectForValue(inf, val, languagesFor(param));
			if (o instanceof Variable)
				varInfo.put((Variable) o, inf);
			addTriplePattern(var, inf.asResource, o);
		}
	}

	private Info[] allButLast(Info[] infos) {
		int n = infos.length;
		Info[] result = new Info[n - 1];
		System.arraycopy(infos, 0, result, 0, n - 1);
		return result;
	}

	/**
	 * Answer the RDFQ item which is the appropriate object to use in a triple
	 * with predicate defined by <code>inf</code> and with lexical form
	 * <code>val</code>. Any language codes that should be used appear in
	 * <code>languages</code>. May update this APIQuery's filter expressions.
	 */
	private Any objectForValue(Info inf, String val, String languages) {
		return vt.objectForValue(inf, val, languages);
	}

	/**
	 * Generate triples to bind <code>var</code> to the value of the
	 * <code>param</code> property chain if it exists (ie all of the triples are
	 * OPTIONAL).
	 */
	protected void optionalProperty(Variable startFrom, Param param,
			Variable var) {
		Param.Info[] infos = param.fullParts();
		Variable s = startFrom;
		int remaining = infos.length;
		List<RDFQ.Triple> chain = new ArrayList<RDFQ.Triple>(infos.length);
		//
		for (Param.Info inf : infos) {
			remaining -= 1;
			Variable o = remaining == 0 ? var : newVar();
			onePropertyStep(chain, s, inf, o);
			s = o;
		}
		optionalGraphTriples.add(chain);
	}

	protected void addPropertyHasntValue(Param param) {
		Variable var = newVar();
		optionalProperty(SELECT_VAR, param, var);
		filterExpressions.add(RDFQ.apply("!", RDFQ.apply("bound", var)));
	}

	private void onePropertyStep(List<RDFQ.Triple> chain, Variable subject,
			Info prop, Variable var) {
		Resource np = prop.asResource;
		varInfo.put(var, prop);
		chain.add(RDFQ.triple(subject, RDFQ.uri(np.getURI()), var));
	}

	/**
	 * Discard any existing order expressions (a string that may appear after
	 * SPARQL's ORDER BY). Add <code>orderBy</code> as the new order
	 * expressions.
	 */
	public void setOrderBy(String orderBy) {
		orderExpressions.setLength(0);
		orderExpressions.append(orderBy);
	}

	public void setFixedSelect(String fixedSelect) {
		this.fixedSelect = fixedSelect;
	}

	/**
	 * Discard any existing order expressions. Decode <code>orderSpec</code> to
	 * produce a new order expression. orderSpec is a comma-separated list of
	 * sort fields, each optionally proceeded by - for DESC. Each field is a
	 * property chain used to bind a new variable v when is used as the ORDER BY
	 * field.
	 */
	public void setSortBy(String orderSpecs) {
		if (sortByOrderSpecsFrozen)
			EldaException.Broken("Elda attempted to set a sort order after generating the select query.");
		sortByOrderSpecs = orderSpecs;
	}

	static class Bool {
		boolean value;

		public Bool(boolean value) {
			this.value = value;
		}
	}

	protected void unpackSortByOrderSpecs() {
		if (sortByOrderSpecsFrozen)
			EldaException.Broken("Elda attempted to unpack the sort order after generating the select query.");
		if (sortByOrderSpecs.length() > 0) {
			orderExpressions.setLength(0);
			Bool mightBeUnbound = new Bool(false);
			for (String spec : sortByOrderSpecs.split(",")) {
				if (spec.length() > 0) {
					boolean descending = spec.startsWith("-");
					if (descending)
						spec = spec.substring(1);
					Variable v = generateSortVariable(spec, mightBeUnbound);
					if (descending) {
						orderExpressions.append(" DESC(" + v.name() + ") ");
					} else {
						orderExpressions.append(" " + v.name() + " ");
					}
				}
			}
			if (true)
				orderExpressions.append(" ?item");
		}
		sortByOrderSpecsFrozen = true;
	}

	private Variable generateSortVariable(String spec, Bool mightBeUnbound) {
		return generateSortVariable(SELECT_VAR, spec + ".", 0, mightBeUnbound);
	}

	private Variable generateSortVariable(Variable anchor, String spec, int where, Bool mightBeUnbound) {
		if (where == spec.length())
			return anchor;
		//
		int dot = spec.indexOf('.', where);
		String thing = spec.substring(0, dot);
		Variable v = varsForPropertyChains.get(thing);
		if (v == null) {
			v = newVar();
			optionalProperty(anchor, Param.make(sns, spec.substring(where)), v);
			mightBeUnbound.value = true;
			return v;
		} else {
			return generateSortVariable(v, spec, dot + 1, mightBeUnbound);
		}
	}

	@Override public Variable newVar() {
		return RDFQ.var(PREFIX_VAR + varcount++);
	}

	/**
	 * Answer the number of variables allocated so far (used for testing).
	 */
	public int countVarsAllocated() {
		return varcount;
	}

	protected void addNameProp(Param param, String literal) {
		Variable newvar = addPropertyHasValue_REV(param);
		addTriplePattern(newvar, RDFS.label, RDFQ.literal(literal));
	}

	public void addWhere(String whereClause) {
		if (whereExpressions.length() > 0) whereExpressions.append(" ");
		whereExpressions.append(whereClause);
	}

	public String assembleSelectQuery(Bindings b, PrefixMapping prefixes) {
		PrefixLogger pl = new PrefixLogger(prefixes);
		return assembleRawSelectQuery(pl, b);
	}

	public String assembleSelectQuery(PrefixMapping prefixes) {
		PrefixLogger pl = new PrefixLogger(prefixes);
		Bindings cc = Bindings.createContext(new Bindings(), new MultiMap<String, String>());
		return assembleRawSelectQuery(pl, cc);
	}
	
	public String assembleRawSelectQuery(PrefixLogger pl, Bindings b) {
		if (!sortByOrderSpecsFrozen)
			unpackSortByOrderSpecs();
		if (fixedSelect == null) {
			StringBuilder q = new StringBuilder();
			q.append("SELECT ");
			if (orderExpressions.length() > 0) q.append("DISTINCT ");
			q.append(SELECT_VAR.name());
			assembleWherePart(q, b, pl);
			if (orderExpressions.length() > 0) {
				q.append(" ORDER BY ");
				q.append(orderExpressions);
				pl.findPrefixesIn(orderExpressions.toString());
			}
			appendOffsetAndLimit(q);
			// System.err.println( ">> QUERY IS: \n" + q.toString() );
			String bound = bindDefinedvariables(pl, q.toString(), b);
			StringBuilder x = new StringBuilder();
			if (counting()) x.append("# Counting has been applied to this query.\n");
			pl.writePrefixes(x);
			x.append(bound);
			return x.toString();
		} else {
			pl.findPrefixesIn(fixedSelect);
			String bound = bindDefinedvariables(pl, fixedSelect, b);
			StringBuilder sb = new StringBuilder();
			pl.writePrefixes(sb);
			sb.append(bound);
			appendOffsetAndLimit(sb);
			return sb.toString();
		}
	}

	private void assembleWherePart(StringBuilder q, Bindings b, PrefixLogger pl) {
		q.append("\nWHERE {\n");
		String graphName = expandGraphName(b);
		if (graphName != null) q.append("GRAPH <" + graphName + "> {" );
		String bgp = constructBGP(pl);
		if (whereExpressions.length() > 0) {
			q.append(whereExpressions);
			pl.findPrefixesIn(whereExpressions.toString());
		} else {
			if (basicGraphTriples.isEmpty())
				bgp = SELECT_VAR.name() + " ?__p ?__v .\n" + bgp;
		}
		q.append(bgp);
		appendFilterExpressions(pl, q);
		if (graphName != null) q.append("} ");
		q.append("} ");
	}

	private String expandGraphName(Bindings b) {
		String toExpand = graphName;
		if (toExpand == null) toExpand = graphTemplate;
		return toExpand == null ? null : b.expandVariables(toExpand);
	}

	private void appendOffsetAndLimit(StringBuilder q) {
		q.append(" OFFSET " + (pageNumber * pageSize));
		q.append(" LIMIT " + pageSize);
	}

	public void appendFilterExpressions(PrefixLogger pl, StringBuilder q) {
		for (RenderExpression i : filterExpressions) {
			q.append(" FILTER (");
			i.render(pl, q);
			q.append(")\n");
		}
	}

	public String constructBGP(PrefixLogger pl) {
		StringBuilder sb = new StringBuilder();
		for (RDFQ.Triple t : QuerySupport.reorder(basicGraphTriples, textSearchConfig.placeEarly()))
			sb.append(t.asSparqlTriple(pl)).append(" .\n");
		for (List<RDFQ.Triple> optional : optionalGraphTriples) {
			sb.append("OPTIONAL { ");
			for (RDFQ.Triple t : optional) {
				sb.append(t.asSparqlTriple(pl)).append(" . ");
			}
			sb.append("}\n");
		}
		return sb.toString();
	}

	/**
	 * Take the SPARQL query string <code>query</code> and replace any ?SPOO
	 * where SPOO is a variable bound in <code>cc</code> with the SPARQL
	 * representation of that variable's value. Note that this will include
	 * <i>any</i> occurrences of ?SPOO, including those inside SPARQL quotes.
	 * Fixing this probably should happen earlier, but note that bits of query
	 * are mashed together from strings in the config file, ie, without going
	 * through RDFQ.
	 */
	protected String bindDefinedvariables(PrefixLogger pl, String query, Bindings cc) {
		// System.err.println( ">> query is: " + query );
		// System.err.println( ">> VarValues is: " + cc );
		StringBuilder result = new StringBuilder(query.length());
		Matcher m = varPattern.matcher(query);
		int start = 0;
		while (m.find(start)) {
			result.append(query.substring(start, m.start()));
			String name = m.group().substring(1);
			Value v = cc.get(name);
			if (v == null || v.spelling() == null) {
				result.append(m.group());
			} else {
				Info prop = varInfo.get(RDFQ.var("?" + name));
				String val = v.spelling();
				String normalizedValue = (prop == null) 
					? valueAsSparql(v, pl)
					: objectForValue(prop, val, defaultLanguage).asSparqlTerm(pl)
					;

				result.append(normalizedValue);
			}
			start = m.end();
		}
		result.append(query.substring(start));
		return result.toString();
	}

	private String valueAsSparql(Value v, PrefixLogger pl) {
		return 
			v.type().equals(RDFS.Resource.getURI()) 
				? "<" + v.spelling() + ">" 
				: v.asSparqlTerm(pl)
			;
	}

	/**
	 * Return the select query that would be run or a plain string for the
	 * resource
	 */
	public String getQueryString(APISpec spec, Bindings call) {
		return isFixedSubject() && isItemEndpoint 
			? "<"+ subjectResource.getURI() + ">" 
			: assembleSelectQuery(call,	spec.getPrefixMap())
			;
	}

	/**
	 * Run the defined query against the datasource
	 */
	public APIResultSet runQuery(NoteBoard nb, Controls c, APISpec spec, Cache cache, Bindings b, View view) {
		Source source = spec.getDataSource();
		try {
			nb.expiresAt = viewSensitiveExpiryTime(spec, view);
			Integer totalCount = requestTotalCount(nb.expiresAt, c, cache, source, b, spec.getPrefixMap());
			nb.totalResults = totalCount;
			String graphName = expandGraphName(b);
			return runQueryWithSource(nb, c, spec, b, graphName, view, source);
		} catch (QueryExceptionHTTP e) {
			EldaException.ARQ_Exception(source, e);
			return /* NEVER */null;
		}
	}

	// may be subclassed
	protected APIResultSet runQueryWithSource
		( NoteBoard nb, Controls c, APISpec spec, Bindings call, String graphName, View view, Source source) {
	//
		Times t = c.times;
		long origin = System.currentTimeMillis();
		Couple<String, List<Resource>> queryAndResults = selectResources(c, spec, call, source);
		long afterSelect = System.currentTimeMillis();
	//
		t.setSelectionDuration(afterSelect - origin);
		String outerSelect = queryAndResults.a;
		List<Resource> results = queryAndResults.b;
	//
		APIResultSet rs = fetchDescriptionOfAllResources(c, outerSelect, spec, graphName, view, results);
	//
		long afterView = System.currentTimeMillis();
		t.setViewDuration(afterView - afterSelect);
		rs.setSelectQuery(outerSelect);
	//
		return rs;
	}

	private Integer requestTotalCount(long expiryTime, Controls c, Cache cache, Source s, Bindings b, PrefixMapping pm) {		
		if (counting()) {
			PrefixLogger pl = new PrefixLogger(pm);
			String countQueryString = assembleRawCountQuery(pl, b);
			int already = cache.getCount(countQueryString);
			if (already < 0 || c.allowCache == false) {			
				Query countQuery = createQuery(countQueryString);
				CountConsumer cc = new CountConsumer();
				s.executeSelect( countQuery, cc );
				cache.putCount(countQueryString, cc.count, expiryTime);
				return cc.count;
			} else {				
				return already;
			}
		}
		return null;
	}

	private long viewSensitiveExpiryTime(APISpec spec, View v) {
//		System.err.println( ">> viewSensitiveExpiryTime: basis " + cacheExpiryMilliseconds );
		long duration = v.minExpiryMillis(spec.getPropertyExpiryTimes(), cacheExpiryMilliseconds);
//		System.err.println( ">> computed duration: " + duration );
		long result = nowPlus(duration);
//		System.err.println( ">> the long result of time: " + result );
		return result;
	}

	private long nowPlus(long duration) {
		return duration < 0 ? duration : System.currentTimeMillis() + duration;
	}

	// may be subclassed
	protected APIResultSet fetchDescriptionOfAllResources(Controls c, String select, APISpec spec, String graphName, View view, List<Resource> results) {
		int count = results.size();
		Model descriptions = ModelFactory.createDefaultModel();
		descriptions.setNsPrefixes(spec.getPrefixMap());
		Graph gd = descriptions.getGraph();
		String detailsQuery = results.isEmpty() || results.get(0) == null 
			? "# no results, no query."
			: view.fetchDescriptionsFor(c, select, results, descriptions, spec, this, graphName)
			;
		return new APIResultSet(gd, results, count < pageSize, enableETags, detailsQuery, view);
	}

	/**
	 * Answer the select query (if any; otherwise, "") and list of resources
	 * obtained by running that query.
	 * 
	 * May be subclassed.
	 */
	protected Couple<String, List<Resource>> selectResources(Controls c, APISpec spec, Bindings b, Source source) {
		final List<Resource> results = new ArrayList<Resource>();
		if (itemTemplate != null)
			setSubject(b.expandVariables(itemTemplate));
		if (isFixedSubject() && isItemEndpoint)
			return new Couple<String, List<Resource>>("", CollectionUtils.list(subjectResource));
		else {
			Couple<String, List<Resource>> x = runGeneralQuery(c, spec, b, source, results);	
			return new Couple<String, List<Resource>>(x.a, x.b);
		}
	}

	private Couple<String, List<Resource>> runGeneralQuery
		( Controls c
		, APISpec spec
		, Bindings cc
		, Source source
		, final List<Resource> results
		) {
		String selectQuery = assembleSelectQuery(cc, spec.getPrefixMap());
	//
		c.times.setSelectQuerySize(selectQuery);
	//
		Query q = createQuery(selectQuery);
		if (log.isDebugEnabled()) log.debug("Running query: " + selectQuery.replaceAll("\n", " "));
		source.executeSelect(q, new ResultResourcesReader(results));
		return new Couple<String, List<Resource>>(selectQuery, results );
	}

	private boolean counting() {
		return 
			Boolean.TRUE.equals(totalCountRequested)
			&& isItemEndpoint == false
			;
	}	
	
	static class CountConsumer implements ResultSetConsumer {

		int count = -1;
		
		@Override public void setup(QueryExecution qe) {			
		}

		@Override public void consume(ResultSet rs) {
			QuerySolution qs = rs.next();
			Literal countLiteral = qs.getLiteral("count");
			this.count = countLiteral.getInt();
		}
		
	}
	
	public String assembleRawCountQuery(PrefixLogger pl, Bindings b) {
		if (!sortByOrderSpecsFrozen) unpackSortByOrderSpecs();
		String distinct = (orderExpressions.length() > 0 ? "DISTINCT " : "");
	//
		StringBuilder q = new StringBuilder();
		q.append("SELECT (COUNT(").append(distinct).append(SELECT_VAR.name()).append( ") AS ?count)" );
		assembleWherePart(q, b, pl);
	//
	// We don't need to order the results, since we're just going to count
	// them, and the DISTINCT in the select operates over the entire
	// result-set.
	//
//		if (orderExpressions.length() > 0) {
//			q.append(" ORDER BY ");
//			q.append(orderExpressions);
//			pl.findPrefixesIn(orderExpressions.toString());
//		}
	//
		String bound = bindDefinedvariables(pl, q.toString(), b);
		StringBuilder x = new StringBuilder();
		pl.writePrefixes(x);
		x.append(bound);
		return x.toString();
	}	

	// may be over-ridden in a subclass
	protected Query createQuery(String selectQuery) {
		try {
			return QueryFactory.create(selectQuery);
		} catch (Exception e) {
			String x = e.getMessage();
			throw new APIException("Internal error building query:\n\n" + e.getMessage() + "\nin:\n\n" + selectQuery, e);
		}
	}

	private static final class ResultResourcesReader implements	Source.ResultSetConsumer {

		private final List<Resource> results;

		private ResultResourcesReader(List<Resource> results) {
			this.results = results;
		}

		@Override public void setup(QueryExecution qe) {
			// TODO can this method be deleted?
		}

		@Override public void consume(ResultSet rs) {
			try {
				while (rs.hasNext()) {
					Resource item = rs.next().getResource(SELECT_VAR.name());
					if (item == null) {
						EldaException.BadSpecification
							( "<br>Oops. No binding for "
							+ SELECT_VAR.name()
							+ " in successful SELECT.\n"
							+ "<br>Perhaps ?item was mis-spelled in an explicit api:where clause.\n"
							+ "<br>It's not your fault; contact the API provider."
							);
					}
					results.add(withoutModel(item));
				}
			} catch (APIException e) {
				throw e;
			} catch (Throwable t) {
				throw new APIException("Query execution problem on query: " + t, t);
			}
		}

		private Resource withoutModel(Resource item) {
			return ResourceFactory.createResource(item.getURI());
		}
	}

	@Override public boolean wantsMetadata(String name) {
		return metadataOptions.contains(name)
				|| metadataOptions.contains("all");
	}

	/**
	 * Answer true if <code>name</code> is a reserved name (_whatever) that is
	 * allowed to be used (and ignored).
	 */
	public boolean allowReserved(String name) {
		return name.equals("_") || allowedReserved.contains(name);
	}

}
