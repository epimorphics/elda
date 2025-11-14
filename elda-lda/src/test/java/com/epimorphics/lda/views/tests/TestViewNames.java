package com.epimorphics.lda.views.tests;

import com.epimorphics.lda.core.View;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestViewNames {

    @Test
    public void testKeepsNames() {
        View a = new View("nemo"), b = new View("anon");
        assertEquals("nemo", a.name());
        assertEquals("anon", b.name());
    }

    @Test
    public void testKeepsNamesUnderCopy() {
        View a = new View("nemo"), b = new View("anon");
        assertEquals("nemo", a.nameWithoutCopy());
        assertEquals("anon", b.nameWithoutCopy());
    }

    // TODO consider what happens to a copy of a copy.
    @Test
    public void testAddsCopySuffix() {
        View a = new View("monalisa");
        View b = a.copy();
        assertEquals("monalisa.copy", b.name());
        assertEquals("monalisa", b.nameWithoutCopy());
    }
}
