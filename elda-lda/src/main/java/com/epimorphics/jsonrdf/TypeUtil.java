/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$

    File:        TypeUtil.java
    Created by:  Dave Reynolds
    Created on:  5 Feb 2010
*/

package com.epimorphics.jsonrdf;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.TriplePattern;
import org.apache.jena.reasoner.transitiveReasoner.TransitiveGraphCache;
import org.apache.jena.reasoner.transitiveReasoner.TransitiveReasoner;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

/**
 * Support for comparing datatypes to reflect XSD type heirarchy
 *
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public class TypeUtil {

    static TransitiveGraphCache tgc;

    static {
        Node sc = RDFS.subClassOf.asNode();
        tgc = new TransitiveGraphCache(TransitiveReasoner.directSubClassOf, sc);
        addSubClass(tgc, XSD.integer, XSD.decimal);
        addSubClass(tgc, XSD.xlong, XSD.integer);
        addSubClass(tgc, XSD.xint, XSD.xlong);
        addSubClass(tgc, XSD.xshort, XSD.xint);
        addSubClass(tgc, XSD.xbyte, XSD.xshort);
        addSubClass(tgc, XSD.unsignedByte, XSD.xshort);
        addSubClass(tgc, XSD.unsignedInt, XSD.xlong);
        addSubClass(tgc, XSD.unsignedShort, XSD.xint);
        addSubClass(tgc, XSD.unsignedLong, XSD.integer);
        addSubClass(tgc, XSD.NCName, XSD.xstring);
        addSubClass(tgc, XSD.token, XSD.xstring);
        addSubClass(tgc, XSD.ENTITY, XSD.xstring);
        addSubClass(tgc, XSD.ID, XSD.xstring);
        addSubClass(tgc, XSD.IDREF, XSD.xstring);
        addSubClass(tgc, XSD.NMTOKEN, XSD.xstring);
    }

    static void addSubClass(TransitiveGraphCache tgc, Resource sub, Resource sup) {
        tgc.addRelation(Triple.create(sub.asNode(), RDFS.subClassOf.asNode(), sup.asNode()));
    }

    public static boolean isSubTypeOf(Resource a, Resource b) {
        return tgc.contains(new TriplePattern(a.asNode(), RDFS.subClassOf.asNode(), b.asNode()));
    }

    public static boolean isSubTypeOf(String aUri, String bUri) {
        return tgc.contains(new TriplePattern(NodeFactory.createURI(aUri), RDFS.subClassOf.asNode(), NodeFactory.createURI(bUri)));
    }
}
