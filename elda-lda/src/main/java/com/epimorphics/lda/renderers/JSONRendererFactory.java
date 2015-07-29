/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.renderers;

import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.shortnames.CompleteContext;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.util.MediaType;
import com.hp.hpl.jena.rdf.model.Resource;

public class JSONRendererFactory implements RendererFactory {

	private final CompleteContext.Mode defaultMode = CompleteContext.Mode.PreferLocalnames;
	
	final MediaType mt;
	final CompleteContext.Mode mode;
	final Boolean jsonUsesISOdate;
	
	public JSONRendererFactory(MediaType mt, CompleteContext.Mode mode, Boolean jsonUsesISOdate) {
		this.mt = mt;
		this.mode = mode;
		this.jsonUsesISOdate = jsonUsesISOdate;
	}
	
	public JSONRendererFactory(MediaType mt) {
		this(mt, CompleteContext.Mode.PreferLocalnames, false);
	}
	
	@Override public RendererFactory withMediaType( MediaType mt ) {
		return new JSONRendererFactory(mt, mode, false);
	}
	
	@Override public RendererFactory withRoot( Resource r ) {
		return new JSONRendererFactory( mt, CompleteContext.Mode.decode( r, defaultMode ), false );
	}
	
	@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
		return new JSONRenderer( mode, ep, mt, jsonUsesISOdate );
	}

    @Override
    public RendererFactory withISODateFormatting(Boolean jsonUsesISOdate) {
        return new JSONRendererFactory(mt, mode, jsonUsesISOdate);
    }
}