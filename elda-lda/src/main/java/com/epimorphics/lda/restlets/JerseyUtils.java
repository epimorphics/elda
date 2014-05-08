/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.restlets;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.support.MultiMap;
import com.epimorphics.util.MediaType;

public class JerseyUtils {

    protected static Logger log = LoggerFactory.getLogger(JerseyUtils.class);
    
	/**
	    Convert a (rs.core) MultivaluedMap to (our, local) MultiMap.
	    This is just to allow bridging from Jersey restlets into our
	    (eventually, Jersey-free internally) code.
	    
	    <p>
	    Empty parameters are silently ignored.
	    </p>
	*/
	public static MultiMap<String, String> convert( MultivaluedMap<String, String> map ) {
		MultiMap<String, String> result = new MultiMap<String, String>();
		for (String key: map.keySet()) {
			for (String value: map.get(key)) {
				if (!value.equals("")) result.add(key, value);
			}
		}
		return result;
	}

	/**
	 	Translate the Jersey media types into Elda media types (because there will
	 	be Jersey-less versions of Elda). Also, if text/html is present, prefer it
	 	regardless of the given order, for those browsers still out there that
	 	"prefer" XML to HTML. They may, but their readers don't.
	*/
	public static List<MediaType> getAcceptableMediaTypes( HttpHeaders headers ) {
		boolean preferHTML = false;
		List<MediaType> mediaTypes = new ArrayList<MediaType>();
		for (javax.ws.rs.core.MediaType mt: headers.getAcceptableMediaTypes()) {
			MediaType newMT = new MediaType( mt.getType(), mt.getSubtype() );
			if (newMT.equals( MediaType.TEXT_HTML)) preferHTML = true;
			else mediaTypes.add( newMT );
		}
		if (preferHTML) mediaTypes.add( 0, MediaType.TEXT_HTML );
		return mediaTypes;
	}

}
