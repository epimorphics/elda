/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.query.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.epimorphics.lda.core.VarSupply;
import com.epimorphics.lda.query.ValTranslator;
import com.epimorphics.lda.query.ValTranslator.Filters;
import com.epimorphics.lda.rdfq.Any;
import com.epimorphics.lda.rdfq.RDFQ;
import com.epimorphics.lda.rdfq.RenderExpression;
import com.epimorphics.lda.rdfq.Variable;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.shortnames.StandardShortnameService;

public class TestValTranslator {
	
	Filters filters = new FilterList();
	
	VarSupply vs = new VarSequence();
	
	@Test public void testME() {
		ShortnameService sns = new StandardShortnameService();
		ValTranslator vt = new ValTranslator(vs, filters, sns);
		Any o = vt.objectForValue( (String) null, "val", null );
		assertEquals( RDFQ.literal( "val" ), o );
	}
	
	static class VarSequence implements VarSupply {

		int count = 0;
		
		@Override public Variable newVar() {
			return RDFQ.var( "?v_" + ++count );
		}
		
		
	}
	
	static class FilterList implements Filters {

		List<RenderExpression> elements = new ArrayList<RenderExpression>();
		
		@Override public void add(RenderExpression e) {
			elements.add( e );			
		}
		
	}

}
