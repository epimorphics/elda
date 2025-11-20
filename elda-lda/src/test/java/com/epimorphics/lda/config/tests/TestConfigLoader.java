package com.epimorphics.lda.config.tests;

import com.epimorphics.lda.configs.ConfigLoader;
import com.epimorphics.lda.core.ModelLoader;
import com.epimorphics.lda.tests_support.FileManagerModelLoader;
import com.epimorphics.lda.vocabularies.ELDA_API;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.XSD;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestConfigLoader {

    static final Model testModel = ModelFactory.createDefaultModel();

    static final ModelLoader ml = new FileManagerModelLoader();

    static final Resource example = testModel.createResource(ELDA_API.getURI() + "example");

    static final Model testModelAgain = testModel.add(example, RDF.type, XSD.xstring);

    @Test
    public void testConfigLoader() {
        Model m = ConfigLoader.loadModelExpanding("includefiles/toplevel.ttl");
        assertTrue(m.isIsomorphicWith(testModel));
    }
}
