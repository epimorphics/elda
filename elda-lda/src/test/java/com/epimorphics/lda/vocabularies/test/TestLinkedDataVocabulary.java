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

package com.epimorphics.lda.vocabularies.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.hamcrest.Matcher;
import org.junit.Test;

import com.epimorphics.lda.vocabularies.API;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public class TestLinkedDataVocabulary
    {
    @Test public void testAPIURI()
        {
        assertThat( API.getURI(), is( "http://purl.org/linked-data/api/vocab#" ) );
        }
    
    @Test public void testAPITypes()
        {
        assertThat( API.ListEndpoint, isAPIResource( Resource.class, "ListEndpoint" ) );
        }
    
    @Test public void testAPIProperties()
        {
//        assertThat( API.resourceVar, isAPIResource( Property.class, "resourceVar" ) );
//        assertThat( API.paramVar, isAPIResource( Property.class, "paramVar" ) );
        assertThat( API.viewer, isAPIResource( Property.class, "viewer" ) );
        assertThat( API.items, isAPIResource( Property.class, "items" ) );
        }
    
    private Matcher<Resource> isAPIResource( final Class<? extends Resource> c, final String localName )
        { return new ResourceMatcher( c, API.getURI(), localName ); }
    }

    
/*
    (c) Copyright 2010 Epimorphics Limited
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
