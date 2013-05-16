package com.epimorphics.lda.renderers.velocity;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.velocity.app.VelocityEngine;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.WrappedIOException;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class Help {
    
	static final Logger log = LoggerFactory.getLogger( Help.class );

	/**
	    Create a new VelocityEngine. Initialise it with properties from
	    the file named by velocityPropertiesFileName, and if that is absent
	    or defines no properties, with a default classpath resource loader.
	    The <code>config</code> resource is currently unused.
	*/
	public static VelocityEngine createVelocityEngine( Bindings b, Resource config ) {
		String propertiesName = velocityPropertiesFileName(b);
		Properties p = getProperties( propertiesName );
		VelocityEngine ve = new VelocityEngine(); 
		if (p.isEmpty()) {
			log.info( "using default velocity properties." );
		//
			String defaultRoot = b.getAsString("_resourceRoot", "") + "/vm/";
			String templateRoot = b.getAsString("_velocityRoot", defaultRoot);
		//
			ve.setProperty( "runtime.references.strict", "true" );
//			ve.setProperty( "resource.loader",  "class" );
//			ve.setProperty( "class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader" );
		//
			ve.setProperty( "resource.loader", "url" );
			ve.setProperty( "url.resource.loader.class", "org.apache.velocity.runtime.resource.loader.URLResourceLoader" );
			ve.setProperty( "url.resource.loader.root", templateRoot );
			ve.setProperty( "url.resource.loader.cache", true );
		//
//			ve.setProperty( "class.resource.loader.cache", false );
//			ve.setProperty( "velocimacro.library.autoreload", true );
		//
			ve.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
			ve.setProperty("runtime.log.logsystem.log4j.category", "velocity");
			ve.setProperty("runtime.log.logsystem.log4j.logger", "velocity");
		} else {
			log.info( "loaded properties file " + propertiesName );
		}
		ve.init();
		return ve;
	}
	
	private static String velocityPropertiesFileName(Bindings b) {
		return b.getAsString("_resourceRoot", "") + "/velocity.properties";
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

	static final String SKOS = "http://www.w3.org/2004/02/skos/core#";
	
	static final Property SKOS_prefLabel = ResourceFactory.createProperty( SKOS + "prefLabel" );
	
	/**
	    Return the "preferred" label of this resource. The definition of
	    "preferred" is:
	    
	    <p>The lexical form of some literal which is the value of the
	    	property <code>skos:prefLabel</code>;
	    	
	    <p>failing that, the lexical form of some literal with no
	    	language code which is the value of the property
	    	<code>rdfs:label</code>;
	    
	    <p>failing that, the lexical form of some literal with a language
	    	code which is the value of the property	<code>rdfs:label</code>;
	    	
	    <p>failing that, the local name of the resource.	
	*/
	public static String labelFor( Resource r ) {
		Statement pref = r.getProperty( SKOS_prefLabel );
		if (pref != null && pref.getObject().isLiteral())
			return ((Literal) pref.getObject()).getLexicalForm();
	//
		String langLabel = null;
		for (Statement s: r.listProperties( RDFS.label ).toList()) {
			RDFNode o = s.getObject();
			if (o.isLiteral()) {
				Literal label = (Literal) o;
				String spelling = label.getLexicalForm();
				if (label.getLanguage().equals("")) return spelling;
				else langLabel = spelling;
			}
		}
		if (langLabel != null) return langLabel;
		return r.getLocalName();
	}
	
	/**
	    Answer a list of all the literals which are the objects of skos:prefLabel
	    or rdfs:label, with the skos labels coming first.
	*/
	public static List<Literal> labelsFor( Resource r ) {
		List<Literal> result = new ArrayList<Literal>();
		for (Statement s: r.listProperties( SKOS_prefLabel ).toList()) result.add( s.getLiteral() );
		for (Statement s: r.listProperties( RDFS.label ).toList()) result.add( s.getLiteral() );
		return result;
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
	public static Map<String, Object> getMetadataFrom( ShortNames shortNames, IdMap ids, Model m ) {
		Map<String, Object> result = new HashMap<String, Object>();		
		List<Resource> pages = m.listSubjectsWithProperty( RDF.type, API.Page ).toList();
		for (Resource p: pages) {
			List<Statement> wrs = p.listProperties( API.wasResultOf ).toList();
			for (Statement wr: wrs) {
				descend( shortNames, ids, result, "", wr.getResource() );
			}
		}
//		for (String k: result.keySet()) {
//			System.err.println( ">> meta: " + k + " => " + result.get(k) );
//		}
		return result;
	}

	private static void descend( ShortNames shortNames, IdMap ids, Map<String, Object> result, String prefix, RDFNode r) {
		if (r.isResource() && hasProperties( r.asResource() )) {
			Resource rr = r.asResource();
			for (Statement s: rr.listProperties().toList()) {
				Resource p = s.getPredicate();
				if (!p.equals( API.termBinding ) && !p.equals( API.variableBinding)) {
					String pn = shortNames.getMetaName(p);
					descend( shortNames, ids, result, (prefix.isEmpty() ? pn : prefix + "." + pn), s.getObject() );
				}
			}
		} else {
			// System.err.println( ">> " + prefix + " = " + r );
			WrappedNode.Bundle b = new WrappedNode.Bundle( shortNames, ids );
			result.put( prefix, new WrappedNode( b, r ) );
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
	public static ShortNames getShortnames( Model m ) {
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
		return new ShortNames(m, uriToShortname);
	}

	/**
	    Answer a map from variable names to their values represented as Items,
	    built from the variableBindings in the model's metadata.
	*/
	public static Map<String, WrappedNode> getVarsFrom(ShortNames names, IdMap ids, Model m) {
			Map<String, WrappedNode> varValue = new HashMap<String, WrappedNode>();
			List<Resource> pages = m.listSubjectsWithProperty( RDF.type, API.Page ).toList();
			if (pages.size() == 1) {
				List<Statement> results = pages.get(0).listProperties( API.wasResultOf ).toList();
				if (results.size() == 1) {
					List<Statement> bindings = results.get(0).getResource().listProperties( API.variableBinding ).toList();
					for (Statement sb: bindings) {
						String sn = sb.getProperty( API.label ).getString();
						RDFNode fn = sb.getProperty( API.value ).getObject();
						WrappedNode.Bundle b = new WrappedNode.Bundle( names, ids );
						varValue.put( sn, new WrappedNode( b, fn ) );
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