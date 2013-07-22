package com.codahale.metrics.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.json.MetricsModule;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MetricsServlet extends HttpServlet {
    public static final String RATE_UNIT = MetricsServlet.class.getCanonicalName() + ".rateUnit";
    public static final String DURATION_UNIT = MetricsServlet.class.getCanonicalName() + ".durationUnit";
    public static final String SHOW_SAMPLES = MetricsServlet.class.getCanonicalName() + ".showSamples";
    public static final String METRICS_REGISTRY = MetricsServlet.class.getCanonicalName() + ".registry";

    private static final long serialVersionUID = 1049773947734939602L;
    private static final String CONTENT_TYPE = "application/json";

    private transient MetricRegistry registry;
    private transient ObjectMapper mapper;

    public MetricsServlet() {
    }

    public MetricsServlet(MetricRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        if (null == registry) {
            final Object registryAttr = config.getServletContext().getAttribute(METRICS_REGISTRY);
            if (registryAttr instanceof MetricRegistry) {
                this.registry = (MetricRegistry) registryAttr;
            } else {
                throw new ServletException("Couldn't find a MetricRegistry instance.");
            }
        }

        final TimeUnit rateUnit = parseTimeUnit(config.getServletContext()
                                                      .getInitParameter(RATE_UNIT),
                                                TimeUnit.SECONDS);

        final TimeUnit durationUnit = parseTimeUnit(config.getServletContext()
                                                          .getInitParameter(DURATION_UNIT),
                                                    TimeUnit.SECONDS);

        final boolean showSamples = Boolean.parseBoolean(config.getServletContext()
                                                               .getInitParameter(SHOW_SAMPLES));

        this.mapper = new ObjectMapper().registerModule(new MetricsModule(rateUnit,
                                                                          durationUnit,
                                                                          showSamples));
    }

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType(CONTENT_TYPE);
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
