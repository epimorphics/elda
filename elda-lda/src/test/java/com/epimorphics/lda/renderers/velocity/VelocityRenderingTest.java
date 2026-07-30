package com.epimorphics.lda.renderers.velocity;

import com.epimorphics.lda.bindings.Bindings;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.*;

public class VelocityRenderingTest {

    @Test
    public void expandVelocityPath_default() {
        List<String> result = new VelocityPathScenario().run();
        List<String> expected = List.of("http://_error_pages/", "http:/velocity/");
        assertEquals(expected, result);
    }

    @Test
    public void expandVelocityPath_withVelocityPath_singleValue() {
        List<String> result = new VelocityPathScenario().withVelocityPath("elda/test").run();
        List<String> expected = List.of("http://elda/test/", "http://_error_pages/", "http:/velocity/");
        assertEquals(expected, result);
    }

    @Test
    public void expandVelocityPath_withVelocityPath_multipleValues() {
        List<String> result = new VelocityPathScenario().withVelocityPath("elda/test,/var/velocity").run();
        List<String> expected = List.of("http://elda/test/", "http:/var/velocity/", "http://_error_pages/", "http:/velocity/");
        assertEquals(expected, result);
    }

    @Test
    public void expandVelocityPath_withVelocityPath_multipleValues_withWhitespace() {
        List<String> result = new VelocityPathScenario().withVelocityPath("elda/test , /var/velocity, elda/void ").run();
        List<String> expected = List.of("http://elda/test/", "http:/var/velocity/", "http://elda/void/", "http://_error_pages/", "http:/velocity/");
        assertEquals(expected, result);
    }

    @Test
    public void expandVelocityPath_withVelocityPath_multipleValues_withEmpty() {
        List<String> result = new VelocityPathScenario().withVelocityPath(", elda/test,,/var/velocity, ,elda/void").run();
        List<String> expected = List.of("http://elda/test/", "http:/var/velocity/", "http://elda/void/", "http://_error_pages/", "http:/velocity/");
        assertEquals(expected, result);
    }

    @Test
    public void expandVelocityPath_withEnvVar_singleValue() {
        List<String> result = new VelocityPathScenario().withEnvVar("elda/test").run();
        List<String> expected = List.of("http://_error_pages/", "http://elda/test/");
        assertEquals(expected, result);
    }

    @Test
    public void expandVelocityPath_withEnvVar_multipleValues() {
        List<String> result = new VelocityPathScenario().withEnvVar("elda/test,/var/velocity").run();
        List<String> expected = List.of("http://_error_pages/", "http://elda/test/", "http:/var/velocity/");
        assertEquals(expected, result);
    }

    @Test
    public void expandVelocityPath_withEnvVar_multipleValues_withWhitespace() {
        List<String> result = new VelocityPathScenario().withEnvVar("elda/test , /var/velocity, elda/void ").run();
        List<String> expected = List.of("http://_error_pages/", "http://elda/test/", "http:/var/velocity/", "http://elda/void/");
        assertEquals(expected, result);
    }

    @Test
    public void expandVelocityPath_withEnvVar_multipleValues_withEmpty() {
        List<String> result = new VelocityPathScenario().withEnvVar(", elda/test,,/var/velocity, ,elda/void").run();
        List<String> expected = List.of("http://_error_pages/", "http://elda/test/", "http:/var/velocity/", "http://elda/void/");
        assertEquals(expected, result);
    }

    private class VelocityPathScenario {
        private Bindings b = new Bindings(u -> {
            try {
                return new URL("http://" + u);
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