/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.bindings;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.vocabs.API;
import com.epimorphics.vocabs.FIXUP;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDFS;

import static com.epimorphics.util.RDFUtils.*;

/**
    Extracts and binds variables from API specifications.
 
 	@author chris
*/
public class VariableExtractor {
	
	static Logger log = LoggerFactory.getLogger(VariableExtractor.class);
    
	public static VarValues findAndBindVariables( Resource root ) {
		return findAndBindVariables( new VarValues(), root ); 
	}	

	public static VarValues findAndBindVariables( VarValues bound, Resource root) {
    	VarValues toDo = new VarValues();
    	findVariables( root, bound, toDo );
    	doRemainingEvaluations( bound, toDo );
		return bound;
	}
	
    /**
	    Find variable declarations hanging off <code>root</code>. Definitions
	    that are not literals-containing-{ are stored directly into 
	    <code>bound</code>. Otherwise, the name and its literal value are
	    stored into <code>toDo</code> for later evaluation.
	*/
	public static void findVariables( Resource root, VarValues bound, VarValues toDo ) {
		for (Statement s: root.listProperties( FIXUP.variable ).toList()) {
			Resource v = s.getResource();
			String name = getStringValue( v, API.name, null );
			String language = getStringValue( v, FIXUP.lang, "" );
			String type = getStringValue( v, FIXUP.type, "" );
			Statement value = v.getProperty( FIXUP.value );
			if (value != null && value.getObject().isLiteral())
				type = emptyIfNull( value.getObject().asNode().getLiteralDatatypeURI() );
			if (value != null && value.getObject().isURIResource())
				type = RDFS.Resource.getURI();
			if (type == null){
				log.warn("type mysteriously null, needs sorting soon" );
				type = "";
			}
			String valueString = getValueString( v, language, type );
			Value var = new Value( valueString, language, type );
			(valueString.contains( "{" ) ? toDo : bound).put( name, var ); 			
			}
		}

	private static String emptyIfNull(String s) {
		return s == null ? "" : s;
	}

	private static String getValueString(Resource v, String language, String type) {
		Statement s = v.getProperty( FIXUP.value );
		if (s == null) return null;
		Node object = s.getObject().asNode();
		if (object.isURI()) return object.getURI();
		if (object.isLiteral()) return object.getLiteralLexicalForm();
		EldaException.Broken( "cannot convert " + object + " to RDFQ type." );
		return null;
	}
	
	/**
	    Evaluate the variables whose lexical form contains references to other
	    variables. Their evaluated form ends up in <code>bound</code>.
	*/
	private static void doRemainingEvaluations( VarValues bound, VarValues toDo ) {
		for (String name: toDo.keySet()) {
			String evaluated = evaluate( name, bound, toDo );
			bound.put( name, toDo.get( name ).withValueString( evaluated ) );
		}
	}
	
	/**
	    Evaluate the variable <code>name</code>. If it is already present in
	    <code>bound</code>, then either its value is <code>null</code>, in
	    which case we are already evaluating it and we have a circularity, or
	    it is bound to its RDF value. Otherwise, recursively evaluate all
	    the variables in its value string and put them together as directed.
	 */
	private static String evaluate( String name, VarValues bound, VarValues toDo ) {
		if (bound.hasVariable( name )) {
			Value v = bound.get( name );
			if (v == null) EldaException.BadSpecification( "circularity in variable definitions involving " + name );
			return v.valueString();			
		} else {
			bound.put( name, (Value) null );
			Value x = toDo.get( name );			
			String valueString = x.valueString();
			if (valueString == null) {
				log.warn( "no value for variable " + name );
				valueString = "(no value for " + name + ")";
			}
			StringBuilder value = new StringBuilder();
			int anchor = 0;
			while (true) {
				int lbrace = valueString.indexOf( '{', anchor );
				if (lbrace < 0) break;
				int rbrace = valueString.indexOf( '}', lbrace );
				value.append( valueString.substring( anchor, lbrace ) );
				String innerName = valueString.substring( lbrace + 1, rbrace );
				String evaluated = evaluate( innerName, bound, toDo );
				value.append( evaluated );
				anchor = rbrace + 1;
			}
			value.append( valueString.substring( anchor ) );
			String result = value.toString();
			return result;			
		}
	}
}
