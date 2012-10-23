package com.epimorphics.lda.renderers.velocity;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import sun.org.mozilla.javascript.internal.WrappedException;

import com.epimorphics.lda.core.APIResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.BrokenException;
import com.hp.hpl.jena.vocabulary.RDFS;

public class VelocityCore {

	public void render( APIResultSet results, OutputStream os ) {
		VelocityEngine ve = new VelocityEngine(); 
		ve.setProperty( "resource.loader",  "class" );
		ve.setProperty( "class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader" );
		ve.init();
//
		VelocityContext vc = new VelocityContext();
		Model m = results.getModel();
		m.write( System.out, "TTL" );
		Map<Resource, String> names = Help.getShortnames( m );
		vc.put( "names", names );
		vc.put( "formats", Help.getFormats( m ) );
		vc.put( "items", itemise( results.getResultList() ) );
		vc.put( "meta",  Help.getMetadataFrom( names, m ) );
		vc.put( "ids",  new HashMap<Resource, String>() );
//
		Template t = ve.getTemplate( "page-shell.vm" );
		try {
			Writer w = new OutputStreamWriter( os, "UTF-8" );
			t.merge( vc,  w );
			w.close();
		} catch (UnsupportedEncodingException e) {
			throw new BrokenException( e );
		} catch (IOException e) {
			throw new WrappedException( e );
		}
	}
	
	private List<Item> itemise( List<Resource> items ) {
		List<Item> result = new ArrayList<Item>( items.size() );
		for (Resource item: items) result.add( new Item( item ) );
		return result;
	}
	

}
