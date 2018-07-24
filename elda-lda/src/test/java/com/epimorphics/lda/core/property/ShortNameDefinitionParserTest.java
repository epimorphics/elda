package com.epimorphics.lda.core.property;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.lda.core.VarSupply;
import com.epimorphics.lda.rdfq.Any;
import com.epimorphics.lda.rdfq.RDFQ;
import com.epimorphics.lda.rdfq.URINode;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.hp.hpl.jena.rdf.model.Property;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ShortNameDefinitionParserTest {

	private final ShortnameService svc = mock(ShortnameService.class);
	private final ShortNameDefinitionParser parser = new ShortNameDefinitionParser(svc);
	private final ViewProperty.Factory factory = mock(ViewProperty.Factory.class);
	private final Context ctx = mock(Context.class);
	private final String definition = "def";
	private final String uri = "http://test.org/def";

	public ShortNameDefinitionParserTest() {
		when(svc.expand(definition)).thenReturn(uri);
	}

	private ViewProperty getViewProperty() {
		return parser.getViewProperty(definition, factory);
	}

	@Test
	public void pattern_isAnyCharacterSequence() {
		String pattern = parser.pattern().pattern();
		assertEquals("(.+)", pattern);
	}

	@Test
	public void viewProperty_shortName_ReturnsShortName() {
		when(ctx.getNameForURI(uri)).thenReturn("defn");
		String name = getViewProperty().shortName(ctx);
		assertEquals("defn", name);
	}

	@Test
	public void viewProperty_asTriple_ReturnsRelationAsTriple() {
		Any subject = mock(Any.class);
		Any object = mock(Any.class);
		VarSupply vars = mock(VarSupply.class);

		RDFQ.Triple triple = getViewProperty().asTriple(subject, object, vars);
		assertEquals(subject, triple.S);
		assertEquals(uri, ((URINode)triple.P).spelling());
		assertEquals(object, triple.O);
	}

	@Test
	public void viewProperty_toString_ReturnsUri() {
		String result = getViewProperty().toString();
		assertEquals(uri, result);
	}

	@Test
	public void viewProperty_asProperty_ReturnsProperty() {
		Property prop = getViewProperty().asProperty();
		assertEquals(uri, prop.getURI());
	}
}