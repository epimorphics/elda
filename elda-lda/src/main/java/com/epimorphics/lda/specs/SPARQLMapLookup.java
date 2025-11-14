package com.epimorphics.lda.specs;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.bindings.Lookup;
import com.epimorphics.lda.bindings.MapLookup;
import com.epimorphics.lda.query.APIQuery;
import com.epimorphics.lda.rdfq.Value;
import com.epimorphics.lda.sources.Source;
import com.epimorphics.lda.sources.Source.ResultSetConsumer;
import com.epimorphics.lda.support.PrefixLogger;
import com.hp.hpl.jena.query.*;

import java.util.Map;
import java.util.regex.Matcher;

public class SPARQLMapLookup implements MapLookup {

    public static final String DEFAULT_PARAM = "param";

    private final Source ds;
    private final Map<String, Element> maps;

    public static class Element {
        final String queryString;
        final String inName;
        final String outName;

        public Element(String inName, String queryString, String outName) {
            this.inName = inName;
            this.queryString = queryString;
            this.outName = outName;
        }

        public String getQueryString() {
            return queryString;
        }
    }

    public SPARQLMapLookup
            (Source ds, Map<String, Element> maps) {
        this.ds = ds;
        this.maps = maps;
    }

    @Override
    public String toString() {
        return "SourceMap";
    }

    @Override
    public String getValueString(Value.Apply apply, Value v, Bindings b, Lookup expander) {

        Element e = maps.get(apply.mapName);

        String in = e.inName;
        if (in == null) in = DEFAULT_PARAM;
        b.put(in, v.spelling());

        String[] result = new String[]{""};

        ResultSetConsumer rsc = new ResultSetConsumer() {

            @Override
            public void setup(QueryExecution qe) {
            }

            @Override
            public void consume(ResultSet rs) {
                while (rs.hasNext()) {
                    QuerySolution qs = rs.next();
                    result[0] = qs.get(e.outName).toString();
                }

            }

        };

        Query query = QueryFactory.create(injectVariables(e.queryString, b));
        ds.executeSelect(query, rsc);

        return result[0];
    }

    private String injectVariables(String q, Bindings b) {
        Matcher m = APIQuery.varPattern.matcher(q);
        StringBuilder sb = new StringBuilder();
        PrefixLogger pl = new PrefixLogger();
        int start = 0;

        while (m.find(start)) {
            String leader = q.substring(start, m.start());
            sb.append(leader);

            String name = m.group().substring(1);

            Value v = b.get(name);
            if (v == null || v.spelling().equals("")) {
                sb.append(m.group());
            } else {
                String term = v.asSparqlTerm(pl);
                sb.append(term);
            }
            start = m.end();
        }

        sb.append(q.substring(start));
        return sb.toString();
    }
}
