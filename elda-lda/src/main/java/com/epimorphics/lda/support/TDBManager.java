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

package com.epimorphics.lda.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.tdb.TDBFactory;
// import com.hp.hpl.jena.tdb.TDBFactory;

/**
    Control point for TDB access -- everything goes through here so that
    there's only one TDB open and the name-expansion is shared.
 
    @author chris
*/
public class TDBManager {

    /**
        The prefix that identifies a path as TDB-related
    */
    public static final String PREFIX = "tdb:";

    /**
        The init-param name that the loader should use to recognise a setting
        for the TDB base directory.
    */
    public static final String TDB_BASE_DIRECTORY = "com.epimorphics.api.TDB-base-directory";

    /**
        The method the loader should call to set the TDB base directory.
    */
    public static void setBaseTDBPath( String value ) { 
        baseTDBPath = value; 
        log.info( "setBaseTDBPath " + value );
    }

    /**
        The TDB base directory path, not (currently) accessible to the outside.
    */
    protected static String baseTDBPath = "";
    
    static Logger log = LoggerFactory.getLogger( TDBManager.class );

    protected static Dataset dataset = null;
    
    /**
        Answer the model with the given name in the TDB dataset.
    */
    public static Model getTDBModelNamed( String uri ) {
        if (dataset == null) dataset = openDataset();
        Model result = 
            (uri == null || uri.isEmpty()) 
                ? dataset.getNamedModel( Quad.unionGraph.getURI() )
                : dataset.getNamedModel( uri );
//        log.debug( "opened " + uri + " (" + result.size() + " triples)" );
        return result;
    }
    
    /*
     * Answer the whole dataset for this TDB
     */
    public static Dataset getDataset() {
        if (dataset == null) dataset = openDataset();
        return dataset;
    }

    private static Dataset openDataset() {
        log.info( "requesting open on TDB dataset at " + baseTDBPath );
        Dataset result = TDBFactory.createDataset( baseTDBPath );
        log.info( "opened: result looks like " + result.toString() );
        return result;
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
