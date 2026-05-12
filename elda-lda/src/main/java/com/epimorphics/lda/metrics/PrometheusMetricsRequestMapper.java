package com.epimorphics.lda.metrics;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

@Path("/metrics")
public class PrometheusMetricsRequestMapper {
    private final PrometheusMeterRegistry registry;

    /**
     * Constructor used to initialise from web servlet.
     * Requires PrometheusMeterRegistry attribute to be set by [PrometheusServletContextListener].
     * @param context
     */
    public PrometheusMetricsRequestMapper(@Context ServletContext context) {
        this((PrometheusMeterRegistry)context.getAttribute("PrometheusMeterRegistry"));
    }

    public PrometheusMetricsRequestMapper(PrometheusMeterRegistry registry) {
        this.registry = registry;
    }

    @GET
    @Produces("text/plain")
    public void get(
            @Context HttpServletResponse servletResponse
    ) {
        try {
            try (OutputStream output = servletResponse.getOutputStream()) {
                try (Writer w = new OutputStreamWriter(output)) {
                    registry.scrape(w);
                }
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}
