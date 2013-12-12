/*
	See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
	for the licence for this software.

	(c) Copyright 2011 Epimorphics Limited
	$Id$
*/
package com.epimorphics.util;

import java.util.*;

/**
    Minimal representation of media types, just enough for what we need
    to do.
    
 	@author chris
*/
public class MediaType
	{
	public static final MediaType NONE = new MediaType( "", "" );
	
	public static final MediaType STAR_STAR = new MediaType( "*", "*" );
	
	public static final MediaType TEXT_XML = new MediaType( "text", "xml" );
	
	public static final MediaType TEXT_HTML = new MediaType( "text", "html", "; charset=utf-8" );
	
	public static final MediaType TEXT_PLAIN = new MediaType( "text", "plain" );
	
	public static final MediaType APPLICATION_JSON = new MediaType( "application", "json" );

	public static final MediaType APPLICATION_XML = new MediaType( "application", "xml" );
	
	public static final MediaType TEXT_TURTLE = new MediaType( "text", "turtle", "; charset=utf-8" );
	
	public static final MediaType APPLICATION_RDF_XML = new MediaType( "application", "rdf+xml" );

	// I'm told that application/javascript doesn't work on some IEs ...
	public static final MediaType TEXT_JAVASCRIPT = new MediaType( "text", "javascript" );

	public static final MediaType APPLICATION_JAVASCRIPT = new MediaType( "application", "javascript" );
	
	/**
	    Initialise this MediaType with the given type, subtype, and
	    Q-value.
	*/	
	public MediaType( String A, String B, float Q )
		{ this( A, B, Q, "" ); }
	
	public MediaType( String A, String B, String params )
		{ this( A, B, 0.0f, params ); }
	
	public MediaType( String A, String B, float Q, String params )
		{ this.type = A; this.subType = B; this.Q = Q; this.params = params; }

	/**
	    Initialise this MediaType with the given type and subtype and
	    a Q value of 1.
	*/
	public MediaType( String A, String B )
		{ this( A, B, 1.0f ); }
	
	final String type;
	final String subType;
	final String params;
	final float Q;
	
	/**
	    A Comparator for media types, based firstly on the Q-value,
	    then on the type field (* earlier than non-*), then on
	    the subtype (likewise). 
	*/
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

	/**
	    Answer the type field of this MediaType.
	*/
	public String getType()
		{ return type; }
	
	/**
	    Answer the subtype field of this MediaType.
	*/
	public String getSubtype()
		{ return subType; }
	
	/**
	    Answer a plausible hash code for this MediaType based on the
	    hash codes of the type and subtype.
	*/
	@Override public int hashCode()
		{ return type.hashCode() ^ subType.hashCode(); }
	
	/**
	    Answer true iff <code>other</code> is a MediaType with the same
	    type and subtype
	*/
	@Override public boolean equals( Object other ) 
		{ return other instanceof MediaType && same( (MediaType) other ); }
	
	private boolean same( MediaType other ) 
		{ return type.equals(other.type) && subType.equals(other.subType); }

	/**
	    This MediaType accepts s if the types and subtypes match. They match
	    if they are equal or if the element of this media type is "*".
	*/
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
		{ return type + "/" + subType + params; }

	/**
	    Answer one of the media types described in <code>canHandle</code>
	    that are accepted by the earliest one of <code>types</code>.
	*/
	public static MediaType accept( List<MediaType> types, String canHandle ) 
		{
		List<MediaType> served = decodeTypes( canHandle );
		for (MediaType t: types)
			for (MediaType s: served)
				if (t.accepts( s )) return s;
		return null;
		}

	/**
	    Answer a list of all the media types described in the
	    elements of <code>e</code>.
	*/
	public static List<MediaType> mediaType( Enumeration<String> e ) 
		{
		List<MediaType> types = new ArrayList<MediaType>();
		while (e.hasMoreElements()) types.addAll( decodeTypes( e.nextElement() ) );
		Collections.sort( types, MediaType.compareMT );
		return types;
		}

	/**
	    Answer a list of all the media types described in the
	    comma-separated list in the string <code>a</code>.
	*/
	public static List<MediaType> decodeTypes( String a ) 
		{
		List<MediaType> result = new ArrayList<MediaType>();
		if (a.length() > 0)
			for (String one: a.split( " *, *" ))
				result.add( decodeType( one ) );
		return result;
		}

	/**
	    Answer the media type described by T/S[; q=Q] in the
	    string <code>one</code>.
	*/
	public static MediaType decodeType( String one )
		{
		float Q = 1.0f;
		String params = "";
		String[] X = one.split( " *; *" );
		for (int i = 1; i < X.length; i += 1)
			if (X[i].startsWith("q="))
				Q = Float.parseFloat( X[1].substring(2));
			else 
				params = params + "; " + X[i];
		String [] AB = X[0].split( "/" );
		return new MediaType( AB[0], AB[1], Q, params );
		}
	}