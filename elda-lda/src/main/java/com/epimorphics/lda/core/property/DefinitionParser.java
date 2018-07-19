package com.epimorphics.lda.core.property;

import java.util.regex.Pattern;

interface DefinitionParser {
	Pattern pattern();
	ViewProperty getViewProperty(String definition, ViewProperty.Factory factory);
}
