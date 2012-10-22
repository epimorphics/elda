package com.epimorphics.lda.renderers.velocity;

import java.io.OutputStream;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.renderers.BytesOutTimed;
import com.epimorphics.lda.renderers.Renderer;
import com.epimorphics.lda.support.Times;
import com.epimorphics.util.MediaType;

public class VelocityRenderer implements Renderer {
	
	final MediaType mt;
	final VelocityCore core;
	
	public VelocityRenderer( MediaType mt ) {
		this.mt = mt;
		this.core = new VelocityCore();
	}
	
    @Override public MediaType getMediaType( Bindings irrelevant ) {
        return mt;
    }

    @Override public String getPreferredSuffix() {
    	return "html";
    }
    
    @Override public Renderer.BytesOut render( Times t, Bindings b, final APIResultSet results ) {
    	return new BytesOutTimed() {

			@Override public void writeAll( OutputStream os ) {
				core.render( results, os );
			}			

			@Override protected String getFormat() {
				return "html";
			}
    		
    	};
    }
}