/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.renderers;

import java.io.*;
import java.util.Map;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.shortnames.CompleteContext;
import com.epimorphics.lda.support.Times;
import com.epimorphics.util.MediaType;
import com.hp.hpl.jena.shared.WrappedException;

/**
    Renderers -- turning result sets into byte streams.
 	@author chris, dave
*/
public interface Renderer {

	/**
	    Renderers produce BytesOut objects which will then
	    stream the rendering to a provided output stream
	    later.
	 
	 	@author chris
	*/
	public interface BytesOut {
		public void writeAll(Times t, OutputStream os);
	}
	
	/**
     	@return the mimetype which this renderer returns
     		in the given renderer context.
    */
    public MediaType getMediaType( Bindings rc );
    
    /**
     	@return the shortname completion mode for this renderer;
    */
    public CompleteContext.Mode getMode();
    
    /**
     	Render a result set. Use t to log times if required.
    */
    public BytesOut render( Times t, Bindings rc, Map<String, String> termBindings, APIResultSet results );

    /**
     	Answer the format suffix associated with this renderer.
    */
	public String getPreferredSuffix();
	
	public static class UTF8 {

		public static String toString(ByteArrayOutputStream os) {
			try { return os.toString("UTF-8"); }
			catch (IOException e) { throw new WrappedException( e ); }
		}
	}
}

