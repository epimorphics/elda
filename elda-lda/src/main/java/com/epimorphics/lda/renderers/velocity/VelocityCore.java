package com.epimorphics.lda.renderers.velocity;

import java.io.*;
import java.util.*;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.core.APIResultSet.MergedModels;
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
		MergedModels mm = results.getModels();
		Model m = mm.getMergedModel();
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
		vc.put( "views", Help.getViews( m ) );
		vc.put( "items", itemised );
		vc.put( "meta", Help.getMetadataFrom( names, ids, m ) );
		vc.put( "vars", Help.getVarsFrom( names, ids, m ) );
		vc.put( "utils", new Utils() );		
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
		
		public <T extends Comparable<? super T>>void sort(List<? extends T> l) {
			Collections.sort(l);
		}
		
		public Map<Object, Object> newMap() {
			return new HashMap<Object, Object>();
		}
		
		public Set<Object> newSet() {
			return new HashSet<Object>();
		}
		
		public List<Object> newList() {
			return new ArrayList<Object>();
		}
		
		public void println(Object x) {
			System.err.println( ">> " + x );
		}
		
		public void println(Object x, Object y) {
			System.err.println( ">> " + x + " " + y );
		}
		
		public String join(Collection<Object> things, String infix) {
			StringBuilder sb = new StringBuilder();
			boolean doneSome = false;
			for (Object t: things) {
				String tString = t.toString();
				if (tString.length() > 0) {
					if (doneSome) sb.append(infix);
					sb.append(tString);
					doneSome = true;
				}
			}
			return sb.toString();
		}
	}

	public WrappedNode topicOf( WrappedNode.Bundle b, Resource thisPage ) {
		return new WrappedNode( b, thisPage.getProperty( FOAF.primaryTopic ).getResource() );
	}
}
