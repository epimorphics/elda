package com.epimorphics.lda.renderers.velocity;

import com.epimorphics.lda.bindings.Bindings;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.*;

public class VelocityRenderingTest {

    @Test
    public void expandVelocityPath_default() {
        List<String> result = new VelocityPathScenario().run();
        List<String> expected = List.of("file:_error_pages/velocity/");
        assertEquals(expected, result);
    }

    @Test
    public void expandVelocityPath_withVelocityPath_singleValue() {
        List<String> result = new VelocityPathScenario().withVelocityPath("elda/test").run();
        List<String> expected = List.of("file:elda/test/", "file:_error_pages/velocity/");
        assertEquals(expected, result);
    }

    @Test
    public void expandVelocityPath_withVelocityPath_multipleValues() {
        List<String> result = new VelocityPathScenario().withVelocityPath("elda/test,/var/velocity").run();
        List<String> expected = List.of("file:elda/test/", "file:/var/velocity/", "file:_error_pages/velocity/");
        assertEquals(expected, result);
    }

    @Test
    public void expandVelocityPath_withVelocityPath_multipleValues_withWhitespace() {
        List<String> result = new VelocityPathScenario().withVelocityPath("elda/test , /var/velocity, elda/void ").run();
        List<String> expected = List.of("file:elda/test/", "file:/var/velocity/", "file:elda/void/", "file:_error_pages/velocity/");
        assertEquals(expected, result);
    }

    @Test
    public void expandVelocityPath_withVelocityPath_multipleValues_withEmpty() {
        List<String> result = new VelocityPathScenario().withVelocityPath(", elda/test,,/var/velocity, ,elda/void").run();
        List<String> expected = List.of("file:elda/test/", "file:/var/velocity/", "file:elda/void/", "file:_error_pages/velocity/");
        assertEquals(expected, result);
    }

    @Test
    public void expandVelocityPath_withEnvVar_singleValue() {
        List<String> result = new VelocityPathScenario().withEnvVar("elda/test").run();
        List<String> expected = List.of("file:elda/test/");
        assertEquals(expected, result);
    }

    @Test
    public void expandVelocityPath_withEnvVar_multipleValues() {
        List<String> result = new VelocityPathScenario().withEnvVar("elda/test,/var/velocity").run();
        List<String> expected = List.of("file:elda/test/", "file:/var/velocity/");
        assertEquals(expected, result);
    }

    @Test
    public void expandVelocityPath_withEnvVar_multipleValues_withWhitespace() {
        List<String> result = new VelocityPathScenario().withEnvVar("elda/test , /var/velocity, elda/void ").run();
        List<String> expected = List.of("file:elda/test/", "file:/var/velocity/", "file:elda/void/");
        assertEquals(expected, result);
    }

    @Test
    public void expandVelocityPath_withEnvVar_multipleValues_withEmpty() {
        List<String> result = new VelocityPathScenario().withEnvVar(", elda/test,,/var/velocity, ,elda/void").run();
        List<String> expected = List.of("file:elda/test/", "file:/var/velocity/", "file:elda/void/");
        assertEquals(expected, result);
    }

    private class VelocityPathScenario {
        private Bindings b = new Bindings(u -> {
            try {
                return new URL("file:" + u);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        });
        private String envRoot;

        public VelocityPathScenario withVelocityPath(String value) {
            b.put("_velocityPath", value); return this;
        }

        public VelocityPathScenario withEnvVar(String value) {
            envRoot = value; return this;
        }

        public List<String> run() {
            return VelocityRendering.expandVelocityPath(b, envRoot);
        }
    }
}