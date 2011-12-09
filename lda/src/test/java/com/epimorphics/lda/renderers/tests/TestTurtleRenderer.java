/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.renderers.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.renderers.Renderer;
import com.epimorphics.lda.renderers.TurtleRenderer;
import com.epimorphics.lda.support.Times;
import com.epimorphics.util.CollectionUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class TestTurtleRenderer {
	
	@Test public void testUtf8SurvivesRendering() {
		Model m = ModelIOUtils.modelFromTurtle( "<eh:/example> <eh:/predicate> '<<\u03ff>>'." );
		Resource it = m.createResource( "<eh:/example>" );
		APIResultSet rs = new APIResultSet( m.getGraph(), CollectionUtils.list(it), true, "notUsed" );
		Renderer.BytesOut rbo = new TurtleRenderer().render( new Times(), null, rs );
		String rendered = Renderer.StreamUtils.pullString( rbo );
		String unwrapped = rendered.replaceFirst( "(.|[\r\n])*<<", "" ).replaceAll( ">>(.|[\r\n])*", "" );
		assertEquals( "Unicode character did not survive rendering", "\u03ff", unwrapped );
	}

}
