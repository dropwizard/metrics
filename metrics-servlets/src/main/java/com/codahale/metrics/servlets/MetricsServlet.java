package com.codahale.metrics.servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.json.MetricsModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.util.JSONPObject;

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
         * @return the {@link MetricRegistry} to inject into the servlet context.
         */
        protected abstract MetricRegistry getMetricRegistry();

        /**
         * @return the {@link TimeUnit} to which rates should be converted, or {@code null} if the
         * default should be used.
         */
        protected TimeUnit getRateUnit() {
            // use the default
            return null;
        }

        /**
         * @return the {@link TimeUnit} to which durations should be converted, or {@code null} if
         * the default should be used.
         */
        protected TimeUnit getDurationUnit() {
            // use the default
            return null;
        }

        /**
         * @return the {@code Access-Control-Allow-Origin} header value, if any.
         */
        protected String getAllowedOrigin() {
            // use the default
            return null;
        }

        /**
         * Returns the name of the parameter used to specify the jsonp callback, if any.
         */
        protected String getJsonpCallbackParameter() {
            return null;
        }

        /**
         * Returns the {@link MetricFilter} that shall be used to filter metrics, or {@link MetricFilter#ALL} if
         * the default should be used.
         */
        protected MetricFilter getMetricFilter() {
            // use the default
            return MetricFilter.ALL;
        }

        @Override
        public void contextInitialized(ServletContextEvent event) {
            final ServletContext context = event.getServletContext();
            context.setAttribute(METRICS_REGISTRY, getMetricRegistry());
            context.setAttribute(METRIC_FILTER, getMetricFilter());
            if (getDurationUnit() != null) {
                context.setInitParameter(MetricsServlet.DURATION_UNIT, getDurationUnit().toString());
            }
            if (getRateUnit() != null) {
                context.setInitParameter(MetricsServlet.RATE_UNIT, getRateUnit().toString());
            }
            if (getAllowedOrigin() != null) {
                context.setInitParameter(MetricsServlet.ALLOWED_ORIGIN, getAllowedOrigin());
            }
            if (getJsonpCallbackParameter() != null) {
                context.setAttribute(CALLBACK_PARAM, getJsonpCallbackParameter());
            }
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
    public static final String METRIC_FILTER = MetricsServlet.class.getCanonicalName() + ".metricFilter";
    public static final String CALLBACK_PARAM = MetricsServlet.class.getCanonicalName() + ".jsonpCallback";

    private static final long serialVersionUID = 1049773947734939602L;
    private static final String CONTENT_TYPE = "application/json";

    private String allowedOrigin;
    private String jsonpParamName;
    private transient MetricRegistry registry;
    private transient ObjectMapper mapper;

    public MetricsServlet() {
    }

    public MetricsServlet(MetricRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

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
        MetricFilter filter = (MetricFilter) context.getAttribute(METRIC_FILTER);
        if (filter == null) {
          filter = MetricFilter.ALL;
        }
        this.mapper = new ObjectMapper().registerModule(new MetricsModule(rateUnit,
                                                                          durationUnit,
                                                                          showSamples,
                                                                          filter));

        this.allowedOrigin = context.getInitParameter(ALLOWED_ORIGIN);
        this.jsonpParamName = context.getInitParameter(CALLBACK_PARAM);
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

        try (OutputStream output = resp.getOutputStream()) {
            if (jsonpParamName != null && req.getParameter(jsonpParamName) != null) {
                getWriter(req).writeValue(output, new JSONPObject(req.getParameter(jsonpParamName), registry));
            } else {
                getWriter(req).writeValue(output, registry);
            }
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
