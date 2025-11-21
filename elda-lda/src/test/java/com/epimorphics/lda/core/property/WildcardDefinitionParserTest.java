package com.epimorphics.lda.core.property;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.lda.core.VarSupply;
import com.epimorphics.lda.rdfq.Any;
import com.epimorphics.lda.rdfq.RDFQ;
import com.epimorphics.lda.rdfq.Variable;
import org.apache.jena.rdf.model.Property;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class WildcardDefinitionParserTest {

    private final WildcardDefinitionParser parser = new WildcardDefinitionParser();
    private final ViewProperty.Factory factory = mock(ViewProperty.Factory.class);
    private final Context ctx = mock(Context.class);

    private ViewProperty getViewProperty() {
        return parser.getViewProperty("*", factory);
    }

    @Test
    public void pattern_isSingleAsterisk() {
        String regex = parser.pattern().pattern();
        assertEquals("^(\\*)$", regex);
    }

    @Test
    public void viewProperty_ShortName_isAny() {
        String name = getViewProperty().shortName(ctx);
        assertEquals("ANY", name);
    }

    @Test
    public void viewProperty_asTriple_ReturnsAnyRelation() {
        Any subject = mock(Any.class);
        Any object = mock(Any.class);

        Variable newVar = mock(Variable.class);
        VarSupply vars = mock(VarSupply.class);
        Mockito.when(vars.newVar()).thenReturn(newVar);

        RDFQ.Triple triple = getViewProperty().asTriple(subject, object, vars);
        assertEquals(subject, triple.S);
        assertEquals(object, triple.O);
        assertEquals(newVar, triple.P);
    }

    @Test
    public void viewProperty_toString_ReturnsAny() {
        String str = getViewProperty().toString();
        assertEquals("_magic:ANY", str);
    }

    @Test
    public void viewProperty_asProperty_ReturnsMagicProperty() {
        Property prop = getViewProperty().asProperty();
        assertEquals("_magic:ANY", prop.getURI());
    }
}
