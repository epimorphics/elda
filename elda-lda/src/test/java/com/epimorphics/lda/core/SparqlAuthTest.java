package com.epimorphics.lda.core;

import com.epimorphics.lda.sources.SparqlSource;
import com.epimorphics.lda.specs.APISpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileManager;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.apache.jena.rdf.model.ModelFactory.createDefaultModel;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class SparqlAuthTest {

    @Before
    public void before() throws Exception {
        File authFile = new File("/tmp/elda/test/authz.txt");
        if (authFile.exists()) authFile.delete(); else authFile.getParentFile().mkdirs();
        Writer w = new FileWriter(authFile).append("basic.user=elda\nbasic.password=p455w0rd");
        w.close();
    }

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
    public void test() {
        Model model = readFromResource("/auth/api.ttl");
        Resource api = model.getResource("http://www.epimorphics.com/tools/example#api");
        APISpec spec = new APISpec(mock(FileManager.class), api, mock(ModelLoader.class));
        SparqlSource ds = (SparqlSource)spec.getDataSource();
        assertEquals("elda", ds.basicUser);
        assertEquals("p455w0rd", new String(ds.basicPassword));
    }
}
