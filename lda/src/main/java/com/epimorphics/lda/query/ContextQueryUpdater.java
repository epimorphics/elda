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
	
	private View view; // = new view(); -- doesn't work, null is important somewhere
	protected final View defaultView;
	protected final View noneSpecified;
	
	protected String requestedFormat = "";

    static Logger log = LoggerFactory.getLogger(APIEndpointImpl.class);
    
    
	/**
		Initialise this ContextQueryUpdater.
		
	    @param context the context providing the parameters and their values
	    @param nv a mapping from names to views, with a default
	    @param sns the ShortnameService to use to translate to URIs
	    @param eps possibly dead: expansion points
	    @param args place to build the query arguments
	*/
	public ContextQueryUpdater( CallContext context, NamedViews nv, ShortnameService sns, ExpansionPoints eps, QueryArguments args ) {
		this.context = context;
		this.sns = sns;
		this.nt = nv;
		this.eps = eps;
		this.defaultView = nv.getDefaultView().copy();
		this.view = this.noneSpecified = new View();
		this.args = args;
	}
	
	/**
	    Apply the context updates to the query, and answer the view
	    specified.
	*/
    public Couple<View, String> updateQueryAndConstructView( List<Deferred> deferredFilters ) {	  
    	activateDeferredFilters( deferredFilters );
    	args.clearLanguages();
    	for (String param: context.getFilterPropertyNames()) 
    		if (param.startsWith( QueryParameter.LANG_PREFIX ))
    			handleLangPrefix( param );
        GEOLocation geo = new GEOLocation();
        for (String param: context.getFilterPropertyNames()) 
            handleParam( geo, param );
        geo.addLocationQueryIfPresent( args );
        // ((QueryArgumentsImpl) args).updateQuery();
        return new Couple<View, String>( (view == noneSpecified ? defaultView : view), requestedFormat );
    }

	private void activateDeferredFilters( List<Deferred> deferred ) {
		for (Deferred d: deferred) {
			APIQuery.log.debug( "activating deferred filter " + d );
			addFilterFromQuery( d.param.expand( context ), set(context.expandVariables( d.val )) );
		}
	}

	private void handleLangPrefix( String taggedParam ) {
		String param = taggedParam.substring( QueryParameter.LANG_PREFIX.length() );
		String sv = context.getStringValue( param );
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
		if (args.isBindable(pString)) {
			// nothing to do -- report suspect?  
		} else if (p.startsWith( QueryParameter.LANG_PREFIX )) {
			// Nothing to do -- done on previous pass 
		} else if (p.startsWith("_") || p.equals("near-lat") || p.equals("near-long")) {
			handleReservedParameters( geo, this, p, val );
		} else {
			log.debug( "handleParam: " + p + " with value: " + val );
			addFilterFromQuery( Param.make( sns, pString ), allVal );
		}
	} 
	
    /**
	    handle the reserved, ie, _wossname, parameters. These may update
	    the given <code>geo</code>, the <code>vs</code>, or this query
	    object. <code>p</code> is the property shortname, <code>val</code>
	    is the value string.
	*/
	public void handleReservedParameters( GEOLocation geo, ViewSetter vs, String p, String val ) {
		if (p.equals(QueryParameter._PAGE)) {
		    args.setPageNumber( positiveInteger(p, val) ); 
		} else if (p.equals(QueryParameter._PAGE_SIZE)) {
		    args.setPageSize( positiveInteger(p, val) );
		} else if (p.equals( QueryParameter._FORMAT )) {
			vs.setFormat(val);
		} else if (p.equals(QueryParameter._METADATA)) {
			args.addMetadataOptions( val.split(",") );
	    } else if (p.equals(QueryParameter._SEARCH)) {
	        args.addSearchTriple( val );
	    } else if (p.equals(QueryParameter._SELECT_PARAM )) {
	    	args.setFixedSelect( val );
	    } else if (p.equals(QueryParameter._LANG)) {
			args.setDefaultLanguage( val );
	    } else if (p.equals(QueryParameter._WHERE)) {
	    	args.addWhere( val );
		} else if (p.equals(QueryParameter._PROPERTIES)) {
			vs.setViewByProperties(val);
		} else if (p.equals(QueryParameter._VIEW)) {
		    vs.setViewByName(val);
		} else if (p.equals(QueryParameter._SUBJECT)) {
		    args.setSubject(val);
		} else if (p.equals( APIQuery.NEAR_LAT)) { 
			geo.setNearLat( val );
		} else if (p.equals( APIQuery.NEAR_LONG )) {
			geo.setNearLong( val );
		} else if (p.equals( QueryParameter._DISTANCE )) { 
			geo.setDistance( val );
		} else if (p.equals( QueryParameter._TEMPLATE )) {
			// vs.setViewByExplicitClause( val );
			args.setViewByTemplateClause( val );
		} else if (p.equals(QueryParameter._SORT)) {
		    args.setOrderBy( val );
		} else {
			throw new EldaException( "unrecognised reserved parameter: " + p );
		}
	}	
	
	private int positiveInteger( String param, String val ) {
		try {
			int result = Integer.parseInt( val );
			if (0 <= result) return result;
		} catch (NumberFormatException e) { /* fall-through */ }
		throw new EldaException( param + "=" + val + ": value must be non-negative integer" );
	}

	/**
     * General interface for extending the query with a specified parameter.
     * This parameter types handled include _page, _orderBy, min-, name- and path parameters.
     * @return the name of the final property referencing the val, to allow type sensitive normalization
    */
    public String addFilterFromQuery( Param param, Set<String> allVal ) {
    	String val = allVal.iterator().next();
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
        return param.lastPropertyOf();
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
		// view.addviewFromParameterValue( "type", null, sns ); // HACK
//		System.err.println( ">> clause '" + clause + "' generates view " + view );
	}
	
	@Override public void setFormat( String format ) {
		requestedFormat = format;
	}

	@Override public void setViewByName( String viewName ) {
		View named  = nt.getView( viewName );
		if (named == null) EldaException.NotFound( "view", viewName );
		view = named.copy().addFrom( view );
		// log.debug( "view " + viewName + " yields view " + view + " from\n  " + nt );
	}

	@Override public void setViewByProperties(String val) {
		view = nt.getDefaultView().copy();
		for (String prop: val.split(","))
			view.addViewFromParameterValue( prop, eps, sns );
//		System.err.println( ">> >> setViewByProperties from " + val + " => " + view );
	}
}