/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
*/
package com.epimorphics.lda.acceptance.tests;

import com.hp.hpl.jena.query.Query;

/**
    An ASK query, with the expected result: true (Positive) or
    false (!isPositive).
*/
public class Ask 
	{
	public final boolean isPositive;
	public final Query ask;
	
	public Ask( boolean isPositive, Query ask ) 
		{ this.isPositive = isPositive; this.ask = ask;	}
	}