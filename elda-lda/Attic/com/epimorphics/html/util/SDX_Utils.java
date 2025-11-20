/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.html.util;

import com.epimorphics.sdx.vocabulary.DSV;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDFS;

public class SDX_Utils {
    public static String labelOf(Resource ds) {
        Statement s = ds.getProperty(RDFS.label);
        return s == null ? SDX_Utils.idOf(ds) : s.getString();
    }

    public static String idOf(Resource ds) {
        Statement s = ds.getProperty(DSV.hasID);
        return s == null ? "?" + ds.getURI() + "?" : s.getString();
    }
}
