package com.yammer.metrics.reporting.guice;

import java.util.Set;

import org.codehaus.jackson.JsonFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.yammer.metrics.HealthChecks;
import com.yammer.metrics.core.HealthCheck;
import com.yammer.metrics.reporting.MetricsServlet;

public class MetricsServletProvider implements Provider<MetricsServlet>
{
    private final Set<HealthCheck> healthChecks;
    private final String healthcheckUri;
    private final String metricsUri;
    private final String pingUri;
    private final String threadsUri;
    private JsonFactory jsonFactory;

    @Inject
    public MetricsServletProvider(Set<HealthCheck> healthChecks,
                                    @Named("MetricsServlet.HEALTHCHECK_URI") String healthcheckUri,
                                    @Named("MetricsServlet.METRICS_URI") String metricsUri,
                                    @Named("MetricsServlet.PING_URI") String pingUri,
                                    @Named("MetricsServlet.THREADS_URI") String threadsUri) {
        this.healthcheckUri = healthcheckUri;
        this.metricsUri = metricsUri;
        this.pingUri = pingUri;
        this.threadsUri = threadsUri;
        this.healthChecks = healthChecks;
    }

    @Inject(optional = true)
    public void setJsonFactory(@Named("MetricsServlet.JSON_FACTORY") JsonFactory jsonFactory) {
        this.jsonFactory = jsonFactory;
    }

    @Override
    public MetricsServlet get()
    {
        for (HealthCheck healthCheck : healthChecks) {
            HealthChecks.register(healthCheck);
        }
        if (jsonFactory != null) {
            return new MetricsServlet(jsonFactory, healthcheckUri, metricsUri, pingUri, threadsUri);
        }
        else {
            return new MetricsServlet(healthcheckUri, metricsUri, pingUri, threadsUri);
        }
    }
}
