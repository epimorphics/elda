package com.epimorphics.lda.core.property;

import com.epimorphics.lda.shortnames.ShortnameService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Factory implements ViewProperty.Factory {

	private final Iterable<DefinitionParser> parsers;

	Factory(Iterable<DefinitionParser> parsers) {
		this.parsers = parsers;
	}

	@Override public ViewProperty getImpl(ShortnameService snr, String definition) {
		ViewProperty.Builder builder = getBuilderImpl(definition);
		return builder.build(snr);
	}

	private ViewProperty.Builder getBuilderImpl(String definition) {
		for (DefinitionParser parser : parsers) {
			Pattern pattern = parser.pattern();
			Matcher matcher = pattern.matcher(definition);

			if (matcher.matches()) {
				String name = matcher.group(1);
				return parser.parse(name);
			}
		}

		return new ViewProperty.Builder.Base(definition);
	}
}
