package com.codahale.metrics.servlets;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 *
 * Example ContextListener that instantiates the registries necessary for the
 * AdminServlet to initialize correctly. Should be added to a servlet context,
 * such as in a web.xml, along with a servlet mapping for AdminServlet.
 *
 */
public class MetricsServletContextListener implements ServletContextListener {

    public static final MetricRegistry metricRegistry = new MetricRegistry();
    public static final HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();

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
