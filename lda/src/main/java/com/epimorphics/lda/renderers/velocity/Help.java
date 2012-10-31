package com.epimorphics.lda.renderers.velocity;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.velocity.app.VelocityEngine;

import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.WrappedIOException;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class Help {
	
    private static final String velocityPropertiesFileName = "velocity.properties";
    
	static final Logger log = LoggerFactory.getLogger( Help.class );

	/**
	    Create a new VelocityEngine. Initialise it with properties from
	    the file named by velocityPropertiesFileName, and if that is absent
	    or defines no properties, with a default classpath resource loader.
	    The <code>config</code> resource is currently unused.
	*/
	public static VelocityEngine createVelocityEngine( Resource config ) {
		Properties p = getProperties( velocityPropertiesFileName );
		VelocityEngine ve = new VelocityEngine(); 
		if (p.isEmpty()) {
			log.info( "using default velocity properties." );
			ve.setProperty( "runtime.references.strict", "true" );
			ve.setProperty( "resource.loader",  "class" );
			ve.setProperty( "class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader" );
		//
			ve.setProperty( "class.resource.loader.cache", false );
			ve.setProperty( "velocimacro.library.autoreload", true );
		//
			ve.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
			ve.setProperty("runtime.log.logsystem.log4j.category", "velocity");
			ve.setProperty("runtime.log.logsystem.log4j.logger", "velocity");
		} else {
			log.info( "loaded properties file " + velocityPropertiesFileName );
		}
		ve.init();
		return ve;
	}
	
	static Properties getProperties( String fileName ) {
		Properties p = new Properties();
		InputStream is = FileManager.get().open( fileName );
		if (is != null) loadNicely( p, is );
		return p;
	}

	private static void loadNicely(Properties p, InputStream is) {
		try { p.load( is ); is.close(); } 
		catch (IOException e) {	throw new WrappedIOException( e ); }
	}

	static String labelFor( Resource r ) {
		Statement s = r.getProperty( RDFS.label );
		return s == null ? r.getLocalName() : s.getString();
	}

	/**
	    Answer a map from metadata item names to Item values. The model's
	    metadata hangs off the wasResultOf value of the displayed Page. The
	    result is a map from pathnames to values, where the pathname is the
	    dot-separated list of shortnames of the properties leading to the
	    value. The terminating values are literals or resources with no
	    properties. Chains involving termBindings or variableBindings are
	    discarded (their content is recorded in the shortnames and varvalues
	    maps produces elsewhere in this class.)
	*/
	public static Map<String, Object> getMetadataFrom( Map<Resource, String> shortNames, Model m ) {
		Map<String, Object> result = new HashMap<String, Object>();
		List<Resource> pages = m.listSubjectsWithProperty( RDF.type, API.Page ).toList();
		for (Resource p: pages) {
			List<Statement> wrs = p.listProperties( API.wasResultOf ).toList();
			for (Statement wr: wrs) {
				descend( shortNames, result, "", wr.getResource() );
			}
		}
		return result;
	}

	private static void descend( Map<Resource, String> shortNames, Map<String, Object> result, String prefix, RDFNode r) {
		if (r.isResource() && hasProperties( r.asResource() )) {
			Resource rr = r.asResource();
			for (Statement s: rr.listProperties().toList()) {
				Resource p = s.getPredicate();
				if (!p.equals( API.termBinding ) && !p.equals( API.variableBinding)) {
					String pn = shortNames.get(p);
					if (pn == null) pn = p.getLocalName();
					descend( shortNames, result, (prefix.isEmpty() ? pn : prefix + "." + pn), s.getObject() );
				}
			}
		} else {
			// System.err.println( ">> " + prefix + " = " + r );
			result.put( prefix, new WrappedNode( r ) );
		}
	}

	static final Property ANY = null;
	
	private static boolean hasProperties(Resource r) {
		return r.getModel().listStatements( r, ANY, ANY ).hasNext();
	}

	/**
	    Answer a map from Resources (for their URIs) to their String shortname,
	    if any, built from the termBindings in the model's metadata.
	*/
	public static Map<Resource, String> getShortnames( Model m ) {
		Map<Resource, String> uriToShortname = new HashMap<Resource, String>();
		List<Resource> pages = m.listSubjectsWithProperty( RDF.type, API.Page ).toList();
		if (pages.size() == 1) {
			List<Statement> results = pages.get(0).listProperties( API.wasResultOf ).toList();
			if (results.size() == 1) {
				List<Statement> bindings = results.get(0).getResource().listProperties( API.termBinding ).toList();
				for (Statement sb: bindings) {
					String sn = sb.getProperty( API.label ).getString();
					Resource fn = sb.getProperty( API.property ).getResource();
					uriToShortname.put( fn, sn );
				}
			}
		}
		return uriToShortname;
	}

	/**
	    Answer a map from variable names to their values represented as Items,
	    built from the variableBindings in the model's metadata.
	*/
	public static Map<String, WrappedNode> getVarsFrom(Map<Resource, String> names, Model m) {
			Map<String, WrappedNode> varValue = new HashMap<String, WrappedNode>();
			List<Resource> pages = m.listSubjectsWithProperty( RDF.type, API.Page ).toList();
			if (pages.size() == 1) {
				List<Statement> results = pages.get(0).listProperties( API.wasResultOf ).toList();
				if (results.size() == 1) {
					List<Statement> bindings = results.get(0).getResource().listProperties( API.variableBinding ).toList();
					for (Statement sb: bindings) {
						String sn = sb.getProperty( API.label ).getString();
						RDFNode fn = sb.getProperty( API.value ).getObject();
						varValue.put( sn, new WrappedNode( fn ) );
					}
				}
			}
			return varValue;
		}

	/**
	    A representation of an available LDA result format, comprised of its
	    name, its media type (as a string), and the URL of the "corresponding"
	    page to link to to see it in that representation.
	*/
	public static class Format implements Comparable<Format> {
		final String linkUsing;
		final String name;
		final String mediaType;
		
		public Format( String linkUsing, String name, String mediaType ) {
			this.linkUsing = linkUsing;
			this.name = name;
			this.mediaType = mediaType;
		}
		
		public String getName() {
			return name;
		}
		
		public String getLink() {
			return linkUsing;
		}

		@Override public int compareTo( Format o ) {
			return name.compareTo( o.name );
		}
	}
	
	/**
	    Answer a set of all the formats available, extracted from the
	    DCTerms metadata of the model.
	*/
	public static List<Format> getFormats( Model m ) {
		List<Format> result = new ArrayList<Format>();
		List<Resource> links = m.listSubjectsWithProperty( DCTerms.format ).toList();
		for (Resource link: links) {
			String name = labelFor( link );
			String type = labelFor( link.getProperty(DCTerms.format).getResource() );
			result.add( new Format( link.getURI(), name, type ) );
		}
		Collections.sort( result );
		return result;
	}
}
