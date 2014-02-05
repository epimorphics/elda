package com.epimorphics.lda.renderers.velocity;

import java.io.*;
import java.util.*;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.epimorphics.lda.bindings.Bindings;
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
	final String suffix;
	
	public VelocityCore( VelocityEngine ve, String suffix, String templateName ) {
		this.ve = ve;
		this.suffix = suffix;
		this.templateName = templateName;
	}

	public void render( APIResultSet results, Bindings bindings, OutputStream os ) {
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
		Map<String, String> filters = new HashMap<String, String>();
		for (String name: bindings.parameterNames()) {
			if (name.charAt(0) != '_')
				filters.put(name, bindings.get(name).spelling());
		}
	//
		VelocityContext vc = new VelocityContext();
		WrappedNode wrappedPage = new WrappedNode( b, thisPage );
		vc.put( "type_suffix", suffix );
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
		vc.put( "filters", filters );
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
		
		final long origin = System.currentTimeMillis();
		
		public Utils() {	
		}
		
		public int toInt(Object x) {
			return Integer.parseInt(x.toString());
		}
		
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
		
		public long currentMillis() {
			return System.currentTimeMillis() - origin;
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
		
		public static String allButLast(int n, String s) {
			return s.substring(0, s.length() - 2);
		}
		
		public static class Param {
			public final String property;
			public final String op;
			
			public Param( String property, String op ) {
				this.op = op;
				this.property = property;
			}
			
			public String getProperty() { return property; }
			
			public String getOp() { return op; }
		}
		
		public Param cutParam(String property) {
			String prop = null, op = null;
			if (property.startsWith("max-")) {
				prop = property.substring(4); op = "<u>&lt;</u>";
			} else if (property.startsWith("maxEx-")) {
				prop = property.substring(6); op = "&lt";
			} else if (property.startsWith("min-")) {
				prop = property.substring(4); op = "<u>&gt;</u>";
			} else if (property.startsWith("minEx-")) {
				prop = property.substring(6); op = "&gt;";
			} else {
				prop = property; op = "=";
			}
			return new Param(prop, op);
		}
	}

	public WrappedNode topicOf( WrappedNode.Bundle b, Resource thisPage ) {
		return new WrappedNode( b, thisPage.getProperty( FOAF.primaryTopic ).getResource() );
	}
}
