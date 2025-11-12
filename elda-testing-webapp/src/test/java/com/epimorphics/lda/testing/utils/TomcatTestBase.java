package com.epimorphics.lda.testing.utils;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.ConnectException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public abstract class TomcatTestBase {

    static Logger log = LoggerFactory.getLogger(TomcatTestBase.class);

    protected static final String BASE_URL = "http://localhost:8070/";

    protected Tomcat tomcat;
    protected Connector connector;
    protected Client c;

    abstract public String getWebappRoot();

    public String getWebappContext() {
        return "/testing";
    }

    /**
     * URL to use for liveness tests
     */
    public String getTestURL() {
        return NameSupport.ensureLastSlash(BASE_URL.substring(0, BASE_URL.length() - 1) + getWebappContext());
    }

    @Before
    public void containerStart() throws Exception {
        String root = getWebappRoot();
        tomcat = new Tomcat();
        connector = new Connector();
        connector.setPort(8070);
        tomcat.getService().addConnector(connector);
        tomcat.setBaseDir(".");

        String contextPath = getWebappContext();

        File rootF = new File(root);
        if (!rootF.exists()) {
            rootF = new File(".");
        }
        if (!rootF.exists()) {
            System.err.println("Can't find root app: " + root);
            System.exit(1);
        }

        System.err.println(">> addWebapp(" + contextPath + ", " + rootF.getAbsolutePath() + ")");
        tomcat.addWebapp(contextPath, rootF.getAbsolutePath());
        tomcat.start();

        // Allow arbitrary HTTP methods so we can use PATCH
        c = ClientBuilder.newBuilder()
                .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                .build();

        checkLive(200);
    }

    @After
    public void containerStop() throws Exception {
        connector.stop();
        tomcat.stop();
        tomcat.destroy();
        try {
            checkLive(503);
        } catch (Throwable e) {
            // Can get net connection exceptions talking to dead tomcat, that's OK
        }
    }

    protected int postFileStatus(String file, String uri) {
        return postFileStatus(file, uri, "text/turtle");
    }

    protected int postFileStatus(String file, String uri, String mime) {
        return postFile(file, uri, mime).getStatus();
    }

    protected ClientResponse postFile(String file, String uri) {
        return postFile(file, uri, "text/turtle");
    }

    protected ClientResponse postFile(String file, String uri, String mime) {
        c.target(uri);
        File src = new File(file);
        return c.target(uri).request().post(Entity.entity(src, mime), ClientResponse.class);
    }

    protected ClientResponse postModel(Model m, String uri) {
        StringWriter sw = new StringWriter();
        m.write(sw, "Turtle");
        return c.target(uri).request().post(Entity.entity(sw.getBuffer().toString(), "text/turtle"), ClientResponse.class);
    }

    protected ClientResponse invoke(String method, String file, String uri, String mime) {
        if (file == null) {
            return c.target(uri).request().header("X-HTTP-Method-Override", method).post(null, ClientResponse.class);
        } else {
            File src = new File(file);
            return c.target(uri).request().header("X-HTTP-Method-Override", method).post(Entity.entity(src, mime), ClientResponse.class);
        }
    }

    protected ClientResponse post(String uri, String... paramvals) {
        WebTarget r = c.target(uri);
        for (int i = 0; i < paramvals.length; ) {
            String param = paramvals[i++];
            String value = paramvals[i++];
            r = r.queryParam(param, value);
        }
        return r.request().post(null, ClientResponse.class);
    }

    protected ClientResponse postForm(String uri, String... paramvals) {
        WebTarget r = c.target(uri);
        var formData = new Form();
        for (int i = 0; i < paramvals.length; ) {
            String param = paramvals[i++];
            String value = paramvals[i++];
            formData.param(param, value);
        }
        return r.request().buildPost(Entity.form(formData)).invoke(ClientResponse.class);
    }

    protected ClientResponse invoke(String method, String file, String uri) {
        return invoke(method, file, uri, "text/turtle");
    }

    protected Model getModelResponse(String uri, String... paramvals) {
        WebTarget r = c.target(uri);
        for (int i = 0; i < paramvals.length; ) {
            String param = paramvals[i++];
            String value = paramvals[i++];
            r = r.queryParam(param, value);
        }
        InputStream response = r.request("text/turtle").get(InputStream.class);
        Model result = ModelFactory.createDefaultModel();
        result.read(response, uri, "Turtle");
        return result;
    }

    protected Response getResponse(String uri) {
        return getResponse(uri, "text/turtle");
    }

    protected Response getResponse(String uri, String mime) {
        return c.target(uri).request(mime).get();
    }

    protected JsonObject getJSONResponse(String uri) {
        Response r = getResponse(uri, MediaType.APPLICATION_JSON);
        return JSON.parse((InputStream) r.getEntity());
    }

    protected Model checkModelResponse(String fetch, String rooturi, String file, Property... omit) {
        Model m = getModelResponse(fetch);
        Resource actual = m.getResource(rooturi);
        Resource expected = FileManager.get().loadModel(file).getResource(rooturi);
        assertTrue(expected.listProperties().hasNext());  // guard against wrong rooturi in config
        TestUtil.testResourcesMatch(expected, actual, omit);
        return m;
    }

    protected Model checkModelResponse(Model m, String rooturi, String file, Property... omit) {
        Resource actual = m.getResource(rooturi);
        Resource expected = FileManager.get().loadModel(file).getResource(rooturi);
        assertTrue(expected.listProperties().hasNext());  // guard against wrong rooturi in config
        TestUtil.testResourcesMatch(expected, actual, omit);
        return m;
    }

    protected Model checkModelResponse(Model m, String file, Property... omit) {
        Model expected = FileManager.get().loadModel(file);
        for (Resource root : expected.listSubjects().toList()) {
            if (root.isURIResource()) {
                TestUtil.testResourcesMatch(root, m.getResource(root.getURI()), omit);
            }
        }
        return m;
    }

    protected void checkLive(int targetStatus) {
        boolean tomcatLive = false;
        int count = 0;
        while (!tomcatLive) {
            String u = getTestURL() + "games.ttl";
            int status = -1;
            try {
                status = getResponse(u).getStatus();
            } catch (ProcessingException e) {
                // Could be that Tomcat is not yet up
                if (e.getCause() instanceof ConnectException) {
                    status = 503;
                } else {
                    throw e;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            log.info("[test] checkLive {}, try {}, status {}", u, count, status);
            if (status != targetStatus) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    fail("Interrupted");
                }
                if (count++ > 120) {
                    fail("Too many tries");
                }
            } else {
                tomcatLive = true;
            }
        }
    }


}
