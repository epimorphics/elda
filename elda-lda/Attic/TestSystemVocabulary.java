/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.sdx.vocabulary.test;

import org.junit.Test;
import org.hamcrest.*;

import static org.junit.Assert.*;

import com.epimorphics.sdx.vocabulary.SYSV;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class TestSystemVocabulary
    {
    @Test public void testSystemVocabulary()
        {
        assertThat( SYSV.currentId, isSysResource( Property.class, "currentId" ) );
        assertThat( SYSV.sysRoot, isSysResource( Resource.class, "sysRoot" ) );
        }

    private Matcher<Resource> isSysResource( Class<? extends Resource> c, String localName )
        { return new ResourceMatcher( c, SYSV.getURI(), localName ); }
    }
