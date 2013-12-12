/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.sdx.system_state;

import com.epimorphics.sdx.vocabulary.SYSV;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.tdb.TDBFactory;

public class ModelState
    {
    public static final int ID_BASE_VALUE = 1000000;

    protected static final String tdbDirectory = "/tmp/dateybayse";

    public static synchronized String createNewId( Model m )
        {
        StmtIterator it = m.listStatements( null, SYSV.currentId, (RDFNode) null );
        Statement s = it.hasNext() ? it.next() : m.createStatement( SYSV.sysRoot, SYSV.currentId, m.createTypedLiteral( 1000000 ) );
        m.remove( s );
        m.add( s.changeLiteralObject( s.getInt() + 1 ) );
        return "ID-" + (s.getInt() + 1);
        }
    
    public static Model getModel()
        {
        Model result = TDBFactory.createModel( tdbDirectory );
        result.setNsPrefixes( PrefixMapping.Extended );
//        PrefixMapping pm = result.getGraph().getPrefixMapping();
//        System.err.println( ">> A: " + pm );
//        pm.expandPrefix( "http://dont.expand.me/ok" );
//        pm.expandPrefix( "ftp://dont.expand.me/ok" );
//        pm.expandPrefix( "frob:ok" );
//        System.err.println( ">> B: " + pm );
        return result;
        }

    }
