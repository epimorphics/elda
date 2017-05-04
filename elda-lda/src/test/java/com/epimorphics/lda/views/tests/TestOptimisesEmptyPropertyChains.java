package com.epimorphics.lda.views.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import com.epimorphics.lda.core.View;
import com.epimorphics.lda.core.View.State;
import com.epimorphics.lda.support.PropertyChain;

public class TestOptimisesEmptyPropertyChains {

	/**
		When View.fetchByGivenPropertyChains is called with an empty
		bunch of property chains, it ignores the state (so adding
		no triples to anything) and returns as the documentation
		for this fetch the comment NoPropertyChainsPresentComment.
	*/
	@Test public void testOptimises() {
		State st = null;
		View v = new View();
		String result = v.fetchByGivenPropertyChains(st, new ArrayList<PropertyChain>() );
		assertEquals(View.NoPropertyChainsPresentComment, result);
	}
}
