/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.sdx.vocabulary.test;

import org.hamcrest.*;
import org.junit.Test;

import static org.junit.Assert.*;

import com.epimorphics.sdx.vocabulary.DSV;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class TestDatasetVocabulary
    {
    @Test public void testDataSetResource()
        {
        assertThat( DSV.DataSet, isDSVResource( Resource.class, "DataSet" ) );
        assertThat( DSV.Entry, isDSVResource( Resource.class, "Entry" ) );
        assertThat( DSV.hasID, isDSVResource( Property.class, "hasID" ) );
        assertThat( DSV.atURL, isDSVResource( Property.class, "atURL" ) );
        assertThat( DSV.Catalogue, isDSVResource( Resource.class, "Catalogue" ) );
        assertThat( DSV.hasURL, isDSVResource( Property.class, "hasURL" ) );
        assertThat( DSV.hasEntry, isDSVResource( Property.class, "hasEntry" ) );
        assertThat( DSV.hasFilter, isDSVResource( Property.class, "hasFilter" ) );
        assertThat( DSV.hasProperty, isDSVResource( Property.class, "hasProperty" ) );
        assertThat( DSV.restrictionWith, isDSVResource( Property.class, "restrictionWith" ) );
        assertThat( DSV.valuesFor, isDSVResource( Property.class, "valuesFor" ) );
        assertThat( DSV.propertyName, isDSVResource( Property.class, "propertyName" ) );
        assertThat( DSV.widerCatalogue, isDSVResource( Property.class, "widerCatalogue" ) );
        assertThat( DSV.narrowerCatalogue, isDSVResource( Property.class, "narrowerCatalogue" ) );
        }

    private Matcher<Resource> isDSVResource( final Class<? extends Resource> c, final String localName )
        { return new ResourceMatcher( c, DSV.getURI(), localName ); }
    }
