package com.epimorphics.lda.core.property;

import com.epimorphics.lda.shortnames.ShortnameService;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import java.util.regex.Pattern;

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
			fullName = name; // name is already expanded
		}

		return ResourceFactory.createProperty(fullName);
	}
}
