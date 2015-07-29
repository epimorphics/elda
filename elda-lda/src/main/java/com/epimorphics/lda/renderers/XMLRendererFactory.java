/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.renderers;

import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.shortnames.*;
import com.epimorphics.lda.shortnames.CompleteContext.Mode;
import com.epimorphics.util.MediaType;
import com.hp.hpl.jena.rdf.model.Resource;

public class XMLRendererFactory implements RendererFactory {
	
	private final CompleteContext.Mode defaultMode = Mode.PreferLocalnames;

	final CompleteContext.Mode mode;
	
	public XMLRendererFactory() {
		this(CompleteContext.Mode.PreferLocalnames);
	}
	
	public XMLRendererFactory(CompleteContext.Mode mode) {
		this.mode = mode;
	}
	
	@Override public RendererFactory withRoot( Resource r ) {
		return new XMLRendererFactory( Mode.decode( r, defaultMode ) );
	}
	
	@Override public RendererFactory withMediaType( MediaType mt ) {
		return this;
	}
	
	@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
		return new XMLRenderer( mode, sns, MediaType.APPLICATION_XML, null );
	}
	
	@Override
    public RendererFactory withISODateFormatting(Boolean jsonUsesISOdate) {
        return this;
    }
}