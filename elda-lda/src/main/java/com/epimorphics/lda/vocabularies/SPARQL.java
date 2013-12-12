/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.vocabularies;

import com.hp.hpl.jena.rdf.model.*;

/**
    A stub for the necessary SPARQL vocabulary.
    
 	@author eh
*/
public class SPARQL {

	private static Property property( String ns, String local )
        { return ResourceFactory.createProperty( ns + local ); }

    private static Resource resource( String ns, String local )
        { return ResourceFactory.createResource( ns + local ); }

	public static final String NS = "http://purl.org/net/opmv/types/sparql#";
	
	public static final String SERVICE = "http://www.w3.org/ns/sparql-service-description#";

	public static final Resource QueryResult = resource( NS, "QueryResult" );
	
	public static final Property query = property( NS, "query" );

	public static final Property endpoint = property( NS, "endpoint" );

	public static final Resource Service = property( SERVICE, "Service" );

	public static final Property url = property( SERVICE, "url" );

}
