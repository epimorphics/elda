package com.epimorphics.lda.core.property;

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
	Boolean isCompatible(Property prop);

	interface Factory {
		ViewProperty getImpl(ShortnameService snr, String definition);
	}

	static Factory factory() {
		return new com.epimorphics.lda.core.property.Factory(
				Arrays.asList(
						new WildcardDefinitionParser(),
						new InverseDefinitionParser()
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

		@Override public Boolean isCompatible(Property prop) {
			return this.prop == prop;
		}

		@Override public String toString() {
			return prop.toString();
		}

		@Override public boolean equals(Object other) {
			return other instanceof ViewProperty && prop.equals(((ViewProperty)other).asProperty());
		}
	}

	interface Builder {
		ViewProperty build(ShortnameService snr);

		class Base implements Builder {
			private final String name;

			Base(String name) {
				this.name = name;
			}

			@Override public ViewProperty build(ShortnameService snr) {
				Property prop = getProperty(snr, name);
				return getViewProperty(prop);
			}

			private Property getProperty(ShortnameService snr, String name) {
				String fullName = snr.expand(name);
				if (fullName == null) {
					fullName = name; // name is already expanded
				}

				return ResourceFactory.createProperty(fullName);
			}

			protected ViewProperty getViewProperty(Property prop) {
				return new ViewProperty.Base(prop);
			}
		}
	}
}