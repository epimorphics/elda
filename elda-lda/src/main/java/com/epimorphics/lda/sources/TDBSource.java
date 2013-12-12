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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.support.TDBManager;
import com.epimorphics.lda.vocabularies.API;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.Lock;

public class TDBSource extends SourceBase implements Source
    {
    static Logger log = LoggerFactory.getLogger(TDBSource.class);

    protected final Model source; 
    protected final Dataset sourceSet;
    protected final String endpoint;
    
    public TDBSource( Resource endpoint ) {
    	super( endpoint );
    	String endpointString = endpoint.getURI();
        String name = endpointString.substring( TDBManager.PREFIX.length() );
        this.endpoint = endpointString;
        this.sourceSet = TDBManager.getDataset();
        if (name != null && !name.isEmpty()) {
            this.source = TDBManager.getTDBModelNamed(name);
            log.debug("TDB with endpoint " + endpointString + " has model with "
                    + this.source.size() + " triples.");
            if (this.source.isEmpty())
                EldaException.EmptyTDB( name );
        } else {
            source = null;
            log.info("Using TDB whole dataset");
        }
    }

    @Override public void addMetadata( Resource meta )
        {        
        meta.addProperty( API.sparqlEndpoint, ResourceFactory.createResource( endpoint ) );
        }

    @Override public String toString()
        { return "TDB datasource - " + endpoint; }
    
    @Override public Lock getLock() {
    	return source == null ? sourceSet.getLock() : source.getLock();
    }
    
    @Override public QueryExecution execute( Query query )
        {
//        if (log.isInfoEnabled()) log.info( "Running query: " + query.toString().replaceAll( "\n", " " ) + " over " + this.source.size() + " triples.");
//        QueryExecution q = QueryExecutionFactory.create( query, source );
//        Model result = q.execDescribe();
//        if (log.isInfoEnabled()) log.info( "Resulting model has " + result.size() + " triples." );
        return
            source == null 
                ?  QueryExecutionFactory.create( query, sourceSet )
                :  QueryExecutionFactory.create( query, source );
        }

    /**
        TDB supports nested selects via ARQ.
    */
	@Override public boolean supportsNestedSelect() 
		{ return true; }
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
