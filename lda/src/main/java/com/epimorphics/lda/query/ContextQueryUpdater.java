/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.query;

import static com.epimorphics.util.CollectionUtils.set;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.util.CollectionUtils;
import com.epimorphics.util.Couple;
import com.epimorphics.lda.core.APIEndpointImpl;
import com.epimorphics.lda.core.CallContext;
import com.epimorphics.lda.core.NamedViews;
import com.epimorphics.lda.core.Param;
import com.epimorphics.lda.core.View;
import com.epimorphics.lda.core.ViewSetter;
import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.query.APIQuery.Deferred;
import com.epimorphics.lda.rdfq.Any;
import com.epimorphics.lda.rdfq.Variable;
import com.epimorphics.lda.shortnames.ShortnameService;

/**
    A ContextQueryUpdater is used to update an APIQuery according to context
    parameters, including geospatial ones, and to deliver the view
    specified by the context.
 
    @author chris
*/
public class ContextQueryUpdater implements ViewSetter {
	
	private final CallContext context;
	private final ShortnameService sns;
	private final NamedViews nt;
	private final ExpansionPoints eps;
	protected final QueryArguments args;
	protected final int endpointKind;
	
	private View view; // = new view(); -- doesn't work, null is important somewhere
	// protected final View defaultView;
	// protected final View noneSpecified;
	
	protected String requestedFormat = "";

    static Logger log = LoggerFactory.getLogger(APIEndpointImpl.class);
    
    public static final int ItemEndpoint = 1;
    public static final int ListEndpoint = 2;
    
	/**
		Initialise this ContextQueryUpdater.
		
	    @param context the context providing the parameters and their values
	    @param nv a mapping from names to views, with a default
	    @param sns the ShortnameService to use to translate to URIs
	    @param eps possibly dead: expansion points
	    @param args place to build the query arguments
	*/
	public ContextQueryUpdater( int endpointKind, CallContext context, NamedViews nv, ShortnameService sns, ExpansionPoints eps, QueryArguments args ) {
		this.context = context;
		this.sns = sns;
		this.nt = nv;
		this.eps = eps;
		// this.defaultView = nv.getDefaultView().copy();
		this.view = nv.getDefaultView().copy();
		this.endpointKind = endpointKind;
		this.args = args;
	}
	
	/**
	    Apply the context updates to the query, and answer the view
	    specified.
	*/
    public Couple<View, String> updateQueryAndConstructView( List<Deferred> deferredFilters ) {	  
    	args.clearLanguages();
    	Set<String> allParamNames = context.getFilterPropertyNames();
    	if (allParamNames.contains( QueryParameter._VIEW )) {
    		view = new View( "empty, will be replaced" );
    	}
		for (String param: allParamNames) 
    		if (param.startsWith( QueryParameter.LANG_PREFIX ))
    			handleLangPrefix( param );
    		else if (param.equals(QueryParameter._LANG)) 
    			args.setDefaultLanguage( context.getStringValue( param ) );
        GEOLocation geo = new GEOLocation();
        for (String param: allParamNames) 
            handleParam( geo, param );
        geo.addLocationQueryIfPresent( args );
        // ((QueryArgumentsImpl) args).updateQuery();
        activateDeferredFilters( deferredFilters );
        return new Couple<View, String>( view, requestedFormat );
    }

	private void activateDeferredFilters( List<Deferred> deferred ) {
		for (Deferred d: deferred) {
			APIQuery.log.debug( "activating deferred filter " + d );
			addFilterFromQuery( d.param.expand( context ), set(context.expandVariables( d.val )) );
		}
	}

	private void handleLangPrefix( String taggedParam ) {
		String param = taggedParam.substring( QueryParameter.LANG_PREFIX.length() );
		String sv = context.getStringValue( taggedParam );
		if (sv == null) {
			log.debug( taggedParam + " supplied, but no value for " + param );
			return;
		}
		String val = context.expandVariables( sv );
		String pString = context.expandVariables( param );
		args.setLanguagesFor( pString, val );
	}
	
