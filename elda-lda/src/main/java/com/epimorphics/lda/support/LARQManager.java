/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.support;


import org.apache.jena.larq.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.shared.WrappedException;

public class LARQManager {

    public static final String LARQ_DIRECTORY_KEY = "com.epimorphics.api.LARQ-base-directory";
    
    public static void setLARQIndexDirectory( String value ) {
//        try {
//        	larqIndexDirectory = FSDirectory.open( new File(value ));    
//        	log.info( "setLARQIndexDirectory " + value );
//        } catch (IOException e) {
//        	throw new WrappedException( e );
//        }
    }
    
    static Logger log = LoggerFactory.getLogger( LARQManager.class );
    
    protected static Directory larqIndexDirectory = null;
    
    public static void setLARQIndex( QueryExecution qx ) {
        log.info( "setting LARQ index [in " + larqIndexDirectory + "] on query " + qx.toString() );
        LARQ.setDefaultIndex( qx.getContext(), getIndex() );
    }

    static final boolean getIndexViaWriter = false;
    
    private static IndexLARQ getIndex()
        {
        if (getIndexViaWriter)
            {
            IndexWriter iw = getWriter();
            IndexBuilderSubject ibs = new IndexBuilderSubject( iw );
            ibs.closeWriter();
            return ibs.getIndex();
            }
        else
            return new IndexLARQ( getReader() );
        }

    private static IndexReader getReader() {
        try { return IndexReader.open( larqIndexDirectory ); }
        catch (Exception e) { throw new WrappedException( e ); }
    }

    private static IndexWriter getWriter() {
    	Version v = Version.LUCENE_30;
    	Analyzer a = new StandardAnalyzer(v);
    	IndexWriterConfig iwc = new IndexWriterConfig( v, a );
        try { return new IndexWriter( larqIndexDirectory, iwc ); }
        catch (Exception e) { throw new WrappedException( e ); }
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
