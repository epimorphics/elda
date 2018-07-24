package com.epimorphics.lda.core.property;

import com.epimorphics.lda.core.VarSupply;
import com.epimorphics.lda.rdfq.Any;
import com.epimorphics.lda.rdfq.RDFQ;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.hp.hpl.jena.rdf.model.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ViewPropertyFactory implements ViewProperty.Factory {

	private final List<DefinitionParser> parsers;

	ViewPropertyFactory(List<DefinitionParser> parsers) {
		this.parsers = parsers;
	}

	@Override public ViewProperty getImpl(String definition) {
		for (DefinitionParser parser : parsers) {
			Matcher matcher = parser.pattern().matcher(definition);
			if (matcher.matches()) {
				definition = matcher.group(1);
				return parser.getViewProperty(definition, this);
			}
		}

		throw new IllegalArgumentException("Unable to parse property definition: " + definition);
	}
}
