/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.core;

import java.util.Set;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.util.Couple;
import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.shortnames.ShortnameService;

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
    public Couple<View, String> updateQueryAndConstructView() {	  
    	query.activateDeferredFilters( context );
    	query.clearLanguages();
    	for (String param: context.getFilterPropertyNames()) 
    		if (param.startsWith( APIQuery.LANG_PREFIX ))
    			handleLangPrefix( param );
        GEOLocation geo = new GEOLocation();
        for (String param: context.getFilterPropertyNames()) 
            handleParam( geo, param );
        geo.addLocationQueryIfPresent( query );
        return new Couple<View, String>( (view == noneSpecified ? defaultView : view), requestedFormat );
    }

	private void handleLangPrefix( String taggedParam ) {
		String param = taggedParam.substring( APIQuery.LANG_LEN );
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
		} else if (p.startsWith( APIQuery.LANG_PREFIX )) {
			// Nothing to do -- done on previous pass 
		} else if (p.startsWith("_") || p.equals("near-lat") || p.equals("near-long")) {
			query.handleReservedParameters( geo, this, p, val );
		} else {
			log.debug( "handleParam: " + p + " with value: " + val );
			query.addFilterFromQuery( Param.make( sns, pString ), allVal );
		}
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