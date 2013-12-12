/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

/**
 * 
 */
package com.epimorphics.lda.vocabularies.test;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import com.hp.hpl.jena.rdf.model.Resource;

public class ResourceMatcher extends BaseMatcher<Resource>
    {
    protected final String ns;
    protected final String localName;
    protected final Class<? extends Resource> c;
    
    ResourceMatcher( Class<? extends Resource> c, String ns, String localName )
        {
        this.c = c;
        this.ns = ns;
        this.localName = localName;
        }
    
    @Override public boolean matches( Object x )
        { 
        return 
            c.isInstance( x ) 
            && (ns + localName).equals( ((Resource) x).getURI() )
            ;
        }
    
    @Override public void describeTo( Description d )
        {
        d.appendText( "has local name " + localName + " in namespace " + ns );
        }
    }