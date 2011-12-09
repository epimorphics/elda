/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

/*
	(c) Copyright 2010 Epimorphics Limited
	[see end of file]
	$Id$
*/

package com.epimorphics.lda.renderers;

import java.io.OutputStream;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.support.Times;
import com.epimorphics.util.MediaType;

public class TurtleRenderer implements Renderer {
	
    @Override public MediaType getMediaType( Bindings irrelevant ) {
        return MediaType.TEXT_TURTLE;
    }
    
    @Override public Renderer.BytesOut render( Times t, Bindings ignored, final APIResultSet results ) {
    	return new Renderer.BytesOut() {

			@Override public void writeAll(Times t, OutputStream os) {
				results.getModel().write( os, "TTL" );
				Renderer.StreamUtils.flush( os );
			}
    		
    	};
    }

}
