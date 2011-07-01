/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.core;

import static com.epimorphics.util.CollectionUtils.set;

import java.util.Set;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.util.CollectionUtils;
import com.epimorphics.util.Couple;
import com.epimorphics.lda.core.APIQuery.Deferred;
import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.rdfq.Any;
import com.epimorphics.lda.rdfq.RDFQ;
import com.epimorphics.lda.rdfq.Variable;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.hp.hpl.jena.rdf.model.Resource;

/**
    A ContextQueryUpdater is used to update an APIQuery according to context
    parameters, including geospatial ones, and to deliver the view
    specified by the context.
 
    @author chris
*/
public class ContextQueryUpdater implements ViewSetter {
	
	private final CallContext context;
	private final APIQuery query;
	private final ShortnameService sns;
	private final NamedViews nt;
	
	private View view; // = new view(); -- doesn't work, null is important somewhere
	protected final View defaultView;
	protected final View noneSpecified;
	
	protected String requestedFormat = "";

    static Logger log = LoggerFactory.getLogger(APIEndpointImpl.class);
    
    public interface Q {

		Q addSubjectHasProperty(Resource lat, Variable latVar);

		Q addNumericRangeFilter(Variable latVar, double lat, double deltaLat);

		Variable newVar();
    	
    }
    
    public static class QQ implements Q {
    	final APIQuery query;
    	
    	public QQ(APIQuery query) {
    		this.query = query;
    	}

    	@Override public QQ addSubjectHasProperty(Resource lat, Variable latVar) {
    		query.addSubjectHasProperty( lat, latVar );
			return this;
		}

		@Override public QQ addNumericRangeFilter(Variable latVar, double lat, double deltaLat) {
			query.addInfixSparqlFilter( RDFQ.literal( lat - deltaLat ), "<", latVar );
			   query.addInfixSparqlFilter( latVar, "<", RDFQ.literal( lat + deltaLat) );
			return this;
		}

		@Override public Variable newVar() {
			return query.newVar();
		}
    }
    
    protected final QQ qq;
    
	/**
		Initialise this ContextQueryUpdater.
		
	    @param context the context providing the parameters and their values
	    @param nv a mapping from names to views, with a default
	    @param sns the ShortnameService to use to translate to URIs
	    @param query the APIQuery to update
	*/
	public ContextQueryUpdater( CallContext context, NamedViews nv, ShortnameService sns, APIQuery query ) {
		this.context = context;
		this.query = query;
		this.sns = sns;
		this.nt = nv;
		this.defaultView = nv.getDefaultView().copy();
		this.view = this.noneSpecified = new View();
		this.qq = new QQ(query);
	}
	
	/**
	    Apply the context updates to the query, and answer the view
	    specified.
	*/
    public Couple<View, String> updateQueryAndConstructView() {	  
    	activateDeferredFilters();
    	query.clearLanguages();
    	for (String param: context.getFilterPropertyNames()) 
    		if (param.startsWith( QueryParameter.LANG_PREFIX ))
    			handleLangPrefix( param );
        GEOLocation geo = new GEOLocation();
        for (String param: context.getFilterPropertyNames()) 
            handleParam( geo, param );
        geo.addLocationQueryIfPresent( qq );
        return new Couple<View, String>( (view == noneSpecified ? defaultView : view), requestedFormat );
    }

	private void activateDeferredFilters() {
		for (Deferred d: query.deferredFilters) {
			APIQuery.log.debug( "activating deferred filter " + d );
			addFilterFromQuery( d.param.expand( context ), set(context.expandVariables( d.val )) );
		}
	}

	private void handleLangPrefix( String taggedParam ) {
		String param = taggedParam.substring( QueryParameter.LANG_PREFIX.length() );
		String val = context.expandVariables( context.getStringValue( param ) );
		String pString = context.expandVariables( param );
		query.setLanguagesFor( pString, val );
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
		if (query.isBindable(pString)) {
			// nothing to do -- report suspect?  
		} else if (p.startsWith( QueryParameter.LANG_PREFIX )) {
			// Nothing to do -- done on previous pass 
		} else if (p.startsWith("_") || p.equals("near-lat") || p.equals("near-long")) {
			query.handleReservedParameters( geo, this, p, val );
		} else {
			log.debug( "handleParam: " + p + " with value: " + val );
			addFilterFromQuery( Param.make( sns, pString ), allVal );
		}
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
    		query.addPropertyHasValue( param, allVal );    		
    	} else if (prefix.equals(QueryParameter.NAME_PREFIX)) {
            query.addNameProp(param.plain(), val);
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
            if (val.equals( "true" )) query.addPropertyHasValue( param );
            else if (val.equals( "false" )) query.addPropertyHasntValue( param );
            else EldaException.BadBooleanParameter( param.toString(), val );
        } else {
        	throw new EldaException( "unrecognised parameter prefix: " + prefix );
        }
        return param.lastPropertyOf();
    }
    
    protected void addRangeFilter( Param param, String val, String op ) {
        Variable newvar = query.newVar(); 
        String prop = addFilterFromQuery( param, CollectionUtils.set(newvar.name()) );
        addInfixSparqlFilter( newvar, op, sns.normalizeNodeToRDFQ( prop, val, query.defaultLanguage ) );
    }
    
    public void addInfixSparqlFilter( Any l, String op, Any r ) {
    	query.filterExpressions.add( RDFQ.infix( l, op, r ) );
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
		log.info( "view " + viewName + " yields view " + view + " from\n  " + nt );
	}

	@Override public void setViewByProperties(String val) {
		view = nt.getDefaultView().copy();
		for (String prop: val.split(","))
			view.addViewFromParameterValue( prop, query, sns );
//		System.err.println( ">> >> setViewByProperties from " + val + " => " + view );
	}
}