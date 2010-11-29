/*
    See lda-top/LICENCE for the licence for this software.
    (c) copyright Epimorphics Limited 2010
    $Id$
*/

package com.epimorphics.vocabs;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
    Vocabulary elements for things that should be in the API schema.
 
 	@author chris
*/
public class FIXUP 
	{
	public static String NS = API.getURI();
	
	public static String getURI()
		{ return NS; }
	
	public static Property prefixMapping = property( NS, "prefixMapping" );
	
	public static Property prefix = property( NS, "prefix" );
	
	public static Property namespace = property( NS, "namespace" );
	
	public static final Property language = property( NS, "language" );

	public static final Resource Hidden = resource( NS, "Hidden" );

	public static final Resource Multivalued = resource( NS, "Multivalued" );

	public static final Property items = property( NS, "items" );

	public static final Property type = property( NS, "type" );

	public static final Property label = property( NS, "label" );

	public static final Property page = property( NS, "page" );

	public static final Resource Page = resource( NS, "Page" );

	public static final Property definition = property( NS, "definition" );

	public static final Property structured = property( NS, "structured" );

	public static final Property variable = property( NS, "variable" );

	public static final Property value = property( NS, "value" );

	private static Property property( String ns, String local )
        { return ResourceFactory.createProperty( ns + local ); }

    private static Resource resource( String ns, String local )
        { return ResourceFactory.createResource( ns + local ); }
	
	}
