package com.codahale.metrics.servlets;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.concurrent.ExecutorService;

/**
 * A listener implementation which injects a {@link MetricRegistry} instance, a
 * {@link HealthCheckRegistry} instance, and an optional {@link ExecutorService} instance  into the
 * servlet context as named attributes.
 */
public abstract class AdminServletContextListener implements ServletContextListener {
    /**
     * Returns the {@link MetricRegistry} to inject into the servlet context.
     */
    protected abstract MetricRegistry getMetricRegistry();

    /**
     * Returns the {@link HealthCheckRegistry} to inject into the servlet context.
     */
    protected abstract HealthCheckRegistry getHealthCheckRegistry();

    /**
     * Returns the {@link ExecutorService} to inject into the servlet context, or {@code null} if
     * the health checks should be run in the servlet worker thread.
     */
    protected ExecutorService getExecutorService() {
        // don't use a thread pool by default
        return null;
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        servletContextEvent.getServletContext().setAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY,
                                                             getHealthCheckRegistry());
        servletContextEvent.getServletContext().setAttribute(HealthCheckServlet.HEALTH_CHECK_EXECUTOR,
                                                             getExecutorService());
        servletContextEvent.getServletContext().setAttribute(MetricsServlet.METRICS_REGISTRY,
                                                             getMetricRegistry());
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        // no-op...
    }
}
