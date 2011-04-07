package com.epimorphics.lda.demo;

import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.renderers.Renderer;
import com.epimorphics.lda.renderers.RendererFactory;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.hp.hpl.jena.rdf.model.Resource;

public class Demo_HTML_RendererFactory implements RendererFactory {

	@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
		// TODO Auto-generated method stub
		return new Demo_HTML_Renderer( ep, sns );
	}

	@Override public RendererFactory withRoot(Resource uri) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override public RendererFactory withMediaType(String mediaType) {
		// TODO Auto-generated method stub
		return this;
	}

}
