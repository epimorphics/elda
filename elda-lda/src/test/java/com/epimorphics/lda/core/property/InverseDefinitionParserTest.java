package com.epimorphics.lda.core.property;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.lda.core.VarSupply;
import com.epimorphics.lda.rdfq.Any;
import com.epimorphics.lda.rdfq.RDFQ;
import com.hp.hpl.jena.rdf.model.Property;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InverseDefinitionParserTest {

	private final InverseDefinitionParser parser = new InverseDefinitionParser();
	private final ViewProperty.Factory factory = mock(ViewProperty.Factory.class);
	private final ViewProperty vp = mock(ViewProperty.class);
	private final Property prop = mock(Property.class);
	private final Context ctx = mock(Context.class);
	private final String definition = "def";

	public InverseDefinitionParserTest() {
		when(factory.getImpl(definition)).thenReturn(vp);
		when(vp.asProperty()).thenReturn(prop);
	}

	private ViewProperty getViewProperty() {
		return parser.getViewProperty(definition, factory);
	}

	@Test
	public void pattern_startsWithTilde() {
		String pattern = parser.pattern().pattern();
		assertEquals("^~(.+)$", pattern);
	}

	@Test
	public void viewProperty_shortName_ReturnsShortName() {
		when(vp.shortName(ctx)).thenReturn("def");
		String name = getViewProperty().shortName(ctx);
		assertEquals("~def", name);
	}

	@Test
	public void viewProperty_asTriple_TransposesSubjectAndObject() {
		Any subject = mock(Any.class);
		Any object = mock(Any.class);
		VarSupply vars = mock(VarSupply.class);
		RDFQ.Triple triple = mock(RDFQ.Triple.class);

		when(vp.asTriple(object, subject, vars)).thenReturn(triple);
		RDFQ.Triple result = getViewProperty().asTriple(subject, object, vars);
		assertEquals(triple, result);
	}

	@Test
	public void viewProperty_toString_PrefixesWithTilde() {
		when(vp.toString()).thenReturn("defn");
		String result = getViewProperty().toString();
		assertEquals("~defn", result);
	}

	@Test
	public void viewProperty_asProperty_delegatesToViewProperty() {
		Property prop = getViewProperty().asProperty();
		assertEquals(this.prop, prop);
	}
}