package com.epimorphics.lda.renderers.velocity;

import java.io.*;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.epimorphics.lda.core.APIResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.BrokenException;
import com.hp.hpl.jena.shared.WrappedException;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;

public class VelocityCore {
	
	final VelocityEngine ve;
	final String templateName;
	
	public VelocityCore( VelocityEngine ve, String templateName ) {
		this.ve = ve;
		this.templateName = templateName;
	}

	public void render( APIResultSet results, OutputStream os ) {
		Resource thisPage = results.getRoot();
		Model m = results.getMergedModel();
		IdMap ids = new IdMap();
		ShortNames names = Help.getShortnames( m );
		boolean isItemEndpoint = thisPage.hasProperty(FOAF.primaryTopic);
		boolean isListEndpoint = !isItemEndpoint;
		WrappedNode.Bundle b = new WrappedNode.Bundle( names,  ids );
		List<WrappedNode> itemised = WrappedNode.itemise( b, results.getResultList() );
	//
		VelocityContext vc = new VelocityContext();
		vc.put( "thisPage", new WrappedNode( b, thisPage ) );
		vc.put( "isItemEndpoint", isItemEndpoint );
		vc.put( "isListEndpoint", isListEndpoint );
		if (isItemEndpoint) vc.put( "primaryTopic", topicOf(b, thisPage) );
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

	public WrappedNode topicOf( WrappedNode.Bundle b, Resource thisPage ) {
		return new WrappedNode( b, thisPage.getProperty( FOAF.primaryTopic ).getResource() );
	}
}
