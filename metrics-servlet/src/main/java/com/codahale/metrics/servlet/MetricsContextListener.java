package com.codahale.metrics.servlet;

import com.codahale.metrics.MetricRegistry;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class MetricsContextListener implements ServletContextListener {
    private final MetricRegistry registry;

    public MetricsContextListener(MetricRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        sce.getServletContext().setAttribute(DefaultWebappMetricsFilter.REGISTRY_ATTRIBUTE, registry);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
