/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.query;

import com.epimorphics.lda.core.Param;

/**
    A P=V pair taken from a filter in the config.
*/
public class PendingParameterValue
	{
	public final Param param;
	public final String val;
	
	public PendingParameterValue( Param param, String val ) 
		{
		this.param = param;
		this.val = val;
		}
	
	@Override public String toString() 
		{ return "<pending " + param + "=" + val + ">"; }
	}