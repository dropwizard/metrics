package com.codahale.metrics.servlets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.json.MetricsModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class MetricsServlet extends HttpServlet {
    private static final String UTF_8 = "UTF-8";
    public static final String RATE_UNIT = MetricsServlet.class.getCanonicalName() + ".rateUnit";
    public static final String DURATION_UNIT = MetricsServlet.class.getCanonicalName() + ".durationUnit";
    public static final String SHOW_SAMPLES = MetricsServlet.class.getCanonicalName() + ".showSamples";
    public static final String METRICS_REGISTRY = MetricsServlet.class.getCanonicalName() + ".registry";

    private static final long serialVersionUID = 1049773947734939602L;
    private static final String CONTENT_TYPE = "application/json";

    private transient MetricRegistry registry;
    private transient ObjectMapper mapper;

    @Override
    public void init(ServletConfig config) throws ServletException {
        final Object registryAttr = config.getServletContext().getAttribute(METRICS_REGISTRY);
        if (registryAttr instanceof MetricRegistry) {
            this.registry = (MetricRegistry) registryAttr;
        } else {
            throw new ServletException("Couldn't find a MetricRegistry instance.");
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
        final OutputStream output = resp.getOutputStream();
        final ByteArrayOutputStream wrappedOutput = new ByteArrayOutputStream();

        resp.setContentType(CONTENT_TYPE);
        resp.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");

        try {
            final boolean prettyPrint = Boolean.parseBoolean(req.getParameter("pretty"));
            final String jsonpCallback = req.getParameter("callback");
            
            if (jsonpCallback != null) {
                validateJsonpCallback(jsonpCallback);                
                output.write(jsonpCallback.getBytes(UTF_8));
                output.write("(".getBytes(UTF_8));
            }

            // We have to use a wrapped stream because the act of writing closes the provided stream.
            getWriter(prettyPrint).writeValue(wrappedOutput, registry);
            output.write(wrappedOutput.toByteArray());
            
            if (jsonpCallback != null) {
                output.write(");".getBytes(UTF_8));
            }

            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (RuntimeException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            output.close();
            wrappedOutput.close();
        }
    }

    private void validateJsonpCallback(final String jsonpCallback) {
        // Ensure that the callback name doesn't contain injected code
        if (jsonpCallback.matches(".*\\W.*")) {
            throw new IllegalArgumentException("'callback' parameter contains illegal (non-word) characters");
        }
        
        // Ensure that the callback name is a resonable method name
        int length = jsonpCallback.length();
        if (length < 1 || length > 64) {
            throw new IllegalArgumentException("'callback' parameter length must be between 1 and 64 characters");
        }
    }

    private ObjectWriter getWriter(boolean prettyPrint) {
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
