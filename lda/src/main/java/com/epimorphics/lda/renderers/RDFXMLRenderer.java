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

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.support.Times;
import com.epimorphics.util.MediaType;

public class RDFXMLRenderer implements Renderer {
	
    @Override public MediaType getMediaType( Bindings irrelevant ) {
       return MediaType.APPLICATION_RDF_XML;
    }

    @Override public String render( Times t, Bindings ignored, APIResultSet results ) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        results.getModel().write( bos, "RDF/XML-ABBREV" );
        return bos.toString();
    }
}
