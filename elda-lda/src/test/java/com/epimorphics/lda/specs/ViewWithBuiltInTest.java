package com.epimorphics.lda.specs;

import com.epimorphics.lda.core.View;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.support.PropertyChain;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static java.util.Comparator.comparing;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class ViewWithBuiltInTest {
    private Model model() {
        String ttl = """
                \
                PREFIX : <http://example.org/test/>
                PREFIX api: <http://purl.org/linked-data/api/vocab#>
                :labelledConceptViewer a api:Viewer
                    ; api:name "concept_labelled"
                    ; api:include api:labelledDescribeViewer
                    ; api:property
                      ( :broader :prefLabel ),
                      ( :narrower :prefLabel )
                    .""";
        Model m = ModelFactory.createDefaultModel();
        RDFDataMgr.read(m, new ByteArrayInputStream(ttl.getBytes(StandardCharsets.UTF_8)), Lang.TTL);
        return m;
    }

    @Test
    public void testThing() {
        ShortnameService sns = mock(ShortnameService.class);
        Model m = model();
        Resource res = m.getResource("http://example.org/test/labelledConceptViewer");
        View v = new ViewBuilder(sns).build(res);
        List<PropertyChain> pcs = v.chains().stream().sorted(comparing(PropertyChain::toString)).toList();

        assertEquals("concept_labelled", v.name());
        assertEquals(View.Type.T_ALL, v.getType());
        assertEquals(2, pcs.size());
        assertEquals("[http://example.org/test/broader, http://example.org/test/prefLabel]", pcs.get(0).toString());
        assertEquals("[http://example.org/test/narrower, http://example.org/test/prefLabel]", pcs.get(1).toString());

    }
}