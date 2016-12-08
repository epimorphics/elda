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

import java.io.OutputStream;
import java.util.Map;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.shortnames.CompleteContext.Mode;
import com.epimorphics.lda.support.Times;
import com.epimorphics.util.MediaType;

public class RDFXMLRenderer implements Renderer {
	
	static final String XML_POISON = "\nSTREAMING ERROR<=>'<=>\"<=>\n";
	
    @Override public MediaType getMediaType( Bindings irrelevant ) {
       return MediaType.APPLICATION_RDF_XML;
    }

    @Override public String getPreferredSuffix() {
    	return "rdf";
    }
    
    @Override public Mode getMode() {
    	return Mode.PreferLocalnames;
    }

    @Override public Renderer.BytesOut render( Times t, Bindings ignored, Map<String, String> termBindings, final APIResultSet results ) {
    	return new BytesOutTimed() {
			
			@Override public void writeAll(OutputStream os) {
				StripPrefixes.Do(results.getMergedModel()).write( os, "RDF/XML-ABBREV" );
			}

			@Override protected String getFormat() {
				return "rdf";
			}

			@Override public String getPoison() {
				return XML_POISON;
			}
		};
    }
}
