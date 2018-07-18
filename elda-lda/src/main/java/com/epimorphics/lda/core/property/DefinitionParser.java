package com.epimorphics.lda.core.property;

import java.util.regex.Pattern;

interface DefinitionParser {
	Pattern pattern();
	ViewProperty.Builder parse(String name);

	abstract class Base implements DefinitionParser {
		private final Pattern pattern;

		Base(Pattern pattern) {
			this.pattern = pattern;
		}

		@Override public Pattern pattern() {
			return pattern;
		}
	}
}