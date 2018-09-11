package com.epimorphics.lda.core.property;

import com.epimorphics.lda.exceptions.UnknownShortnameException;
import com.epimorphics.lda.shortnames.ShortnameService;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

import java.util.regex.Pattern;

/**
 * Parser for properties which are given by their short name.
 * This parser accepts any definition and attempts to obtain a property from it.
 */
class ShortNameDefinitionParser implements DefinitionParser {
	private final Pattern pattern;
	private final ShortnameService svc;

	ShortNameDefinitionParser(ShortnameService svc) {
		this.pattern = Pattern.compile("(.+)");
		this.svc = svc;
	}

	@Override public Pattern pattern() {
		return pattern;
	}

	@Override public ViewProperty getViewProperty(String definition, ViewProperty.Factory factory) {
		Property prop = expand(definition);
		return new ViewProperty.Base(prop);
	}

	private Property expand(String name) {
		String fullName = svc.expand(name);
		if (fullName == null) {
			throw new UnknownShortnameException(name);
		}

		return ResourceFactory.createProperty(fullName);
	}
}
