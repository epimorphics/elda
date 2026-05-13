package com.epimorphics.lda.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.time.Duration;

public class RequestMetricsFilter extends HttpFilter {
    private final MeterRegistry registry;

    public RequestMetricsFilter(MeterRegistry registry) {
        this.registry = registry;
    }

    private Timer requestTimer;
    private Counter successCounter;
    private Counter clientErrorCounter;
    private Counter serverErrorCounter;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        requestTimer = Timer.builder("http_request_duration")
                .description("The time taken to perform each request.")
                .sla(
                        Duration.ofMillis(30),
                        Duration.ofMillis(100),
                        Duration.ofMillis(300),
                        Duration.ofSeconds(1),
                        Duration.ofSeconds(3),
                        Duration.ofSeconds(10),
                        Duration.ofSeconds(30),
                        Duration.ofSeconds(100)
                )
                .register(registry);

        successCounter = Counter.builder("http_request_status")
                .tag("status", "2xx")
                .description("Requests completed successfully.")
                .register(registry);
        clientErrorCounter = Counter.builder("http_request_status")
                .tag("status", "4xx")
                .description("Requests rejected due to client error.")
                .register(registry);
        serverErrorCounter = Counter.builder("http_request_status")
                .tag("status", "5xx")
                .description("Requests failed due to internal error.")
                .register(registry);
    }

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        Timer.Sample timer = Timer.start();
        try {
            chain.doFilter(req, res);
        } finally {
            timer.stop(requestTimer);
            int statusCode = res.getStatus();
            Response.Status status = Response.Status.fromStatusCode(statusCode);
            Response.Status.Family family = status.getFamily();
            switch (family) {
                case Response.Status.Family.SUCCESSFUL ->  successCounter.increment();
                case Response.Status.Family.CLIENT_ERROR -> clientErrorCounter.increment();
                case Response.Status.Family.SERVER_ERROR -> serverErrorCounter.increment();
            }
        }
    }
}

