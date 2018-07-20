package com.epimorphics.lda.core.property;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.lda.core.VarSupply;
import com.epimorphics.lda.rdfq.Any;
import com.epimorphics.lda.rdfq.RDFQ;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import java.util.Arrays;

public interface ViewProperty {
	Property asProperty();
	RDFQ.Triple asTriple(Any subject, Any object, VarSupply vars);
	String shortName(Context ctx);

	interface Factory {
		ViewProperty getImpl(String definition);
	}

	static Factory factory(ShortnameService sns) {
		return new com.epimorphics.lda.core.property.Factory(
				Arrays.asList(
						new InverseDefinitionParser(),
						new WildcardDefinitionParser(),
						new ShortNameDefinitionParser(sns)
				)
		);
	}

	class Base implements ViewProperty {
		protected final Property prop;

		public Base(Property prop) {
			this.prop = prop;
		}

		@Override public Property asProperty() {
			return prop;
		}

		@Override public RDFQ.Triple asTriple(Any subject, Any object, VarSupply vars) {
			return RDFQ.triple(subject, RDFQ.uri(prop), object);
		}

		@Override public String shortName(Context ctx) {
			return ctx.getNameForURI(prop.getURI());
		}

		@Override public String toString() {
			return prop.toString();
		}

		@Override public boolean equals(Object other) {
			return other instanceof ViewProperty && prop.equals(((ViewProperty)other).asProperty());
		}
	}
}