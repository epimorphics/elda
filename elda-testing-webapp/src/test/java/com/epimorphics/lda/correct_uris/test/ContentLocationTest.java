package com.epimorphics.lda.correct_uris.test;

import com.epimorphics.lda.testing.utils.TestUtil;
import com.epimorphics.lda.testing.utils.TomcatTestBase;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.lda.vocabularies.OpenSearch;
import com.epimorphics.lda.vocabularies.XHV;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import jakarta.ws.rs.core.Response;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test that the content-location header tracks the chosen rendering format.
 * Also test that the content-location has at least some of the meta-data
 * properties.
 */
public class ContentLocationTest extends TomcatTestBase {

    @Override
    public String getWebappRoot() {
        return "src/main/webapp";
    }

    @Test
    public void testContentLocationIncludesNegotiatedRenderer() {
        testContentLocationIncludesRenderer("games", "games.ttl", "?_metadata=all&_view=basic");
    }

    @Test
    public void testContentLocationIncludesExplicitTTL() {
        testContentLocationIncludesRenderer("games.ttl", "games.ttl", "?_metadata=all&_view=basic");
    }

    @Test
    public void testContentLocationIncludesExplicitTTL_Format() {
        testContentLocationIncludesRenderer("games", "games.ttl", "?_format=ttl&_metadata=all&_view=basic");
    }

    @Test
    public void testContentLocationIncludesExplicitJSON() {
        testContentLocationIncludesRenderer("games.json", "games.json", "?_metadata=all&_view=basic");
    }

    @Test
    public void testContentLocationIncludesExplicitJSON_Format() {
        testContentLocationIncludesRenderer("games", "games.json", "?_format=json&_metadata=all&_view=basic");
    }

    @Test
    public void testContentLocationIncludesExplicitXML() {
        testContentLocationIncludesRenderer("games.xml", "games.xml", "?_metadata=all&_view=basic");
    }

    @Test
    public void testContentLocationIncludesExplicitXML_Format() {
        testContentLocationIncludesRenderer("games", "games.xml", "?_format=xml&_metadata=all&_view=basic");
    }

    @Test
    public void testContentLocationIncludesExplicitHTML() {
        testContentLocationIncludesRenderer("games.html", "games.html", "?_metadata=all&_view=basic");
    }

    @Test
    public void testContentLocationIncludesExplicitHTML_Format() {
        testContentLocationIncludesRenderer("games", "games.html", "?_format=html&_metadata=all&_view=basic");
    }

    protected void testContentLocationIncludesRenderer(String provided, String expected, String query) {
        Response response = getResponse(BASE_URL + "testing/" + provided + query, "text/turtle");
        assertEquals(200, response.getStatus());

        String fullLocation = response.getHeaders().get("Content-Location").getFirst().toString();

        String shortLocation = fullLocation
                .replaceAll("http://[^/]*/", "")
                .replaceAll("\\?.*", "");

        assertEquals(expected, shortLocation);

        if (shortLocation.endsWith(".ttl")) {
            String entity = response.readEntity(String.class);
            Model result = TestUtil.modelFromTurtle(entity);
            Resource root = result.createResource(fullLocation);

            assertTrue(root.hasProperty(OpenSearch.itemsPerPage));
            assertTrue(root.hasProperty(DCTerms.hasFormat));
            assertTrue(root.hasProperty(DCTerms.hasPart));
            assertTrue(root.hasProperty(API.items));
            assertTrue(root.hasProperty(API.page));
            assertTrue(root.hasProperty(XHV.next));
        }

    }
}
