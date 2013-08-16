package com.epimorphics.lda.renderers;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.shortnames.CompleteContext.Mode;
import com.epimorphics.lda.support.Times;
import com.epimorphics.util.MediaType;
import com.hp.hpl.jena.rdf.model.Resource;

public class FeedRenderer implements Renderer {

	private final MediaType mt;
	
	public FeedRenderer(MediaType mt, Bindings bindings, Resource config) {
		this.mt = mt;
	}

	@Override public MediaType getMediaType(Bindings rc) {
		return mt;
	}

	@Override public Mode getMode() {
		return Mode.PreferPrefixes;
	}

	@Override public BytesOut render
		(Times t
		, Bindings rc
		, Map<String, String> termBindings
		, final APIResultSet results
		) {
		return new BytesOutTimed() {

			@Override protected void writeAll(OutputStream os) {
				PrintStream ps = new PrintStream( os );
				renderFeed( ps, results );
				ps.flush();
				ps.close();
			}

			@Override protected String getFormat() {
				return FeedRendererFactory.format;
			}
			
		};
	}

	@Override public String getPreferredSuffix() {
		return FeedRendererFactory.format;
	}
	
	private void renderFeed( PrintStream ps, APIResultSet results ) {
		ps.println( "<x>this feature is not yet supported</x>" );
	}

}
