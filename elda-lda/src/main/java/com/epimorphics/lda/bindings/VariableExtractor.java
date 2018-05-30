/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.bindings;


import static com.epimorphics.util.RDFUtils.getStringValue;
import static com.epimorphics.util.RDFUtils.getResourceValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.rdfq.Value;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.lda.vocabularies.ELDA_API;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
    Extracts and binds variables from API specifications.
 
 	@author chris
*/
public class VariableExtractor {
	
	static Logger log = LoggerFactory.getLogger(VariableExtractor.class);
    
	public static Bindings findAndBindVariables( Resource root ) {
		return findAndBindVariables( new Bindings(), root ); 
	}	

	public static Bindings findAndBindVariables( Bindings bound, Resource root) {
    	findVariables( root, bound );
		return bound;
	}
	
	public static final Literal nullString = ResourceFactory.createPlainLiteral("");
	
    /**
	    Find variable declarations hanging off <code>root</code>. Definitions
	    that are not literals-containing-{ are stored directly into 
	    <code>bound</code>. Otherwise, the name and its literal value are
	    stored into <code>toDo</code> for later evaluation.
	*/
	public static void findVariables( Resource root, Bindings bound ) {
		// See issue #180
		for (Statement s: root.listProperties( API.variable ).toList()) {
			Resource valueRoot = s.getResource();
			String name = getStringValue( valueRoot, API.name, null );
			String type = getStringValue( valueRoot, API.type, null );
			String language = getStringValue( valueRoot, API.lang, "" );
			
			Statement value = valueRoot.getProperty( API.value );
			RDFNode valueNode = value == null ? nullString : value.getObject();
			
			if (type == null && value != null && value.getObject().isLiteral())
				type = emptyIfNull( value.getObject().asNode().getLiteralDatatypeURI() );
			if (type == null && value != null && value.getObject().isURIResource())
				type = RDFS.Resource.getURI();
			if (type == null) {
				log.debug("no type for variable '{}'; using default ''", name);
				type = "";
			}
			
			bound.put(name, getValueFrom(valueRoot, valueNode, language, type));
		}
	}
	
	private static Value getValueFrom(Resource v, RDFNode valueNode, String language, String type) {
		Value.Apply app = Value.noApply;
		String valueString = null;
		
		if (valueNode.isAnon()) {
			valueString = getValueString( ELDA_API.mapFrom, valueNode.asResource() );
			Resource mapResource = getResourceValue( v, ELDA_API.mapWith );
			String mapName = (mapResource == null ? null : mapResource.getURI());
			app = new Value.Apply(mapName, valueString);
		} else {
			valueString = getValueString( API.value, v );
		}
		return new Value(valueString, language, type, app);
	}
	
	private static String emptyIfNull(String s) {
		return s == null ? "" : s;
	}

	private static String getValueString(Property forValue, Resource v) {
		Statement s = v.getProperty( forValue );
		if (s == null) return null;
		Node object = s.getObject().asNode();
		if (object.isURI()) return object.getURI();
		if (object.isLiteral()) return object.getLiteralLexicalForm();
		EldaException.Broken( "cannot convert " + object + " to RDFQ type." );
		return null;
	}
}
