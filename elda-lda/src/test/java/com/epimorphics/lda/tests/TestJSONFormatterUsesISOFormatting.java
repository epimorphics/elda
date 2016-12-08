package com.epimorphics.lda.tests;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import org.codehaus.jettison.json.JSONException;
import org.junit.Test;

import com.epimorphics.lda.cache.Cache;
import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.renderers.Renderer;
import com.epimorphics.lda.renderers.Renderer.BytesOut;
import com.epimorphics.lda.support.MultiMap;
import com.epimorphics.lda.support.Times;
import com.epimorphics.util.MediaType;

public class TestJSONFormatterUsesISOFormatting {

    protected static final String TEST_BASE = "src/test/resources/api/";
    protected APITester tester = new APITester(TEST_BASE + "dateTimeApiSpec.ttl");
    protected String apiName = "api1";

    @Test
    public void testListWithISOFormattedDates() throws JSONException, IOException {
        String uriTemplate = "http://dummy/iso/persons";
        Path expectedResultFilePath = Paths.get(TEST_BASE, "testPersonListWithISOFormattedDates.json");
        Cache.Registry.clearAll();

        MultiMap<String, String> bindings = new MultiMap<String, String>();
        APIEndpoint ep = tester.router.getMatch(uriTemplate, bindings).getEndpoint();
        APIResultSet rs = tester.runQuery(uriTemplate, "");

        Renderer r = ep.getRendererByType(MediaType.APPLICATION_JSON);
        Times times = new Times();
        BytesOut response = r.render(times, ep.getSpec().getBindings().copy(), new HashMap<String, String>(), rs);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        response.writeAll(times, baos);

        String expected = new String(Files.readAllBytes(expectedResultFilePath));
        String actual = baos.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void testListWithDefaultFormattedDates() throws IOException {
        String uriTemplate = "http://dummy/default/persons";
        Path expectedResultFilePath = Paths.get(TEST_BASE, "testPersonListWithDefaultFormattedDates.json");
        Cache.Registry.clearAll();

        MultiMap<String, String> bindings = new MultiMap<String, String>();
        APIEndpoint ep = tester.router.getMatch(uriTemplate, bindings).getEndpoint();
        APIResultSet rs = tester.runQuery(uriTemplate, "");

        Renderer r = ep.getRendererByType(MediaType.APPLICATION_JSON);
        Times times = new Times();
        BytesOut response = r.render(times, ep.getSpec().getBindings().copy(), new HashMap<String, String>(), rs);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        response.writeAll(times, baos);

        String expected = new String(Files.readAllBytes(expectedResultFilePath));
        String actual = baos.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void testOnePersonWithISOFormattedDates() {
        String uriTemplate = "http://dummy/iso/persons/person_02";
        Cache.Registry.clearAll();

        MultiMap<String, String> bindings = new MultiMap<String, String>();
        APIEndpoint ep = tester.router.getMatch(uriTemplate, bindings).getEndpoint();
        APIResultSet rs = tester.runQuery(uriTemplate, "");

        Renderer r = ep.getRendererByType(MediaType.APPLICATION_JSON);
        Times times = new Times();
        BytesOut response = r.render(times, ep.getSpec().getBindings().copy(), new HashMap<String, String>(), rs);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        response.writeAll(times, baos);

        String expected = "{ \"format\" : \"linked-data-api\", \"version\" : \"0.2\", \"result\" : {\"_about\" : \"http://dummy/iso/persons/person_02\", \"definition\" : \"http://dummy/iso/persons/_personId/meta\", \"extendedMetadataVersion\" : \"http://dummy/iso/persons/person_02?_metadata=all\", \"primaryTopic\" : {\"_about\" : \"http://www.epimorphics.com/examples/eg1#person_02\", \"birthDate\" : \"2001-07-17\", \"created\" : \"2014-12-31T20:59:56Z\", \"isPrimaryTopicOf\" : \"http://dummy/iso/persons/person_02\", \"name\" : \"Person 2\", \"type\" : \"http://www.epimorphics.com/examples/eg1#Person\"}\n    , \"type\" : [\"http://purl.org/linked-data/api/vocab#ItemEndpoint\", \"http://purl.org/linked-data/api/vocab#Page\"]}\n}\n";
        String actual = baos.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void testOnePersonWithDefaultFormattedDates() {
        String uriTemplate = "http://dummy/default/persons/person_02";
        Cache.Registry.clearAll();

        MultiMap<String, String> bindings = new MultiMap<String, String>();
        APIEndpoint ep = tester.router.getMatch(uriTemplate, bindings).getEndpoint();
        APIResultSet rs = tester.runQuery(uriTemplate, "");

        Renderer r = ep.getRendererByType(MediaType.APPLICATION_JSON);
        Times times = new Times();
        BytesOut response = r.render(times, ep.getSpec().getBindings().copy(), new HashMap<String, String>(), rs);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        response.writeAll(times, baos);

        String expected = "{ \"format\" : \"linked-data-api\", \"version\" : \"0.2\", \"result\" : {\"_about\" : \"http://dummy/default/persons/person_02\", \"definition\" : \"http://dummy/default/persons/_personId/meta\", \"extendedMetadataVersion\" : \"http://dummy/default/persons/person_02?_metadata=all\", \"primaryTopic\" : {\"_about\" : \"http://www.epimorphics.com/examples/eg1#person_02\", \"birthDate\" : \"Tue, 17 Jul 2001\", \"created\" : \"Wed, 31 Dec 2014 20:59:56 GMT+0000\", \"isPrimaryTopicOf\" : \"http://dummy/default/persons/person_02\", \"name\" : \"Person 2\", \"type\" : \"http://www.epimorphics.com/examples/eg1#Person\"}\n    , \"type\" : [\"http://purl.org/linked-data/api/vocab#ItemEndpoint\", \"http://purl.org/linked-data/api/vocab#Page\"]}\n}\n";
        String actual = baos.toString();

        assertEquals(expected, actual);
    }
}
