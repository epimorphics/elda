/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2012 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.vocabularies.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.epimorphics.jsonrdf.RDFUtil;
import com.epimorphics.lda.vocabularies.EXTRAS;

/**
    Ensure that the EXTRAS elements for missing list elements/tails,
    which have their home in RDFUtil.Vocab, are exactly as they should 
    be when seen from EXTRAS.
*/
public class TestCrosslinkedVocabularies {

	@Test public void RDF_Vocab_shares_NS_with_EXTRAS() {
		assertEquals( EXTRAS.NS, RDFUtil.Vocab.NS );
	}
	
	@Test public void ensure_spellings_match_names() {
		assertEquals( "missingListTail", RDFUtil.Vocab.missingListTail.getLocalName() );
		assertEquals( "missingListElement", RDFUtil.Vocab.missingListElement.getLocalName() );
	}
}
