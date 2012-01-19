package com.yammer.metrics.reporting;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MetricsRegistry;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * A servlet context listener which shuts down the various thread pools when the context is
 * destroyed.
 */
public class MetricsServletContextListener implements ServletContextListener {
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        final ServletContext context = sce.getServletContext();
        final Object metricsRegistry = context.getAttribute(MetricsServlet.REGISTRY_ATTRIBUTE);
        if (metricsRegistry instanceof MetricsRegistry) {
            ((MetricsRegistry) metricsRegistry).shutdown();
        } else {
            Metrics.shutdown();
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // nothing needs to happen
    }
}
