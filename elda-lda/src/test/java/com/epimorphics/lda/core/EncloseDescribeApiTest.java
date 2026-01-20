package com.epimorphics.lda.core;

import com.epimorphics.lda.specs.APISpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileManager;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.apache.jena.rdf.model.ModelFactory.createDefaultModel;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class EncloseDescribeApiTest {

    private Model readFromResource(String path) {
        Model model = createDefaultModel();
        try {
            try (InputStream input = this.getClass().getResource(path).openStream()) {
                model.read(input, "", "TTL");
            }
        } catch (IOException ioe) {
            fail(ioe.getMessage());
        }

        return model;
    }

    @Test
    public void apiSpec_withEncloseDescribe() {
        Model model = readFromResource("/view/api.ttl");
        Resource api = model.getResource("http://www.epimorphics.com/tools/example#encloseDescribe");
        APISpec spec = new APISpec(mock(FileManager.class), api, mock(ModelLoader.class));
        assertTrue(spec.getEncloseDescribe());
    }

    @Test
    public void apiSpec_withoutEncloseDescribe() {
        Model model = readFromResource("/view/api.ttl");
        Resource api = model.getResource("http://www.epimorphics.com/tools/example#default");
        APISpec spec = new APISpec(mock(FileManager.class), api, mock(ModelLoader.class));
        assertFalse(spec.getEncloseDescribe());
    }
}
