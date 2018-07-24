package com.epimorphics.lda.core.property;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class FactoryTest {
	private final List<DefinitionParser> parsers = new ArrayList<>();
	private final ViewPropertyFactory factory = new ViewPropertyFactory(parsers);

	private ViewProperty getViewProperty(String def) {
		return factory.getImpl(def);
	}

	@Test
	public void getImpl_ParserPatternMatches_ReturnsViewPropertyForCapturedDefinition() {
		DefinitionParser parser = mock(DefinitionParser.class);
		parsers.add(parser);
		ViewProperty vp = mock(ViewProperty.class);

		when(parser.pattern()).thenReturn(Pattern.compile("@(.+)@"));
		when(parser.getViewProperty(any(), any())).thenReturn(vp);

		ViewProperty result = getViewProperty("@def@");
		assertEquals(vp, result);
		verify(parser).getViewProperty("def", factory);
	}

	@Test(expected = IllegalArgumentException.class)
	public void getImpl_ParserPatternWithoutMatches_throwsException() {
		DefinitionParser parser = mock(DefinitionParser.class);
		parsers.add(parser);
		when(parser.pattern()).thenReturn(Pattern.compile("@(.+)@"));
		getViewProperty("@def");
	}

	@Test
	public void getImpl_MultipleParsers_ReturnsFirstMatchingParserViewProperty() {
		DefinitionParser parser1 = mock(DefinitionParser.class);
		DefinitionParser parser2 = mock(DefinitionParser.class);
		DefinitionParser parser3 = mock(DefinitionParser.class);

		parsers.add(parser1);
		parsers.add(parser2);
		parsers.add(parser3);

		when(parser1.pattern()).thenReturn(Pattern.compile("(.+)@"));
		when(parser2.pattern()).thenReturn(Pattern.compile("@(.+)"));
		when(parser3.pattern()).thenReturn(Pattern.compile("(.+)"));

		ViewProperty vp = mock(ViewProperty.class);
		when(parser2.getViewProperty(any(), any())).thenReturn(vp);

		ViewProperty result = getViewProperty("@def");
		assertEquals(vp, result);
		verify(parser2).getViewProperty("def", factory);
	}
}