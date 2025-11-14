package com.epimorphics.lda.core.property;

import java.util.regex.Pattern;

/**
 * Defines a rule for parsing a view property definition.
 * Implementations may parse the entire definition, or they may parse part of the definition and delegate the rest.
 */
interface DefinitionParser {
    /**
     * @return The pattern for definitions which the parser accepts. This MUST contain a single capturing group which
     * captures the part of the definition that should be passed to <code>getViewProperty</code>.
     */
    Pattern pattern();

    /**
     * @param definition A property definition for which to create a <code>ViewProperty/code>.
     * @param factory    A factory implementation, which may be used to parse the definition recursively.
     * @return The view property implementation derived from the property definition.
     */
    ViewProperty getViewProperty(String definition, ViewProperty.Factory factory);
}
