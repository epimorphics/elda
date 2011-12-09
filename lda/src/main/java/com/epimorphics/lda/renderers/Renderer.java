/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

/******************************************************************
    File:        Renderer.java
    Created by:  Dave Reynolds
    Created on:  2 Feb 2010
 * 
 * (c) Copyright 2010, Epimorphics Limited
 * $Id:  $
 *****************************************************************/

package com.epimorphics.lda.renderers;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.support.Times;
import com.epimorphics.util.MediaType;
import com.hp.hpl.jena.shared.WrappedException;

/**
 * Abstraction for renderer
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
*/
public interface Renderer {

	public interface BytesOut {
		public void writeAll(Times t, OutputStream os);
	}
	
	public class StreamUtils {

		public static OutputStreamWriter asUTF8(OutputStream os) {
			try { 
				return new OutputStreamWriter( os, "UTF-8" );
			} catch (UnsupportedEncodingException e) {
				throw new WrappedException( e );
			}
		}

		public static void flush(OutputStream os) {
			try { os.flush(); } 
			catch (IOException e) { throw new WrappedException( e  ); }
		}

		public static String pullString(BytesOut rbo) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			rbo.writeAll( new Times(), bos );
			return bos.toString();
		}
		
	}
	
	public class BytesOutString implements BytesOut {

		final String content;
		
		public BytesOutString( String content ) {
			this.content = content;
		}

		@Override public void writeAll(Times t, OutputStream os) {
			try {
				OutputStream bos = new BufferedOutputStream(os);
				OutputStreamWriter osw = new OutputStreamWriter(bos, "UTF-8" );
				osw.write( content );
				osw.flush();
				osw.close();
			} catch (IOException e) {
				throw new WrappedException( e );
			}
		}
		
	}
	
    /**
     	@return the mimetype which this renderer returns
     		in the given renderer context.
    */
    public MediaType getMediaType( Bindings rc );
    
    /**
     	Render a result set. Use t to log times if required.
    */
    public BytesOut render( Times t, Bindings rc, APIResultSet results );
}

