/*
    See lda-top/LICENCE for the licence for this software.
    (c) copyright Epimorphics Limited 2010
    $Id$
*/

/**
 * 
 */
package com.epimorphics.lda.core;

import com.epimorphics.lda.rdfq.RDFQ;
import com.epimorphics.lda.support.NumericArgUtils;
import com.epimorphics.lda.vocabularies.GEOStub;

/**
    GEOLocation is used in the query-building component to record the values
    of near-lat, near-long, and _distance parameters and to build the
    appropriate query component if any of them are present.
    
 	@author chris

*/
public class GEOLocation
	{
	private String distance = null;
	private String nearLong = null;
	private String nearLat = null;
	
	void addLocationQueryIfPresent( APIQuery query )
		{
	    if (nearLat != null && nearLong != null && distance != null) 
	    	{
			double lat = NumericArgUtils.readDegrees( "near-lat", nearLat );
			double lang = NumericArgUtils.readDegrees( "near-long", nearLong );
			double d = NumericArgUtils.readMiles( distance );
	    //
			double deltaLat = NumericArgUtils.deltaLat( d, lat, lang );
			double deltaLong = NumericArgUtils.deltaLong( d, lat, lang );
		//
		    RDFQ.Variable latVar = query.newVar(), longVar = query.newVar();
		    query
		        .addSubjectHasProperty( GEOStub.LAT, latVar )
		        .addSubjectHasProperty( GEOStub.LONG, longVar )
		        .addNumericRangeFilter( latVar, lat, deltaLat )
		        .addNumericRangeFilter( longVar, lang, deltaLong )
		        ;
	    	}
	    else if (nearLat != null || nearLong != null || distance != null)
	    	{
	    	throw new QueryParseException( "some, but not all, of near-lat, near-long, and _distance supplied" );
	    	}
		}
	
	void setNearLat(String nearLat) 
		{
		if (this.nearLat != null) throw new QueryParseException( "near-lat appears more than once in the URI" );
		this.nearLat = nearLat;
		}
	
	void setNearLong(String nearLong) 
		{			
		if (this.nearLong != null) throw new QueryParseException( "near-long appears more than once in the URI" );
		this.nearLong = nearLong;
		}
	
	void setDistance(String distance) 
		{			
		if (this.distance != null) throw new QueryParseException( "_distance appears more than once in the URI" );
		this.distance = distance;
		}
	}