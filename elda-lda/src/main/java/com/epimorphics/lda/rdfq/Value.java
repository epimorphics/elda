/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.rdfq;

import com.epimorphics.lda.bindings.Lookup;
import com.epimorphics.lda.support.PrefixLogger;
import com.epimorphics.util.RDFUtils;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.util.FmtUtils;
import org.apache.jena.vocabulary.XSD;

public class Value extends Term {
    final String spelling;
    final String language;
    final String datatype;

    // final String mapName;

    public static class Apply {
        public final String mapName;
        public final String argument;

        public Apply(String mapName, String argument) {
            this.mapName = mapName;
            this.argument = argument;
        }

        @Override
        public String toString() {
            return "(" + mapName + " | " + argument + ")";
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Apply && same((Apply) other);
        }

        private boolean same(Apply other) {
            return mapName.equals(other.mapName) && argument.equals(other.argument);
        }
    }

    public final Apply apply;

    public static final Value emptyPlain = new Value("");

    public static final Apply noApply = null;

    public Value(String spelling) {
        this(spelling, "", XSD.xstring.toString(), noApply);
    }

    public Value(String spelling, String language, String datatype, Apply apply) {
        this.spelling = spelling == null ? "" : spelling;
        this.language = language == null ? "" : language;
        this.datatype = datatype == null ? "" : datatype;
        this.apply = apply;
    }

    @Override
    public String toString() {
        return "{" + spelling + "|" + language + "|" + datatype + "|" + apply + "}";
    }

    @Override
    public String asSparqlTerm(PrefixLogger pl) {
        String lang = (language.equals("none") ? "" : language);
        RDFDatatype dt = datatype.length() == 0 ? null : TypeMapper.getInstance().getSafeTypeByName(datatype);
        Node n = NodeFactory.createLiteral(spelling, lang, dt);
        if (datatype.length() > 0) pl.present(datatype);
        String lf = FmtUtils.stringForNode(n, RDFUtils.noPrefixes);
        return lf;
    }

    /**
     * Answer a new Value with the same language and datatype as this
     * one, but with a new lexical form aka valueString vs.
     */
    @Override
    public Value replaceBy(String vs) {
        return new Value(vs, language, datatype, apply);
    }

    @Override
    public String spelling() {
        return spelling;
    }

    public String spelling(Lookup l) {
        if (apply != null) {
            String vs = l.getValueString(spelling);
            return vs == null ? "ABSENT" : vs;
        }
        return spelling;
    }

    public String lang() {
        return language;
    }

    public String type() {
        return datatype;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Value && same((Value) other);
    }

    @Override
    public int hashCode() {
        return spelling.hashCode() + language.hashCode() + datatype.hashCode();
    }

    private boolean same(Value other) {
        return
                spelling.equals(other.spelling)
                        && language.equals(other.language)
                        && datatype.equals(other.datatype)
                        && same(apply, other.apply)
                ;
    }

    private boolean same(Apply x, Apply y) {
        return x == null ? y == null : x.equals(y);
    }
}
