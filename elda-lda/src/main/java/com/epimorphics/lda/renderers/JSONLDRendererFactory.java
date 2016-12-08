package com.epimorphics.lda.renderers;

import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.util.MediaType;
import com.hp.hpl.jena.rdf.model.Resource;

public class JSONLDRendererFactory implements RendererFactory {

	Resource config = null;
	
	boolean jsonUsesISOdate = false;
	
	MediaType mt = MediaType.APPLICATION_JSON_LD;
	
	@Override public Renderer buildWith(APIEndpoint ep, ShortnameService sns) {
		return new JSONLDRenderer(config, mt, ep, sns, jsonUsesISOdate);
	}

	@Override public RendererFactory withRoot(Resource config) {
		this.config = config;
		return this;
	}

	@Override public RendererFactory withMediaType(MediaType mt) {
		this.mt = mt;
		return this;
	}

	@Override public RendererFactory withISODateFormatting(Boolean jsonUsesISOdate) {
		this.jsonUsesISOdate = jsonUsesISOdate;
		return this;
	}

}
