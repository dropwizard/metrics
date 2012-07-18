package com.yammer.metrics.servlet;

import com.yammer.metrics.HealthChecks;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.HealthCheck;
import com.yammer.metrics.core.HealthCheckRegistry;
import com.yammer.metrics.core.MetricsRegistry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * An HTTP servlet which runs the health checks registered with a given {@link HealthCheckRegistry}
 * and prints the results as a {@code text/plain} entity. Only responds to {@code GET} requests.
 * <p/>
 * If the servlet context has an attribute named
 * {@code com.yammer.metrics.servlet.HealthCheckServlet.registry} which is a
 * {@link HealthCheckRegistry} instance, {@link HealthCheckServlet} will use it instead of
 * {@link HealthChecks}.
 */
public class HealthCheckServlet extends HttpServlet {
    /**
     * The attribute name of the {@link HealthCheckRegistry} instance in the servlet context.
     */
    public static final String REGISTRY_ATTRIBUTE = HealthCheckServlet.class.getName() + ".registry";
    private static final String CONTENT_TYPE = "text/plain";

    private HealthCheckRegistry registry;

    /**
     * Creates a new {@link HealthCheckServlet}.
     */
    public HealthCheckServlet() {
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.registry = getHealthCheckRegistryFactory(config);
    }

    protected HealthCheckRegistry getHealthCheckRegistryFactory(ServletConfig config) {
        final Object o = config.getServletContext().getAttribute(REGISTRY_ATTRIBUTE);
        if (o instanceof HealthCheckRegistry) {
            return (HealthCheckRegistry) o;
        } else {
            return HealthChecks.defaultRegistry();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException {
        final Map<String, HealthCheck.Result> results = registry.runHealthChecks();
        resp.setContentType(CONTENT_TYPE);
        resp.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
        final PrintWriter writer = resp.getWriter();
        if (results.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
            writer.println("! No health checks registered.");
        } else {
            if (isAllHealthy(results)) {
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            for (Map.Entry<String, HealthCheck.Result> entry : results.entrySet()) {
                final HealthCheck.Result result = entry.getValue();
                if (result.isHealthy()) {
                    if (result.getMessage() != null) {
                        writer.format("* %s: OK\n  %s\n", entry.getKey(), result.getMessage());
                    } else {
                        writer.format("* %s: OK\n", entry.getKey());
                    }
                } else {
                    if (result.getMessage() != null) {
                        writer.format("! %s: ERROR\n!  %s\n", entry.getKey(), result.getMessage());
                    }

                    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
                    final Throwable error = result.getError();
                    if (error != null) {
                        writer.println();
                        error.printStackTrace(writer);
                        writer.println();
                    }
                }
            }
        }
        writer.close();
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