	private Set<String> expandVariables( Set<String> s ) {
		Set<String> result = new HashSet<String>(s.size());
		for (String x: s) result.add( context.expandVariables( x ) );
		return result;
	}

	private void handleParam( GEOLocation geo, String p ) {
		Set<String> values = context.getStringValues(p);
		Set<String> allVal = expandVariables( values );
		String val = allVal.iterator().next();
		String pString = context.expandVariables( p );
		if (val == null) EldaException.NullParameter( p );
	//
		if (p.startsWith( QueryParameter.LANG_PREFIX )) {
			// Nothing to do -- done on previous pass 
	    } else if (p.equals(QueryParameter._LANG)) {
			// Also done on previous pass
		} else if (isReserved(p)) {
			handleReservedParameters( geo, this, p, val );
		} else {
			log.debug( "handleParam: " + p + " with value: " + val );
			contemplation( pString, allVal );
			addFilterFromQuery( Param.make( sns, pString ), allVal );
		}
	}

	private void contemplation( String pString, Set<String> allVal ) {
		// HERE
		if (false) {
			System.err.println( ">> contemplating " + pString + "=" + allVal );
		}
	}

	private boolean isReserved(String p) {
		return 
			p.startsWith("_") 
			|| p.equals("near-lat") || p.equals("near-long") 
			|| p.equals( QueryParameter.callback )
			;
	} 
	
	static final Pattern callbackPattern = Pattern.compile( "^[a-zA-Z_][a-zA-Z0-9]*$" );
	
    /**
	    handle the reserved, ie, _wossname, parameters. These may update
	    the given <code>geo</code>, the <code>vs</code>, or this query
	    object. <code>p</code> is the reserved-property name, <code>val</code>
	    is the value string.
	*/
	public void handleReservedParameters( GEOLocation geo, ViewSetter vs, String p, String val ) {
		if (p.equals(QueryParameter._PAGE)) {
			mustBeListEndpoint( p );
		    args.setPageNumber( positiveInteger( p, val ) ); 
		} else if (p.equals(QueryParameter._PAGE_SIZE)) {
			mustBeListEndpoint( p );
		    args.setPageSize( positiveInteger( p, val ) );
		} else if (p.equals( QueryParameter._FORMAT )) {
			vs.setFormat(val);
		} else if (p.equals(QueryParameter._METADATA)) {
			args.addMetadataOptions( val.split(",") );
	    } else if (p.equals(QueryParameter._SEARCH)) {
	        args.addSearchTriple( val );
	    } else if (p.equals(QueryParameter._SELECT_PARAM )) {
	    	args.setFixedSelect( val );
	    } else if (p.equals(QueryParameter._WHERE)) {
	    	args.addWhere( val );
		} else if (p.equals(QueryParameter._PROPERTIES)) {
			vs.setViewByProperties(val);
		} else if (p.equals(QueryParameter._VIEW)) {
		    vs.setViewByName(val);
		} else if (p.equals(QueryParameter._SUBJECT)) {
		    args.setSubject(val);
		} else if (p.equals( QueryParameter.callback )) {
			if (!callbackPattern.matcher( val ).matches())
				throw new EldaException( "illegal callback name", val, EldaException.BAD_REQUEST );
		} else if (p.equals( APIQuery.NEAR_LAT)) { 
			geo.setNearLat( val );
		} else if (p.equals( APIQuery.NEAR_LONG )) {
			geo.setNearLong( val );
		} else if (p.equals( QueryParameter._DISTANCE )) { 
			geo.setDistance( val );
		} else if (p.equals( QueryParameter._TEMPLATE )) {
			args.setViewByTemplateClause( val );
		} else if (p.equals(QueryParameter._SORT)) {
		    args.setSortBy( val );
		} else if (p.equals(QueryParameter._ORDERBY )) {
			args.setOrderBy( val );
		} else {
			EldaException.BadRequest( "unrecognised reserved parameter: " + p );
			throw new EldaException( "Can never get here!" );
		}
	}	
	
