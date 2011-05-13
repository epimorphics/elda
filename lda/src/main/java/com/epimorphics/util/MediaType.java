/*
	See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
	for the licence for this software.

	(c) Copyright 2011 Epimorphics Limited
	$Id$
*/
package com.epimorphics.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

/**
    Minimal representation of media types, just enough for what we need
    to do.
    
 	@author chris
*/
public class MediaType
	{
	public static final MediaType STAR_STAR = new MediaType( "*", "*" );
	
	public static final MediaType TEXT_XML = new MediaType( "text", "xml" );
	
	public static final MediaType TEXT_HTML = new MediaType( "text", "html" );
	
	public static final MediaType TEXT_PLAIN = new MediaType( "text", "plain" );
	
	public static final MediaType APPLICATION_JSON = new MediaType( "application", "json" );
	
	public static final MediaType TEXT_TURTLE = new MediaType( "text", "turtle" );
	
	public static final MediaType APPLICATION_RDF_XML = new MediaType( "application", "rdf+xml" );

	public MediaType( String A, String B, float Q )
		{ this.type = A; this.subType = B; this.Q = Q; }
	
	public MediaType( String A, String B )
		{ this( A, B, 1.0f ); }
	
	final String type;
	final String subType;
	final float Q;
	
	static final Comparator<? super MediaType> compareMT = new Comparator<MediaType>() 
		{
		@Override public int compare( MediaType a, MediaType b ) 
			{
			if (a.Q > b.Q) return -1;
			if (a.Q > b.Q) return +1;
			if (a.type.equals( "*" )) return -1;
			if (b.type.equals( "*" )) return +1;
			if (a.subType.equals( "*" )) return -1;
			if (b.subType.equals( "*" )) return +1;
			return 0;
			}
		};

	public String getType()
		{ return type; }
	
	public String getSubtype()
		{ return subType; }
	
	@Override public int hashCode()
		{ return type.hashCode() ^ subType.hashCode(); }
	
	@Override public boolean equals( Object other ) 
		{ return other instanceof MediaType && same( (MediaType) other ); }
	
	private boolean same( MediaType other ) 
		{ return type.equals(other.type) && subType.equals(other.subType); }

	public boolean accepts( MediaType s ) 
		{
		return (type.equals("*") || type.equals( s.type )) && (subType.equals("*") || subType.equals( s.subType ));
		}
	
	/**
	    Answer the string type/subType, ignoring the Q-value.
	*/
	@Override public String toString()
		{ return type + "/" + subType; }
	
	/**
	    Answer the string type/subType; q=Q.
	*/
	public String toFullString()
		{ return type + "/" + subType + "; q=" + Q; }

	/**
	 * 
	 * @param types
	 * @param canHandle
	 * @return
	 */
	public static String accept( List<MediaType> types, String canHandle ) 
		{
		List<MediaType> served = decodeTypes( canHandle );
		for (MediaType t: types)
			for (MediaType s: served)
				if (t.accepts( s )) return s.type + "/" + s.subType;
		return null;
		}

	public static List<MediaType> mediaType( Enumeration<String> e ) 
		{
		List<MediaType> types = new ArrayList<MediaType>();
		while (e.hasMoreElements()) types.addAll( decodeTypes( e.nextElement() ) );
		Collections.sort( types, MediaType.compareMT );
		return types;
		}

	public static List<MediaType> decodeTypes( String a ) 
		{
		List<MediaType> result = new ArrayList<MediaType>();
		if (a.length() > 0)
			for (String one: a.split( " *, *" ))
				result.add( decodeType( one ) );
		return result;
		}

	public static MediaType decodeType( String one )
		{
		float Q = 1.0f;
		String[] X = one.split( " *; *" );
		for (int i = 1; i < X.length; i += 1)
			if (X[i].startsWith("q="))
				Q = Float.parseFloat( X[1].substring(2));
		String [] AB = X[0].split( "/" );
		return new MediaType( AB[0], AB[1], Q );
		}
	}