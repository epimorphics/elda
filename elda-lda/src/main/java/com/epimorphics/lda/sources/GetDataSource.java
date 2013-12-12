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

package com.epimorphics.lda.sources;

import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.support.TDBManager;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.lda.vocabularies.EXTRAS;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;

public class GetDataSource
    {
    public static Source sourceFromSpec( FileManager fm, Resource sourceConfig, AuthMap am ) 
        {
    	Resource endpoint = sourceConfig.getPropertyResourceValue( API.sparqlEndpoint );
        
        if (endpoint == null)
        	EldaException.BadSpecification( "no SPARQL endpoint specified for " + sourceConfig );
                
        if (endpoint.hasProperty( RDF.type, EXTRAS.Combiner ))
        	return new CombinedSource( fm, am, endpoint );
        
        String sparqlEndpointString = endpoint.getURI();  
        
        return 
            sparqlEndpointString.startsWith( LocalSource.PREFIX ) ? new LocalSource( fm, endpoint )
        	: sparqlEndpointString.startsWith( HereSource.PREFIX ) ? new HereSource( sourceConfig.getModel(), endpoint )
            : sparqlEndpointString.startsWith( TDBManager.PREFIX ) ? new TDBSource( endpoint )
            : new SparqlSource( endpoint, am )
            ;
        }
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
