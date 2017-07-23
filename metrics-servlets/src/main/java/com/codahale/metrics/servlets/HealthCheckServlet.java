package com.codahale.metrics.servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ExecutorService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckFilter;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.json.HealthCheckModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class HealthCheckServlet extends HttpServlet {
    public static abstract class ContextListener implements ServletContextListener {
        /**
         * @return the {@link HealthCheckRegistry} to inject into the servlet context.
         */
        protected abstract HealthCheckRegistry getHealthCheckRegistry();

        /**
         * @return the {@link ExecutorService} to inject into the servlet context, or {@code null}
         * if the health checks should be run in the servlet worker thread.
         */
        protected ExecutorService getExecutorService() {
            // don't use a thread pool by default
            return null;
        }

        /**
         * @return the {@link HealthCheckFilter} that shall be used to filter health checks,
         * or {@link HealthCheckFilter#ALL} if the default should be used.
         */
        protected HealthCheckFilter getHealthCheckFilter() {
            return HealthCheckFilter.ALL;
        }

        @Override
        public void contextInitialized(ServletContextEvent event) {
            final ServletContext context = event.getServletContext();
            context.setAttribute(HEALTH_CHECK_REGISTRY, getHealthCheckRegistry());
            context.setAttribute(HEALTH_CHECK_EXECUTOR, getExecutorService());
        }

        @Override
        public void contextDestroyed(ServletContextEvent event) {
            // no-op
        }
    }

    public static final String HEALTH_CHECK_REGISTRY = HealthCheckServlet.class.getCanonicalName() + ".registry";
    public static final String HEALTH_CHECK_EXECUTOR = HealthCheckServlet.class.getCanonicalName() + ".executor";
    public static final String HEALTH_CHECK_FILTER = HealthCheckServlet.class.getCanonicalName() + ".healthCheckFilter";

    private static final long serialVersionUID = -8432996484889177321L;
    private static final String CONTENT_TYPE = "application/json";

    private transient HealthCheckRegistry registry;
    private transient ExecutorService executorService;
    private transient HealthCheckFilter filter;
    private transient ObjectMapper mapper;

    public HealthCheckServlet() {
    }

    public HealthCheckServlet(HealthCheckRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        final ServletContext context = config.getServletContext();
        if (null == registry) {
            final Object registryAttr = context.getAttribute(HEALTH_CHECK_REGISTRY);
            if (registryAttr instanceof HealthCheckRegistry) {
                this.registry = (HealthCheckRegistry) registryAttr;
            } else {
                throw new ServletException("Couldn't find a HealthCheckRegistry instance.");
            }
        }

        final Object executorAttr = context.getAttribute(HEALTH_CHECK_EXECUTOR);
        if (executorAttr instanceof ExecutorService) {
            this.executorService = (ExecutorService) executorAttr;
        }


        final Object filterAttr = context.getAttribute(HEALTH_CHECK_FILTER);
        if (filterAttr instanceof HealthCheckFilter) {
            filter = (HealthCheckFilter) filterAttr;
        }
        if (filter == null) {
            filter = HealthCheckFilter.ALL;
        }

        this.mapper = new ObjectMapper().registerModule(new HealthCheckModule());
    }

    @Override
    public void destroy() {
        super.destroy();
        registry.shutdown();
    }

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException {
        final SortedMap<String, HealthCheck.Result> results = runHealthChecks();
        resp.setContentType(CONTENT_TYPE);
        resp.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
        if (results.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
        } else {
            if (isAllHealthy(results)) {
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }

        try (OutputStream output = resp.getOutputStream()) {
            getWriter(req).writeValue(output, results);
        }
    }

    private ObjectWriter getWriter(HttpServletRequest request) {
        final boolean prettyPrint = Boolean.parseBoolean(request.getParameter("pretty"));
        if (prettyPrint) {
            return mapper.writerWithDefaultPrettyPrinter();
        }
        return mapper.writer();
    }

    private SortedMap<String, HealthCheck.Result> runHealthChecks() {
        if (executorService == null) {
            return registry.runHealthChecks(filter);
        }
        return registry.runHealthChecks(executorService, filter);
    }

    private static boolean isAllHealthy(Map<String, HealthCheck.Result> results) {
        for (HealthCheck.Result result : results.values()) {
            if (!result.isHealthy()) {
                return false;
            }
        }
        return true;
    }
}
