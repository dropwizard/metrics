package com.codahale.metrics.servlets;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 *
 * ContextListener that instantiates the registries necessary for the
 * AdminServlet to initialize correctly. Should be added to a servlet context,
 * such as in a web.xml, along with a servlet mapping for AdminServlet.
 */
public class MetricsServletContextListener implements ServletContextListener {
    public final MetricRegistry metricRegistry;
    public final HealthCheckRegistry healthCheckRegistry;

    public MetricsServletContextListener(MetricRegistry metricRegistry,
                                         HealthCheckRegistry healthCheckRegistry) {
        this.metricRegistry = metricRegistry;
        this.healthCheckRegistry = healthCheckRegistry;
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        servletContextEvent.getServletContext().setAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY,healthCheckRegistry);
        servletContextEvent.getServletContext().setAttribute(MetricsServlet.METRICS_REGISTRY, metricRegistry);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        // no-op...
    }
}
