/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package com.epimorphics.util.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.epimorphics.util.CollectionUtils;
import com.epimorphics.util.TemplateUtils;

public class TestTemplateUtils {
	
	@Test public void test_split_template() {
		assertEquals( CollectionUtils.list( "aaaa!", "?dbdg", "!bbbb", "?frob", "!cccc", "?elnt", "" ), 
			TemplateUtils.splitTemplate( "aaaa!?dbdg!bbbb?frob!cccc?elnt" ) );
		assertEquals( CollectionUtils.list( "", "?dbdg", "!bbbb", "?frob", "!cccc", "?elnt", "" ), 
			TemplateUtils.splitTemplate( "?dbdg!bbbb?frob!cccc?elnt" ) );
		assertEquals( CollectionUtils.list( "", "?dbdg", "!bbbb", "?frob", "!cccc", "?elnt", "!done" ), 
			TemplateUtils.splitTemplate( "?dbdg!bbbb?frob!cccc?elnt!done" ) );
	}
}
