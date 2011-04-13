/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class LanguageFilter {

	/**
	    Filter the model m according to the view-languages rules 
	    of the LDA spec.
	    
		<p>
		If the list of viewing languages contains some values, then
		the only values of a property that will be provided are those
		without a language or whose language is in that list. 
	*/
    public static void filterByLanguages( Model m, String[] split) {
    	Set<String> allowed = new HashSet<String>( Arrays.asList( split ) );
    	List<Statement> removes = new ArrayList<Statement>();
    //	
    	for (Resource sub: m.listSubjects().toSet()) {
    		for (Property prop: sub.listProperties().mapWith( Statement.Util.getPredicate ).toSet()) {
    			boolean hasLanguagedObjects = false;
    			List<Statement> plains = new ArrayList<Statement>();
    			for (Statement s: sub.listProperties( prop ).toList()) {
    				RDFNode mo = s.getObject();
    				Node o = mo.asNode();
    				if (o.isLiteral() && o.getLiteralDatatypeURI() == null) {
    					String lang = o.getLiteralLanguage();
    					if (lang.equals( "" )) {
    						plains.add( s );
    					} else if (allowed.contains( lang )) {
    						hasLanguagedObjects = true;  						
    					} else {
    						removes.add( s );
    					}
    				}
    			}
    			if (hasLanguagedObjects) removes.addAll( plains );
    		}
    	}
    //
    	m.remove( removes );
	}
}
