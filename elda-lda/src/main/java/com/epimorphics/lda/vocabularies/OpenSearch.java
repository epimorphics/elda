/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

/******************************************************************
    File:        OpenSearch.java
    Created by:  Dave Reynolds
    Created on:  2 Feb 2010
 * 
 * (c) Copyright 2010, Epimorphics Limited
 * $Id:  $
 *****************************************************************/

package com.epimorphics.lda.vocabularies;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * Vocabulary file for selected parts of OpenSearch vocabulary
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public class OpenSearch {
    public static final String ns = "http://a9.com/-/spec/opensearch/1.1/";
    
    public static final Property itemsPerPage = property( "itemsPerPage" );
    public static final Property startIndex = property( "startIndex" );
    public static final Property totalResults = property( "totalResults" );

    public static String getURI()
        { return ns; }

    private static Property property( String localName )
        { return ResourceFactory.createProperty( ns + localName ); }

}

