package com.epimorphics.lda.core.property;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.lda.core.VarSupply;
import com.epimorphics.lda.rdfq.Any;
import com.epimorphics.lda.rdfq.RDFQ;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import java.util.regex.Pattern;

class WildcardDefinitionParser implements DefinitionParser {
	private final Pattern pattern = Pattern.compile("^(\\*)$");

	@Override public Pattern pattern() {
		return pattern;
	}

	@Override public ViewProperty getViewProperty(String definition, ViewProperty.Factory factory) {
		return new WildcardProperty();
	}

	private class WildcardProperty extends ViewProperty.Base {
		WildcardProperty() {
			super(ResourceFactory.createProperty( "_magic:ANY" ));
		}

		@Override public RDFQ.Triple asTriple(Any subject, Any object, VarSupply vars) {
			return RDFQ.triple(subject, vars.newVar(), object);
		}

		@Override public String shortName(Context ctx) {
			return "ANY";
		}
	}
}
