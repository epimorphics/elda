package com.epimorphics.lda.core.property;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.lda.core.VarSupply;
import com.epimorphics.lda.rdfq.Any;
import com.epimorphics.lda.rdfq.RDFQ;

import java.util.regex.Pattern;

/**
 * Parser for inverted view properties.
 * Inverted / inverse view properties denote that resources which relate to a particular object by the given property
 * are included in the view.
 */
class InverseDefinitionParser implements DefinitionParser {
    private final Pattern pattern = Pattern.compile("^~(.+)$");

    @Override
    public Pattern pattern() {
        return pattern;
    }

    @Override
    public ViewProperty getViewProperty(String definition, ViewProperty.Factory factory) {
        ViewProperty vp = factory.getImpl(definition);
        return new InverseProperty(vp);
    }

    private class InverseProperty extends ViewProperty.Base {
        private final ViewProperty vp;

        InverseProperty(ViewProperty vp) {
            super(vp.asProperty());
            this.vp = vp;
        }

        @Override
        public RDFQ.Triple asTriple(Any subject, Any object, VarSupply vars) {
            return vp.asTriple(object, subject, vars);
        }

        @Override
        public String shortName(Context ctx) {
            return "~" + vp.shortName(ctx);
        }

        @Override
        public String toString() {
            return "~" + vp.toString();
        }
    }
}
