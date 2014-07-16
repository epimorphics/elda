/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.support;

/**
    A place to store values going in to, and coming out of, queries.
*/
public class NoteBoard {
	
	/**
	    The total number of results for this list endpoint query.
	*/
	public Integer totalResults;
	
	/**
	    The expiry date/time in milliseconds for this query result.
	*/
	public long expiresAt;
}