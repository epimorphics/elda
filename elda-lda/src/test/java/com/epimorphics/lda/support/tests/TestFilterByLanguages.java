/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.support.tests;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.support.LanguageFilter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;

public class TestFilterByLanguages {
	
	@Test public void testFiltering() {
		Model m = model( ":s :p 17 ." );
		Model expected = model( ":s :p 17 ." );
		LanguageFilter.filterByLanguages( m, "en,cw".split(",") );
		ModelTestBase.assertIsoModels( expected, m );
	}
	
	@Test public void testFiltering1() {
		Model m = model( ":s :p ''." );
		Model expected = model( ":s :p '' ." );
		LanguageFilter.filterByLanguages( m, "en,cw".split(",") );
		ModelTestBase.assertIsoModels( expected, m );
	}
	
	@Test public void testFiltering2() {
		Model m = model( ":s :p 17 ." );
		Model expected = model( ":s :p 17 ." );
		LanguageFilter.filterByLanguages( m, "en,cw".split(",") );
		ModelTestBase.assertIsoModels( expected, m );
	}
	
	@Test public void testFiltering3() {
		Model m = model( ":s :p 'x'@en ; :q 'x'@it." );
		Model expected = model( ":s :p 'x'@en ." );
		LanguageFilter.filterByLanguages( m, "en,cw".split(",") );
		ModelTestBase.assertIsoModels( expected, m );
	}
	
	@Test public void testFiltering4() {
		Model m = model( ":s :p 'omit'; :p 'keep'@en ." );
		Model expected = model( ":s :p 'keep'@en ." );
		LanguageFilter.filterByLanguages( m, "en,cw".split(",") );
		ModelTestBase.assertIsoModels( expected, m );
	}
	
	@Test public void testFilteringWithNone() {
		Model m = model( ":s :p 'omit'@en; :p 'keep'; :p 'also'@cy ." );
		Model expected = model( ":s :p 'keep'; :p 'also'@cy ." );
		LanguageFilter.filterByLanguages( m, "none,cy".split(",") );
		ModelTestBase.assertIsoModels( expected, m );
	}

	private Model model( String ttl ) {
		return ModelIOUtils.modelFromTurtle( ttl );
	}

}