	private void mustBeListEndpoint( String p ) {
		if (endpointKind != ListEndpoint)
			EldaException.BadRequest( p + " can only be used with a list endpoint." );
	}

	private int positiveInteger( String param, String val ) {
		try {
			int result = Integer.parseInt( val );
			if (0 <= result) return result;
		} catch (NumberFormatException e) { /* fall-through */ }
		throw new EldaException( param + "=" + val, "value must be non-negative integer.", EldaException.BAD_REQUEST );
	}

	/**
     * General interface for extending the query with a specified parameter.
     * This parameter types handled include _page, _orderBy, min-, name- and path parameters.
     * @return the name of the final property referencing the val, to allow type sensitive normalization
    */
    public void addFilterFromQuery( Param param, Set<String> allVal ) {
    	String val = allVal.iterator().next();
    	if (val.equals( "" )) {
    		log.debug( "parameter " + param + " given empty value." );
    		return;
    	}
    	String prefix = param.prefix();
    	if (prefix == null) {
    		args.addPropertyHasValue( param, allVal );    		
    	} else if (prefix.equals(QueryParameter.NAME_PREFIX)) {
            args.addNameProp(param.plain(), val);
        } else if (prefix.equals( QueryParameter.LANG_PREFIX )) {
        	// handled elsewhere
        } else if (prefix.equals(QueryParameter.MIN_PREFIX)) {
            addRangeFilter(param.plain(), val, ">=");
        } else if (prefix.equals(QueryParameter.MIN_EX_PREFIX)) {
            addRangeFilter(param.plain(), val, ">");
        } else if (prefix.equals(QueryParameter.MAX_PREFIX)) {
            addRangeFilter(param.plain(), val, "<=");
        } else if (prefix.equals(QueryParameter.MAX_EX_PREFIX)) {
            addRangeFilter(param.plain(), val, "<");
        } else if (prefix.equals(QueryParameter.EXISTS_PREFIX)) {
            if (val.equals( "true" )) args.addPropertyHasValue( param );
            else if (val.equals( "false" )) args.addPropertyHasntValue( param );
            else EldaException.BadBooleanParameter( param.toString(), val );
        } else {
        	throw new EldaException( "unrecognised parameter prefix: " + prefix );
        }
    }
    
    protected final Map<String,Variable> seenParamVariables = new HashMap<String, Variable>();
    
    public static final boolean dontSquishVariables = false;
    
    protected void addRangeFilter( Param param, String val, String op ) {
    	Variable already = seenParamVariables.get(param.asString());
    	String prop = param.lastPropertyOf();
    	if (already == null || dontSquishVariables) {
	        seenParamVariables.put( param.asString(), already = args.newVar() );
	        args.addPropertyHasValue( param, CollectionUtils.set(already.name() ) );
    	}
	    Any r = sns.normalizeNodeToRDFQ( prop, val, args.getDefaultLanguage() );
		args.addInfixSparqlFilter( already, op, r );
    }    

	/**
	    Half-baked construction of a view given a clause.
	    Make that tenth-baked. Maybe epsilon-baked.
	*/
	@Override public void setViewByExplicitClause( String clause ) {
		view = new View();
		String [] parts = clause.replaceAll( "[\\[\\],;]", " " ).split( " +" );
		for (String p: parts) {
			if (!p.startsWith("?")) {
				String prop = p.replaceFirst( "^.*[/#:]", "" );
				view.addViewFromParameterValue( prop, null, sns );
			}
		}
	}
	
	@Override public void setFormat( String format ) {
		requestedFormat = format;
	}

	@Override public void setViewByName( String viewName ) {
		View named  = nt.getView( viewName );
		if (named == null) EldaException.NotFound( "view", viewName );
		view = named.copy().addFrom( view );
	}

	@Override public void setViewByProperties(String val) {
//		System.err.println( ">> setViewByProperties: base is " + view );
		view = view.copy(); 
		for (String prop: val.split(","))
			if (prop.length() > 0) 
				view.addViewFromParameterValue( prop, eps, sns );
//		System.err.println( ">> ... view becomes " + view );
	}
}