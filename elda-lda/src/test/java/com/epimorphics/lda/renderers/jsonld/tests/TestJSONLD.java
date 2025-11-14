package com.epimorphics.lda.renderers.jsonld.tests;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.jsonrdf.JSONWriterFacade;
import com.epimorphics.jsonrdf.JSONWriterWrapper;
import com.epimorphics.jsonrdf.ReadContext;
import com.epimorphics.lda.renderers.JSONLDComposer;
import com.hp.hpl.jena.rdf.model.*;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.fail;

/**
 * Tests for JSON LD output. Very partial.
 */
public class TestJSONLD {

    final Model model = ModelFactory.createDefaultModel();
    final Resource root = model.createResource("eh:/root");
    final Resource item = model.createResource("eh:/item");
    final Property prop = model.createProperty("eh:/prop");

    final Literal ll = model.createLiteral("chat", "fr");

    /**
     * Test that a literal with language code is correctly
     * translated (Issue #180).
     */
    @Test
    public void testRendersLanguagedLiteral() throws UnsupportedEncodingException {
        model.add(item, prop, ll);
        ReadContext context = new Context();
        Map<String, String> termBindings = new HashMap<String, String>();
        boolean allStructured = false;

        ByteArrayOutputStream s = new ByteArrayOutputStream();
        Writer w = new OutputStreamWriter(s, "UTF-8");
        JSONWriterFacade jw = new JSONWriterWrapper(w, true);

        JSONLDComposer jc = new JSONLDComposer
                (model
                        , root
                        , context
                        , termBindings
                        , allStructured
                        , jw
                );

        jc.renderItems(Arrays.asList(item));
        String content = s.toString();
        if (!content.contains("{\"@lang\" : \"fr\", \"@value\" : \"chat\"}"))
            fail("");
    }

}
