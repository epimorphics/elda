package com.epimorphics.lda.core;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.vocabs.API;
import com.epimorphics.vocabs.FIXUP;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.Literal;

import static com.epimorphics.util.RDFUtils.*;

/**
    Extracts and binds variables from API specifications.
 
 	@author chris
*/
public class VariableExtractor {

    static Logger log = LoggerFactory.getLogger(VariableExtractor.class);
    
	public static Map<String, RDFNode> findAndBindVariables( Resource root ) {
		return findAndBindVariables( new HashMap<String, RDFNode>(), root ); 
	}	

	public static Map<String, RDFNode> findAndBindVariables( Map<String, RDFNode> bound, Resource root) {
    	Map<String, String> toDo = new HashMap<String, String>();
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
	public static void findVariables( Resource root, Map<String, RDFNode> bound, Map<String, String> toDo ) {
		for (Statement s: root.listProperties( FIXUP.variable ).toList()) {
			Resource v = s.getResource();
			String name = getStringValue( v, API.name, null );
			RDFNode value = v.getProperty( FIXUP.value ).getObject();
			if (value.isLiteral()) {
				String lf = ((Literal) value).getLexicalForm();
				if (lf.contains( "{" ))	toDo.put( name, lf );
				else bound.put( name, value );
			} else {
				bound.put( name, value );    			
			}
		}
	}
	
	/**
	    Evaluate the variables whose lexical form contains references to other
	    variables. Their evaluated form ends up in <code>bound</code>.
	*/
	private static void doRemainingEvaluations(Map<String, RDFNode> bound,	Map<String, String> toDo) {
		for (Map.Entry<String, String> e: toDo.entrySet()) {
			String evaluated = evaluate( e.getKey(), bound, toDo );
			bound.put( e.getKey(), ResourceFactory.createPlainLiteral( evaluated ) );
		}
	}
	
	/**
	    Evaluate the variable <code>name</code>. If it is already present in
	    <code>bound</code>, then either its value is <code>null</code>, in
	    which case we are already evaluating it and we have a circularity, or
	    it is bound to its RDF value. Otherwise, recursively evaluate all
	    the variables in its value string and put them together as directed.
	 */
	private static String evaluate( String name, Map<String, RDFNode> bound, Map<String, String> toDo ) {
		if (bound.containsKey( name )) {
			RDFNode value = bound.get( name );
			if (value == null) throw new RuntimeException( "circularity in variable definitions involving " + name );
			return value.asLiteral().getLexicalForm();			
		} else {
			bound.put( name, null );
			String valueString = toDo.get( name );
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
