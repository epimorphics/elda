package com.epimorphics.lda.core;

import com.epimorphics.lda.sources.Source;
import com.epimorphics.lda.support.Controls;
import com.epimorphics.lda.support.Times;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.apache.jena.rdf.model.ModelFactory.createDefaultModel;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EncloseDescribeTest {
    private final Controls c = new Controls(false, mock(Times.class));
    private final Source s = mock(Source.class);

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

    private void assertIsomorphic(Model expected, Model actual) {
        if (!expected.isIsomorphicWith(actual)) {
            assertEquals(expected, actual);
        }
    }

    @Test
    public void describeView_fetchDescriptions_withReverseClosure_returnsForwardClosure() {
        Model describe = readFromResource("/view/bw_reverse_closure.ttl");
        when(s.executeDescribe(any())).thenReturn(describe);

        View v = new View("test", View.Type.T_DESCRIBE);

        List<Resource> roots = List.of(createResource("http://environment.data.gov.uk/id/bathing-water/"));
        Model result  = createDefaultModel();
        List<Source> sources = List.of(s);
        VarSupply vs = mock(VarSupply.class);

        View.State state = new View.State(roots, result, sources, vs, "#graph");


        v.fetchDescriptions(c, state, true);

        Model expected = readFromResource("/view/bw_closure.ttl");
        assertIsomorphic(expected, result);
    }

    @Test
    public void allView_fetchDescriptions_withReverseClosure_returnsForwardClosure() {
        Model describe = readFromResource("/view/bw_reverse_closure.ttl");
        when(s.executeDescribe(any())).thenReturn(describe);
        when(s.executeConstruct(any())).thenReturn(createDefaultModel());

        View v = new View("test", View.Type.T_ALL);

        List<Resource> roots = List.of(createResource("http://environment.data.gov.uk/id/bathing-water/"));
        Model result  = createDefaultModel();
        List<Source> sources = List.of(s);
        VarSupply vs = mock(VarSupply.class);

        View.State state = new View.State(roots, result, sources, vs, "#graph");


        v.fetchDescriptions(c, state, true);

        Model expected = readFromResource("/view/bw_closure.ttl");
        assertIsomorphic(expected, result);
    }
}
