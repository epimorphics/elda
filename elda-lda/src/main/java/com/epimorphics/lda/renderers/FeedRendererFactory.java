package com.epimorphics.lda.renderers;

import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.util.MediaType;
import com.hp.hpl.jena.rdf.model.Resource;

public class FeedRendererFactory implements RendererFactory  {
	
	public static final MediaType atom = new MediaType( "application", "atom+xml" );
	
	private  MediaType mt = atom;
	
	private Resource config = null;
	
	public static final String format = "atom";
	
	public FeedRendererFactory() {
	}
	
	@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
		return new FeedRenderer( mt, config, sns );
	}

	@Override public RendererFactory withRoot( Resource config ) {
		this.config = config;
		return this;
	}

	@Override public RendererFactory withMediaType( MediaType mt ) {
		this.mt = mt;
		return this;
	}
}
