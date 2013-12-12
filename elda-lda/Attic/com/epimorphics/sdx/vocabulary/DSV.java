/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.sdx.vocabulary;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class DSV
    {
    protected static final String ns = "http://epimorphics.com/RDF/vocabularies/DataSet#";
    
    public static final Resource DataSet = resource( "DataSet" );
    public static final Resource Catalogue = resource( "Catalogue" );
    public static final Resource Entry = resource( "Entry" );

    public static final Property hasID = property( "hasID" );
    public static final Property atURL = property( "atURL" );

    public static final Property hasURL = property( "hasURL" );
    public static final Property hasEntry = property( "hasEntry" );
    public static final Property hasFilter = property( "hasFilter" );
    public static final Property hasProperty = property( "hasProperty" );

    public static final Property onProperty = property( "onProperty" );
    public static final Property hasValue = property( "hasValue" );
    public static final Property widerCatalogue = property( "widerCatalogue" );
    public static final Property narrowerCatalogue = property( "narrowerCatalogue" );
    
    public static final Property valuesFor = property( "valuesFor" );
    public static final Property propertyName = property( "propertyName" );
    
    public static final Property restrictionWith = property( "restrictionWith" );

    public static String getURI()
        { return ns; }

    private static Resource resource( String localName )
        { return ResourceFactory.createResource( ns + localName ); }

    private static Property property( String localName )
        { return ResourceFactory.createProperty( ns + localName ); }

    }
