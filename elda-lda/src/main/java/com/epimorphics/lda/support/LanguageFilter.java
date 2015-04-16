/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.support;

import java.util.*;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.*;

/**
    This class implements the language-filtering rules of LDA views.
*/
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
        if (allowed.contains( "none" )) allowed.add( "" );
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
                if (allowed.contains( lang )) hasLanguagedObjects = true; 
                else if (lang.equals( "" )) plains.add( s );                        
                else removes.add( s );
            }
        }
        Model m = S.getModel();
		if (hasLanguagedObjects) m.remove( plains );
        m.remove( removes );        
    }
    
	private static boolean isStringLiteral(Node o) {
		return o.isLiteral() && o.getLiteralDatatypeURI() == null; 
	}
}
