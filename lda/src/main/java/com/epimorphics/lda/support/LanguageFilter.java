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
        the only untyped literal values of a property P for some subject 
        S that will kept are those with a language in the list or, if 
        there are none, those with no language. 
        </p>
    */
    public static void filterByLanguages( Model m, String[] split) {
        Set<String> allowed = new HashSet<String>( Arrays.asList( split ) );
        for (Resource sub: m.listSubjects().toSet()) {
            List<Statement> removes = new ArrayList<Statement>();
            for (Property prop: sub.listProperties().mapWith( Statement.Util.getPredicate ).toSet())
                findUnwantedStrings( allowed, sub, removes, prop );
            m.remove( removes );
        }
    }

    /**
        Add to <code>removes</code> the statements whose objects are
        unwanted untyped literals.
    */
    private static void findUnwantedStrings( Set<String> allowed, Resource sub, List<Statement> removes, Property prop ) {
        boolean hasLanguagedObjects = false;
        List<Statement> plains = new ArrayList<Statement>();
        for (Statement s: sub.listProperties( prop ).toList()) {
            RDFNode mo = s.getObject();
            Node o = mo.asNode();
            if (o.isLiteral() && o.getLiteralDatatypeURI() == null) {
                String lang = o.getLiteralLanguage();
                if (lang.equals( "" )) plains.add( s );
                else if (allowed.contains( lang )) hasLanguagedObjects = true;                          
                else removes.add( s );
            }
        }
        if (hasLanguagedObjects) removes.addAll( plains );
    }
}
