package com.epimorphics.lda.renderers.velocity;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.core.View;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.BrokenException;
import com.hp.hpl.jena.shared.WrappedException;

public class VelocityCore {
	
	final VelocityEngine ve;
	final String templateName;
	
	public VelocityCore( VelocityEngine ve, String templateName ) {
		this.ve = ve;
		this.templateName = templateName;
	}

	public void render( APIResultSet results, OutputStream os ) {
		Resource thisPage = results.getRoot();
		View v = results.getView();
		Model m = results.getModel();
		IdMap ids = new IdMap();
		ShortNames names = Help.getShortnames( m );
		WrappedNode.Bundle b = new WrappedNode.Bundle( names,  ids );
		List<WrappedNode> itemised = WrappedNode.itemise( b, results.getResultList() ); // new ExtractByView( names, v ).itemise( ids, results.getResultList() );
	//
		VelocityContext vc = new VelocityContext();
		vc.put( "thisPage", new WrappedNode( b, thisPage ) );
		vc.put( "ids",  ids );
		vc.put( "names", names );
		vc.put( "formats", Help.getFormats( m ) );
		vc.put( "items", itemised );
		vc.put( "meta",  Help.getMetadataFrom( names, ids, m ) );
		vc.put( "vars",  Help.getVarsFrom( names, ids, m ) );
	//
		Template t = ve.getTemplate( templateName );
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
}
