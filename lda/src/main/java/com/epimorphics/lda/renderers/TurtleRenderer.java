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

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.support.Times;
import com.epimorphics.util.MediaType;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.shared.WrappedException;

public class TurtleRenderer implements Renderer {
	
    @Override public MediaType getMediaType( Bindings irrelevant ) {
        return MediaType.TEXT_TURTLE;
    }
    
    @Override public String render( Times t, Bindings ignored, APIResultSet results ) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        results.getModel().write( bos, "TTL" );
        try {
			return bos.toString( "UTF-8" );
		} catch (UnsupportedEncodingException e) {
			throw new WrappedException( e );
		}
    }

}
