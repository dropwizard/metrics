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
    @SuppressWarnings("deprecation")
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        final ServletContext context = sce.getServletContext();
        final MetricsRegistry metricsRegistry = (MetricsRegistry) context.getAttribute(MetricsServlet.ATTR_NAME_METRICS_REGISTRY);
        if (metricsRegistry != null) {
            metricsRegistry.shutdown();
        } else {
            Metrics.shutdown();
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // nothing needs to happen
    }
}
