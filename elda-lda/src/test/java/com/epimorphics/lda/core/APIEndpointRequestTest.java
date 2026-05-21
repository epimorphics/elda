package com.epimorphics.lda.core;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.support.Controls;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.*;

public class APIEndpointRequestTest {
    private final Controls c = new Controls();
    private final Bindings b = new Bindings();

    private APIEndpoint.Request request(String uri) {
        URI requestUri = URI.create(uri);
        return new APIEndpoint.Request(c, requestUri, b);
    }

    @Test
    public void getUriPlain_withUnescapedQuery_returnsUri() {
        URI result = request("http://localhost:8080/api/test?_view=all&thing.label=foo+&+bar").getURIplain();
        assertEquals("http://localhost:8080/api/test?_view=all&+bar=&thing.label=foo+", result.toString());
    }

    @Test
    public void getUriPlain_withViewParamsOnly_returnsUri() {
        URI result = request("http://localhost:8080/api/test?_view=all&label=foo").getURIplain();
        assertEquals("http://localhost:8080/api/test?_view=all&label=foo", result.toString());
    }

    @Test
    public void getUriPlain_withNonViewParams_returnsPlainUri() {
        URI result = request("http://localhost:8080/api/test?_metadata=true&_format=ttl&callback=hi&_view=all&label=foo&_mark=true").getURIplain();
        assertEquals("http://localhost:8080/api/test?_view=all&label=foo", result.toString());
    }

    @Test
    public void getUriPlain_withoutParams_returnsUri() {
        URI result = request("http://localhost:8080/api/test").getURIplain();
        assertEquals("http://localhost:8080/api/test", result.toString());
    }
}