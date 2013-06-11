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
	
	public JSONRendererFactory(MediaType mt, CompleteContext.Mode mode) {
		this.mt = mt;
		this.mode = mode;
	}
	
	public JSONRendererFactory(MediaType mt) {
		this(mt, CompleteContext.Mode.PreferLocalnames);
	}
	
	@Override public RendererFactory withMediaType( MediaType mt ) {
		return new JSONRendererFactory(mt, mode);
	}
	
	@Override public RendererFactory withRoot( Resource r ) {
		return new JSONRendererFactory( mt, CompleteContext.Mode.decode( r, defaultMode ) );
	}
	
	@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
		return new JSONRenderer( mode, ep, mt );
	}
}