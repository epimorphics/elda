/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
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

import java.io.*;
import java.util.Map;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.shortnames.CompleteContext.Mode;
import com.epimorphics.lda.support.Times;
import com.epimorphics.util.MediaType;
import com.epimorphics.util.StreamUtils;
import com.hp.hpl.jena.shared.WrappedException;

public class TurtleRenderer implements Renderer {
	
    @Override public MediaType getMediaType( Bindings irrelevant ) {
        return MediaType.TEXT_TURTLE;
    }

    @Override public String getPreferredSuffix() {
    	return "ttl";
    }
    
    @Override public Mode getMode() {
    	return Mode.PreferLocalnames;
    }
    
    @Override public Renderer.BytesOut render( Times t, Bindings ignored, Map<String, String> termBindings, final APIResultSet results ) {
    	ByteArrayOutputStream os = new ByteArrayOutputStream();
    	StripPrefixes.Do(results.getMergedModel()).write( os, "TTL" );
    	try { os.flush(); } catch (IOException e) { throw new WrappedException( e ); }
    	final String content = UTF8.toString( os );
    	
    	return new BytesOutTimed() {

			@Override public void writeAll(OutputStream os) {			
				OutputStreamWriter u = StreamUtils.asUTF8(os);
				try {
					u.write(content);
					u.flush();
					u.close();
				} catch (IOException e) {
					throw new WrappedException(e);
				}
			}

			@Override protected String getFormat() {
				return "ttl";
			}
    		
    	};
    }

}
