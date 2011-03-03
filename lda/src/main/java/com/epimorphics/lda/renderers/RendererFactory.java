/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.renderers;

import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.shortnames.ShortnameService;

/**
    A RendererFactory can construct a Renderer given an {@link APIEndpoint}
    and a {@link ShortnameService}.
*/
public interface RendererFactory
	{
	public Renderer buildWith( APIEndpoint ep, ShortnameService sns );
	}