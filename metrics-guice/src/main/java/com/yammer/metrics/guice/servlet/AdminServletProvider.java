package com.yammer.metrics.guice.servlet;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.yammer.metrics.core.*;
import com.yammer.metrics.reporting.*;

import java.util.Set;

public class AdminServletProvider implements Provider<AdminServlet> {
    private final MetricsRegistry metricsRegistry;
    private final HealthCheckRegistry healthCheckRegistry;
    private final Set<HealthCheck> healthChecks;
    private final String healthcheckUri;
    private final String metricsUri;
    private final String pingUri;
    private final String threadsUri;
    private JsonFactory jsonFactory;

    @Inject
    public AdminServletProvider(Set<HealthCheck> healthChecks,
                                MetricsRegistry metricsRegistry,
                                HealthCheckRegistry healthCheckRegistry,
                                @Named("AdminServlet.HEALTHCHECK_URI") String healthcheckUri,
                                @Named("AdminServlet.METRICS_URI") String metricsUri,
                                @Named("AdminServlet.PING_URI") String pingUri,
                                @Named("AdminServlet.THREADS_URI") String threadsUri) {
        this.metricsRegistry = metricsRegistry;
        this.healthCheckRegistry = healthCheckRegistry;
        this.healthcheckUri = healthcheckUri;
        this.metricsUri = metricsUri;
        this.pingUri = pingUri;
        this.threadsUri = threadsUri;
        this.healthChecks = healthChecks;
    }

    @Inject(optional = true)
    public void setJsonFactory(@Named("AdminServlet.JSON_FACTORY") JsonFactory jsonFactory) {
        this.jsonFactory = jsonFactory;
    }

    @Override
    public AdminServlet get() {
        for (HealthCheck healthCheck : healthChecks) {
            healthCheckRegistry.register(healthCheck);
        }

        final JsonFactory factory = jsonFactory == null ? new JsonFactory(new ObjectMapper()) : jsonFactory;

        return new AdminServlet(new HealthCheckServlet(healthCheckRegistry),
                                new MetricsServlet(Clock.defaultClock(),
                                                   VirtualMachineMetrics.getInstance(),
                                                   metricsRegistry, factory, true),

                                new PingServlet(),
                                new ThreadDumpServlet(VirtualMachineMetrics.getInstance()),
                                healthcheckUri, metricsUri, pingUri, threadsUri);
    }
}
