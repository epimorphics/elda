/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) copyright Epimorphics Limited 2010
    $Id$
*/
package com.epimorphics.lda.support;

import java.util.List;

import javax.ws.rs.core.MediaType;

/**
    Support code for turning a list of media types into a comma-separated
    string, discarding the wimpy type "/".
    
 	@author chris
*/
public class MediaTypeSupport 
	{
	public static final MediaType UNSPECIFIED = new MediaType( "", "" );

	/**
	    Answer a string which is a comma-separated sequence of the X/Y
	    media type strings of the elements of the mediaTypes list. Ignore
	    the media type "/".
	*/
	public static String mediaTypeString(List<MediaType> mediaTypes) {
		StringBuilder result = new StringBuilder();
		String prefix = "";
		for (MediaType mt: mediaTypes)
			if (!mt.equals( UNSPECIFIED )) 
				{
				result.append( prefix ).append( mt.toString() );
				prefix = ", ";
				}
		return result.toString();
		}

	}
