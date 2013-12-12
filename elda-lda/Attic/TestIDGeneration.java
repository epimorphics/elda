/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.sdx.system_state.test;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import com.epimorphics.sdx.system_state.ModelState;
import com.epimorphics.sdx.vocabulary.SYSV;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.shared.ReificationStyle;

public class TestIDGeneration
    {
    @Test public void testMe()
        {
        Model m = ModelFactory.createDefaultModel();
        String id = ModelState.createNewId( m );
        assertThat( id, is( "ID-" + (ModelState.ID_BASE_VALUE + 1) ) );
        ModelTestBase.assertIsoModels( m, model( "sys:sysRoot sys:currentId 1000001" ) );
        }

    private Model model( String terms )
        {
        Model result = ModelTestBase.createModel( ReificationStyle.Standard );
        result.setNsPrefix( "sys", SYSV.getURI() );
        ModelTestBase.modelAdd( result, terms );
        return result; 
        }
    }
