/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.renderers;

import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.util.MediaType;
import com.hp.hpl.jena.rdf.model.Resource;

/**
    A RendererFactory can construct a Renderer given an {@link APIEndpoint}
    and a {@link ShortnameService}.
*/
public interface RendererFactory
	{
	/**
	    Answer a Renderer constructed to our recipe for the
	    given endpoint and short-name service.
	*/
	public Renderer buildWith( APIEndpoint ep, ShortnameService sns );

	/**
	    Answer a new RendererFactory just like this one, but with
	    the given Resource for additional properties.
	*/
	public RendererFactory withRoot( Resource uri );

	/**
	    Answer a new RendererFactory just like this one, but producing
	    renderers that announce the given media type.
	*/
	public RendererFactory withMediaType( MediaType mt );
	}