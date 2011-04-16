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

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.datatypes.xsd.impl.XSDBaseNumericType;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class LanguageFilter {
	/**
        Filter the model m according to the view-languages rules 
        of the LDA spec.
        
        <p>
        If the list of viewing languages contains some values, then
        the only untyped literal values of a property P for some subject 
        S that will kept are those with a language in the list or, if 
        there are none, those with no language. 
        </p>
    */
    public static void filterByLanguages( Model m, String[] split) {
        Set<String> allowed = new HashSet<String>( Arrays.asList( split ) );
        for (Resource sub: m.listSubjects().toList()) {
            for (Property prop: sub.listProperties().mapWith( Statement.Util.getPredicate ).toSet())
                removeUnwantedPropertyValues( allowed, sub, prop );
        }
    }

    /**
        Removes from the model the statements (S, P, ?o) whose objects are
        unwanted untyped literals.
    */
    private static void removeUnwantedPropertyValues( Set<String> allowed, Resource S, Property P ) {
    	boolean hasLanguagedObjects = false;
    	List<Statement> removes = new ArrayList<Statement>();
        List<Statement> plains = new ArrayList<Statement>();
        for (StmtIterator it = S.listProperties( P ); it.hasNext();) {
        	Statement s = it.next();
            RDFNode mo = s.getObject();
            Node o = mo.asNode();
            if (isStringLiteral(o)) {
                String lang = o.getLiteralLanguage();
                if (lang.equals( "" )) plains.add( s );
                else if (allowed.contains( lang )) hasLanguagedObjects = true;                          
                else removes.add( s );
            }
        }
        Model m = S.getModel();
		if (hasLanguagedObjects) m.remove( plains );
        m.remove( removes );        
    }

    private static final String XSDString = XSDDatatype.XSDstring.getURI();
    
	private static boolean isStringLiteral(Node o) {
		if (o.isLiteral()) {
			String type = o.getLiteralDatatypeURI();
			return type == null ; // || type.equals( XSDString );
		}
		return false;
	}
}
