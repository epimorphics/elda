/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.shortnames;

import java.util.ArrayList;
import java.util.List;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.lda.rdfq.Any;
import com.epimorphics.lda.rdfq.Term;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
    <p>
    A ShortnameService maps shortnames to full URIs so that classes and
    properties in web APIs can refer to them compactly and legally. A
    shortname should always be both a legal NCName and a legal Javascript
    variable name.
    </p>
    
    <p>
    The shortname to URI mapping is unique but the URI to shortname
    is not, there is a preferred shortname for each URI but can also
    have non-preferred shortnames (e.g. with prefixes).
    </p>
    
    <p>
    The shortnames are defined via annotations (api:label, rdfs:label)
    in either the API specification or a referenced vocabulary file.
    Where such labels are not available we fall back on local names,
    or prefixed-local names.
    </p>
    
    <p>
    Derived from the original ShortnameService (which is now an implementation
    class, StandardShortnameService).
    </p>
    
    @author chris
*/
public interface ShortnameService 
	{
	
	/**
	    Answer a context object suitable for driving a JSON encoding. Never
	    answers null, but may answer an empty context. Typically the 
	    context was supplied when the ShortnameService object was
	    constructed.
	*/
	public Context asContext();
	
	/**
	    Answer the NameMap of this shortname service. A NameMap just maps
	    the long names to short names and back. It does not make the
	    commitments that Context does.
	*/
	public NameMap nameMap();

	/**
	    If r is a resource, answer r; if it is a literal with lexical form l,
	    answer normaliseResource(l); otherwise throw an API exception.
	*/
	public Resource normalizeResource( RDFNode r );

	/**
	    If r is a resource, answer r; if it is a literal with lexical form l,
	    answer normaliseResource(l); otherwise throw an API exception.
	*/
	public Resource normalizeResource( Term r );
	
	/**
	    Answer a resource with uri = expand(s). If there's no such expansion
	    but s "looks like" a uri, return a resource with uri = s. Otherwise
	    throw an API exception.
	*/
	public Resource normalizeResource( String s );

	/**
	    Answer a string which is the SPARQL representation of the thing
	    val treated as the object of the property prop.
	*/
	public String normalizeNodeToString( String prop, String val );

	/**
	    Answer a string which is the SPARQL representation of the thing
	    val treated as the object of the property prop. The language
	    (if non-null) will be used as the language encoding for any
	    plain literals
	*/
	public String normalizeNodeToString( String prop, String val, String language );

	/**
	    Answer a RDFQ node which has the SPARQL representation of the thing
	    val treated as the object of the property prop. The language
	    (if non-null) will be used as the language encoding for any
	    plain literals
	*/
	public Any normalizeNodeToRDFQ( String prop, String val, String language );

	/**
	    Answer a string which is the SPARQL representation of the value
	    val. If val can be expanded to a URI, it is; otherwise it is assumed
	    to be the spelling of a plain string.
	*/
	public String normalizeValue( String val );

	/**
	    Answer a string which is the SPARQL representation of the value
	    val. If val can be expanded to a URI, it is; otherwise it is assumed
	    to be the spelling of a plain string. The language
	    (if non-null) will be used as the language encoding for any
	    plain literals
	*/
	public String normalizeValue( String val, String language );

	/**
	    Answer the full name (URI) corresponding to the short name s.
	*/
	public String expand( String s );
	
	/**
	    Answer the preferred short name for the full URI u.
	*/
	public String shorten( String u );
	
	/**
	    Utilities on ShortnameService's.
	 
	 	@author chris
	*/
	public class Util
		{
		/**
		    Answer a list of properties, where each property's URI is the
		    expansion according to sns of the corresponding element in the
		    dotted string shortNames.
		*/
		public static List<Property> expandProperties( String shortNames, ShortnameService sns ) 
			{
			// System.err.println( ">> expandProperties: " + shortNames );
			String [] elements = shortNames.split( "\\." );
			List<Property> result = new ArrayList<Property>( elements.length );
			for (String e: elements) 
				if (e.length() > 0)
					{
					String expanded = sns.expand( e );
					if (expanded == null) throw new IllegalArgumentException( "no long name for '" + e + "'" );
					result.add( ResourceFactory.createProperty( expanded ) );
					}
			return result;
			}
		
		}
	}
