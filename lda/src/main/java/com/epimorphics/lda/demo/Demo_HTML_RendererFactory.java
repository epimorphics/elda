/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.demo;

import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.renderers.Renderer;
import com.epimorphics.lda.renderers.RendererFactory;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.util.MediaType;
import com.hp.hpl.jena.rdf.model.Resource;

public class Demo_HTML_RendererFactory implements RendererFactory {

	@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
		return new Demo_HTML_Renderer( ep, sns );
	}

	@Override public RendererFactory withRoot(Resource uri) {
		return this;
	}

	@Override public RendererFactory withMediaType(MediaType mt) {
		return this;
	}

}
