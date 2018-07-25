package com.epimorphics.lda.core.property;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.lda.core.VarSupply;
import com.epimorphics.lda.rdfq.Any;
import com.epimorphics.lda.rdfq.RDFQ;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.specs.PropertyExpiryTimes;
import com.hp.hpl.jena.rdf.model.Property;

import java.util.Arrays;

/**
 * Defines a property to be rendered on a particular View, either through the API spec or request query parameters.
 */
public interface ViewProperty {
	/**
	 * @return The underlying Jena property.
	 */
	Property asProperty();

	/**
	 * @param subject The subject for which to create a triple.
	 * @param object The object for which to create a triple.
	 * @param vars The store from which to obtain new variables to be included in the triple.
	 * @return A triple which relates the subject to the object by this view property.
	 */
	RDFQ.Triple asTriple(Any subject, Any object, VarSupply vars);

	/**
	 * @param ctx The context for which to derive a short name.
	 * @return The short name associated with this property.
	 */
	String shortName(Context ctx);

	long expiryTimeMillis(PropertyExpiryTimes pet);

	/**
	 * Defines a factory which instantiates <code>ViewProperty</code> implementations.
	 */
	interface Factory {
		/**
		 * @param definition The definition of a view property, eg. from an API spec or query parameter.
		 * @return The view property derived from the definition.
		 */
		ViewProperty getImpl(String definition);
	}

	static Factory factory(ShortnameService sns) {
		return new ViewPropertyFactory(
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

		@Override public long expiryTimeMillis(PropertyExpiryTimes pet) {
			return pet.timeInMillisFor(prop);
		}
	}
}