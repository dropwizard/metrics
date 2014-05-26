package com.codahale.metrics.servlets;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.concurrent.ExecutorService;

/**
 * A listener implementation which injects a {@link MetricRegistry} instance, a
 * {@link HealthCheckRegistry} instance, and an optional {@link ExecutorService} instance  into the
 * servlet context as named attributes.
 *
 * @deprecated Use {@link MetricsServlet.ContextListener} and
 *             {@link HealthCheckServlet.ContextListener} instead.
 */
@Deprecated
public abstract class AdminServletContextListener implements ServletContextListener {
    /**
     * @return the {@link MetricRegistry} to inject into the servlet context.
     */
    protected abstract MetricRegistry getMetricRegistry();

    /**
     * @return the {@link HealthCheckRegistry} to inject into the servlet context.
     */
    protected abstract HealthCheckRegistry getHealthCheckRegistry();

    /**
     * @return the {@link ExecutorService} to inject into the servlet context, or {@code null} if
     * the health checks should be run in the servlet worker thread.
     */
    protected ExecutorService getExecutorService() {
        // don't use a thread pool by default
        return null;
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        final ServletContext context = servletContextEvent.getServletContext();
        context.setAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY, getHealthCheckRegistry());
        context.setAttribute(HealthCheckServlet.HEALTH_CHECK_EXECUTOR, getExecutorService());
        context.setAttribute(MetricsServlet.METRICS_REGISTRY, getMetricRegistry());
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        // no-op...
    }
}
