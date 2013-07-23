package com.codahale.metrics.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.json.MetricsModule;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * A servlet which returns the metrics in a given registry as an {@code application/json} response.
 */
public class MetricsServlet extends HttpServlet {
    /**
     * An abstract {@link ServletContextListener} which allows you to programmatically inject the
     * {@link MetricRegistry}, rate and duration units, and allowed origin for
     * {@link MetricsServlet}.
     */
    public static abstract class ContextListener implements ServletContextListener {
        /**
         * Returns the {@link MetricRegistry} to inject into the servlet context.
         */
        protected abstract MetricRegistry getMetricRegistry();

        /**
         * Returns the {@link TimeUnit} to which rates should be converted, or {@code null} if the
         * default should be used.
         */
        protected TimeUnit getRateUnit() {
            // use the default
            return null;
        }

        /**
         * Returns the {@link TimeUnit} to which durations should be converted, or {@code null} if
         * the default should be used.
         */
        protected TimeUnit getDurationUnit() {
            // use the default
            return null;
        }

        /**
         * Returns the {@code Access-Control-Allow-Origin} header value, if any.
         */
        protected String getAllowedOrigin() {
            // use the default
            return null;
        }

        @Override
        public void contextInitialized(ServletContextEvent event) {
            final ServletContext context = event.getServletContext();
            context.setAttribute(METRICS_REGISTRY, getMetricRegistry());
            context.setAttribute(RATE_UNIT, getRateUnit());
            context.setAttribute(DURATION_UNIT, getDurationUnit());
            context.setAttribute(ALLOWED_ORIGIN, getAllowedOrigin());
        }

        @Override
        public void contextDestroyed(ServletContextEvent event) {
            // no-op
        }
    }

    public static final String RATE_UNIT = MetricsServlet.class.getCanonicalName() + ".rateUnit";
    public static final String DURATION_UNIT = MetricsServlet.class.getCanonicalName() + ".durationUnit";
    public static final String SHOW_SAMPLES = MetricsServlet.class.getCanonicalName() + ".showSamples";
    public static final String METRICS_REGISTRY = MetricsServlet.class.getCanonicalName() + ".registry";
    public static final String ALLOWED_ORIGIN = MetricsServlet.class.getCanonicalName() + ".allowedOrigin";

    private static final long serialVersionUID = 1049773947734939602L;
    private static final String CONTENT_TYPE = "application/json";

    private String allowedOrigin;
    private transient MetricRegistry registry;
    private transient ObjectMapper mapper;

    public MetricsServlet() {
    }

    public MetricsServlet(MetricRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        final ServletContext context = config.getServletContext();
        if (null == registry) {
            final Object registryAttr = context.getAttribute(METRICS_REGISTRY);
            if (registryAttr instanceof MetricRegistry) {
                this.registry = (MetricRegistry) registryAttr;
            } else {
                throw new ServletException("Couldn't find a MetricRegistry instance.");
            }
        }

        final TimeUnit rateUnit = parseTimeUnit(context.getInitParameter(RATE_UNIT),
                                                TimeUnit.SECONDS);
        final TimeUnit durationUnit = parseTimeUnit(context.getInitParameter(DURATION_UNIT),
                                                    TimeUnit.SECONDS);
        final boolean showSamples = Boolean.parseBoolean(context.getInitParameter(SHOW_SAMPLES));
        this.mapper = new ObjectMapper().registerModule(new MetricsModule(rateUnit,
                                                                          durationUnit,
                                                                          showSamples));

        this.allowedOrigin = config.getInitParameter(ALLOWED_ORIGIN);
    }

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType(CONTENT_TYPE);
        if (allowedOrigin != null) {
            resp.setHeader("Access-Control-Allow-Origin", allowedOrigin);
        }
        resp.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
        resp.setStatus(HttpServletResponse.SC_OK);

        final OutputStream output = resp.getOutputStream();
        try {
            getWriter(req).writeValue(output, registry);
        } finally {
            output.close();
        }
    }

    private ObjectWriter getWriter(HttpServletRequest request) {
        final boolean prettyPrint = Boolean.parseBoolean(request.getParameter("pretty"));
        if (prettyPrint) {
            return mapper.writerWithDefaultPrettyPrinter();
        }
        return mapper.writer();
    }

    private TimeUnit parseTimeUnit(String value, TimeUnit defaultValue) {
        try {
            return TimeUnit.valueOf(String.valueOf(value).toUpperCase(Locale.US));
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }
}
