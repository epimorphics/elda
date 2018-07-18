package com.epimorphics.lda.core.property;

import com.epimorphics.lda.core.VarSupply;
import com.epimorphics.lda.rdfq.Any;
import com.epimorphics.lda.rdfq.RDFQ;
import com.hp.hpl.jena.rdf.model.Property;

import java.util.regex.Pattern;

class InverseDefinitionParser extends DefinitionParser.Base {

	InverseDefinitionParser() {
		super(Pattern.compile("^~(.+)$"));
	}

	@Override public ViewProperty.Builder parse(String name) {
		return new ViewProperty.Builder.Base(name) {
			@Override protected ViewProperty getViewProperty(Property prop) {
				return new InverseProperty(prop);
			}
		};
	}

	private class InverseProperty extends ViewProperty.Base {
		InverseProperty(Property prop) {
			super(prop);
		}

		@Override public RDFQ.Triple asTriple(Any subject, Any object, VarSupply vars) {
			return RDFQ.triple(object, RDFQ.uri(prop), subject);
		}
	}
}