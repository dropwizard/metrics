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
    private JsonFactory jsonFactory;
    private String healthcheckUri;
    private String metricsUri;
    private String pingUri;
    private String threadsUri;

    @Inject
    public MetricsServletProvider(Set<HealthCheck> healthChecks) {
        this.healthChecks = healthChecks;
    }

    @Inject(optional = true)
    public void setJsonFactory(@Named("MetricsServlet.JSON_FACTORY") JsonFactory jsonFactory) {
        this.jsonFactory = jsonFactory;
    }

    @Inject(optional = true)
    public void setUris(@Named("MetricsServlet.HEALTHCHECK_URI") String healthcheckUri,
                         @Named("MetricsServlet.METRICS_URI") String metricsUri,
                         @Named("MetricsServlet.PING_URI") String pingUri,
                         @Named("MetricsServlet.THREADS_URI") String threadsUri) {
        if ((healthcheckUri == null) || (metricsUri == null) || (pingUri == null) | (threadsUri == null)) {
            throw new IllegalArgumentException("All uris need to be specified");
        }
        this.healthcheckUri = healthcheckUri;
        this.metricsUri = metricsUri;
        this.pingUri = pingUri;
        this.threadsUri = threadsUri;
    }

    @Override
    public MetricsServlet get()
    {
        for (HealthCheck healthCheck : healthChecks) {
            HealthChecks.register(healthCheck);
        }
        if ((healthcheckUri != null) && (metricsUri != null) && (pingUri != null) && (threadsUri != null)) {
            if (jsonFactory != null) {
                return new MetricsServlet(jsonFactory, healthcheckUri, metricsUri, pingUri, threadsUri);
            }
            else {
                return new MetricsServlet(healthcheckUri, metricsUri, pingUri, threadsUri);
            }
        }
        else {
            if (jsonFactory != null) {
                return new MetricsServlet(jsonFactory);
            }
            else {
                return new MetricsServlet();
            }
        }
    }
}
