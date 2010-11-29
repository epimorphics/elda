/*
    See lda-top/LICENCE for the licence for this software.
    (c) copyright Epimorphics Limited 2010
    $Id$
*/

package com.epimorphics.lda.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.core.APIQuery.Param;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.hp.hpl.jena.shared.NotFoundException;

/**
    A ContextQueryUpdater is used to update an APIQuery according to context
    parameters, including geospatial ones, and to deliver the view
    specified by the context.
 
    @author chris
*/
public class ContextQueryUpdater {
	
	private final CallContext context;
	private final APIQuery query;
	private final ShortnameService sns;
	private final NamedViews nt;
	
	private View view; // = new view(); -- doesn't work, null is important somewhere
	protected final View defaultView;
	protected final View noneSpecified;

    static Logger log = LoggerFactory.getLogger(APIEndpointImpl.class);
    
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
	}
	
	/**
	    Apply the context updates to the query, and answer the view
	    specified.
	*/
    public View updateQueryAndConstructView() {	  
    	query.activateDeferredFilters( context );
        GEOLocation geo = new GEOLocation();
        for (String param: context.getFilterPropertyNames()) 
            handleParam( geo, Param.make( param ) );
        geo.addLocationQueryIfPresent( query );
        return view == noneSpecified ? defaultView : view;
    }

	private void handleParam(GEOLocation geo, Param p) {
		String zpString = p.asString();
		String val = context.expand( context.getParameterValue(zpString) );
		String pString = context.expand( zpString );
	//
		if (val == null)
			throw new RuntimeException( "value for " + p + " is null." );
//		System.err.println( ">> pString=" + pString + ", value=" + val );
	//
		if (query.isBindable(pString)) 
			{ /* nothing to do -- report suspect? */ } 
		else if (p.is(APIQuery.TEMPLATE_PARAM)) {
			setViewByProperties(val);
		} else if (p.is(APIQuery.SHOW_PARAM)) {
		    setViewByName(val);
		} else if (p.is(APIQuery.WHERE_PARAM)) {
		    query.addWhere(val);
		} else if (p.is(APIQuery.SUBJECT_PARAM)) {
		    query.setSubject(val);
		} else if (p.is( APIQuery.NEAR_LAT)) { 
			geo.setNearLat( val );
		} else if (p.is( APIQuery.NEAR_LONG )) {
			geo.setNearLong( val );
		} else if (p.is( APIQuery.DISTANCE )) { 
			geo.setDistance( val );
		} else if (p.is( APIQuery._TEMPLATE )) {
			setViewByExplicitClause( val );
			query.setViewByTemplateClause( val );
		} else {
			log.info( "handleParam: " + p + " with value: " + val );
//			System.err.println( ">> handleParam: " + p + " with value: " + val );
			query.addFilterFromQuery( Param.make( pString ), val );
		}
	}

	/**
	    Half-baked construction of a view given a clause.
	    Make that tenth-baked. Maybe epsilon-baked.
	*/
	private void setViewByExplicitClause( String clause ) {
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

	private void setViewByName( String viewName ) {
		// view = view.addFrom( nt.getView( viewName ) );
//		System.err.println( ">> setViewByName from " + viewName + " given current view " + view );
		View named  = nt.getView( viewName );
		if (named == null) throw new NotFoundException( "view " + viewName );
//		System.err.println( ">> named view is " + named );
		view = named.copy().addFrom( view );
//		System.err.println( ">> result is " + view );
		log.info( "view " + viewName + " yields view " + view + " from\n  " + nt );
	}

	private void setViewByProperties(String val) {
		view = nt.getDefaultView().copy();
		for (String prop: val.split(","))
			view.addViewFromParameterValue( prop, query, sns );
//		System.err.println( ">> >> setViewByProperties from " + val + " => " + view );
	}
}