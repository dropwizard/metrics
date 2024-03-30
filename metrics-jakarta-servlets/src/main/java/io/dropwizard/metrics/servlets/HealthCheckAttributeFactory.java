package io.dropwizard.metrics.servlets;

import com.codahale.metrics.health.HealthCheckFilter;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.ExecutorService;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

public class HealthCheckAttributeFactory {

    public static final String HEALTH_CHECK_REGISTRY = HealthCheckServlet.class.getCanonicalName() + ".registry";
    public static final String HEALTH_CHECK_EXECUTOR = HealthCheckServlet.class.getCanonicalName() + ".executor";
    public static final String HEALTH_CHECK_FILTER = HealthCheckServlet.class.getCanonicalName() + ".healthCheckFilter";
    public static final String HEALTH_CHECK_MAPPER = HealthCheckServlet.class.getCanonicalName() + ".mapper";
    public static final String HEALTH_CHECK_HTTP_STATUS_INDICATOR = HealthCheckServlet.class.getCanonicalName() + ".httpStatusIndicator";

    private final ServletContext context;

    public HealthCheckAttributeFactory(ServletContext context) {
        this.context = context;
    }

    public HealthCheckRegistry createHealthCheckRegistry() throws ServletException {
        final Object registryAttr = context.getAttribute(HEALTH_CHECK_REGISTRY);
        if (registryAttr instanceof HealthCheckRegistry) {
            return (HealthCheckRegistry) registryAttr;
        } else {
            throw new ServletException("Couldn't find a HealthCheckRegistry instance.");
        }
    }

    public ExecutorService createExecutorService() {
        final Object executorAttr = context.getAttribute(HEALTH_CHECK_EXECUTOR);
        return executorAttr instanceof ExecutorService ? (ExecutorService) executorAttr : null;
    }

    public HealthCheckFilter createHealthCheckFilter() {
        final Object filterAttr = context.getAttribute(HEALTH_CHECK_FILTER);
        return filterAttr instanceof HealthCheckFilter ? (HealthCheckFilter) filterAttr : HealthCheckFilter.ALL;
    }

    public ObjectMapper createObjectMapper() {
        final Object mapperAttr = context.getAttribute(HEALTH_CHECK_MAPPER);
        return mapperAttr instanceof ObjectMapper ? (ObjectMapper) mapperAttr : new ObjectMapper();
    }

    public boolean createHttpStatusIndicator() {
        final Object httpStatusIndicatorAttr = context.getAttribute(HEALTH_CHECK_HTTP_STATUS_INDICATOR);
        return httpStatusIndicatorAttr instanceof Boolean ? (Boolean) httpStatusIndicatorAttr : true;
    }
}
