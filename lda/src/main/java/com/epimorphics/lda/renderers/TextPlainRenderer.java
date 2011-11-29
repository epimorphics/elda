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

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.*;
import com.epimorphics.lda.support.Times;
import com.epimorphics.util.MediaType;

public final class TextPlainRenderer implements Renderer {

    @Override public MediaType getMediaType( Bindings irrelevant ) {
        return MediaType.TEXT_PLAIN;
    }

    @Override public String render( Times t, Bindings ignored, APIResultSet results ) {
        return results.toString();
    }
}
    
