/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.renderers.tests;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.core.View;
import com.epimorphics.lda.renderers.*;
import com.epimorphics.lda.renderers.Renderer.BytesOut;
import com.epimorphics.lda.support.Times;
import com.epimorphics.util.CollectionUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.WrappedException;

public class TestTurtleRenderer {
	
	@Test public void testUtf8SurvivesRendering() {
		Model m = ModelIOUtils.modelFromTurtle( "<eh:/example> <eh:/predicate> '<<\u03ff>>'." );
		Resource it = m.createResource( "<eh:/example>" );
		APIResultSet rs = new APIResultSet( m.getGraph(), CollectionUtils.list(it), true, false, "notUsed", new View() );
		Renderer.BytesOut rbo = new TurtleRenderer().render( new Times(), null, new HashMap<String, String>(), rs );
		String rendered = TestTurtleRenderer.pullString( rbo );
		// String unwrapped = rendered.replaceFirst( "(.|[\r\n])*<<", "" ).replaceAll( ">>(.|[\r\n])*", "" );
		String unwrapped = rendered.split("<<")[1].split(">>")[0];
		assertEquals( "Unicode character did not survive rendering", "\u03ff", unwrapped );
	}

	public static String pullString( BytesOut rbo ) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		rbo.writeAll( new Times(), bos );
		try { 
			return bos.toString( "UTF-8" );
		} catch (UnsupportedEncodingException e) {
			throw new WrappedException( e );
		}
	}

}
