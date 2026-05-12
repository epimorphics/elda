package com.epimorphics.lda.metrics;

import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.jvm.*;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import java.util.Arrays;
import java.util.List;

public class PrometheusServletContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        RequestMetricsFilter filter = new RequestMetricsFilter(registry);
        ServletContext ctx = sce.getServletContext();
        ctx.addFilter("RequestMetricsFilter", filter);
        ctx.setAttribute("PrometheusMeterRegistry", registry);

        initJvmMetrics(ctx, registry);
    }

    private void initJvmMetrics(ServletContext ctx, PrometheusMeterRegistry registry) {
        String attr = ctx.getInitParameter("enableJvmMetrics");
        boolean enableJvmMetrics = attr != null && attr.equals("true");

        if (enableJvmMetrics) {
            List<MeterBinder> binders = Arrays.asList(
                    new JvmMemoryMetrics(),
                    new JvmGcMetrics(),
                    new JvmThreadMetrics()
            );
            binders.forEach((MeterBinder binder) -> binder.bindTo(registry));
        }
    }
}
