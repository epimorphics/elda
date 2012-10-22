package com.epimorphics.lda.renderers;

import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.renderers.velocity.VelocityRenderer;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.util.MediaType;
import com.hp.hpl.jena.rdf.model.Resource;

public class VelocityRendererFactory implements RendererFactory {

	private MediaType mt = MediaType.TEXT_HTML;
	
	public VelocityRendererFactory() {
	}
	
	@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
		return new VelocityRenderer( mt );
	}

	@Override public RendererFactory withRoot( Resource uri ) {
		return this;
	}

	@Override public RendererFactory withMediaType( MediaType mt ) {
		this.mt = mt;
		return this;
	}
}
