/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.

    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.renderers;

import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.renderers.velocity.VelocityRenderer;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.util.MediaType;
import com.hp.hpl.jena.rdf.model.Resource;

public class VelocityRendererFactory implements RendererFactory {

    private MediaType mt = MediaType.TEXT_HTML;

    private Resource config = null;

    @Override
    public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
        return new VelocityRenderer( mt, ep, config, null, sns );
    }

    @Override
    public RendererFactory withRoot( @SuppressWarnings( "hiding" ) Resource config ) {
        this.config = config;
        return this;
    }

    @Override
    public RendererFactory withMediaType( @SuppressWarnings( "hiding" ) MediaType mt ) {
        this.mt = mt;
        return this;
    }
    
    @Override
    public RendererFactory withISODateFormatting(Boolean jsonUsesISOdate) {
        return this;
    }
}
