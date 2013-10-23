package com.epimorphics.lda.renderers.velocity;

import java.io.*;
import java.util.*;

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
		WrappedNode wrappedPage = new WrappedNode( b, thisPage );
		vc.put( "thisPage", wrappedPage );
		vc.put( "isItemEndpoint", isItemEndpoint );
		vc.put( "isListEndpoint", isListEndpoint );
		if (isItemEndpoint) vc.put( "primaryTopic", topicOf(b, thisPage) );
		vc.put( "ids",  ids );
		vc.put( "names", names );
		vc.put( "formats", Help.getFormats( m ) );
		vc.put( "items", itemised );
		vc.put( "meta", Help.getMetadataFrom( names, ids, m ) );
		vc.put( "vars", Help.getVarsFrom( names, ids, m ) );
		vc.put( "utils", new Utils() );
		vc.put( "seen", new HashSet<WrappedNode>() );
//		vc.put( "onceies", Help.getOnceies( wrappedPage, m ) );
//		
//		System.err.println( ">> " + Help.getOnceies( wrappedPage, m ) );
		
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
	
	/**
	    Utilities for template code that it can't make from the
	    otherwise-available Wrapped methods and context data.
	*/
	public static class Utils {
		
		public Map<Object, Object> newMap() {
			return new HashMap<Object, Object>();
		}
		
		public Set<Object> newSet() {
			return new HashSet<Object>();
		}
	}

	public WrappedNode topicOf( WrappedNode.Bundle b, Resource thisPage ) {
		return new WrappedNode( b, thisPage.getProperty( FOAF.primaryTopic ).getResource() );
	}
}
