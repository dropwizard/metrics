package com.yammer.metrics.web;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.*;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * {@link Filter} implementation which captures request information and a breakdown of the response
 * codes being returned.
 */
public abstract class WebappMetricsFilter implements Filter {
    private final String otherMetricName;
    private final Map<Integer, String> meterNamesByStatusCode;
    private final String registryAttribute;

    // initialized after call of init method
    private ConcurrentMap<Integer, Meter> metersByStatusCode;
    private Meter otherMeter;
    private Counter activeRequests;
    private Timer requestTimer;

    /**
     * Creates a new instance of the filter.
     *
     * @param registryAttribute the attribute used to look up the metrics registry in the servlet context
     * @param meterNamesByStatusCode A map, keyed by status code, of meter names that we are
     *                               interested in.
     * @param otherMetricName        The name used for the catch-all meter.
     */
    public WebappMetricsFilter(String registryAttribute, Map<Integer, String> meterNamesByStatusCode,
                               String otherMetricName) {
        this.registryAttribute = registryAttribute;
        this.otherMetricName = otherMetricName;
        this.meterNamesByStatusCode = meterNamesByStatusCode;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        final MetricsRegistry metricsRegistry = getMetricsFactory(filterConfig);
        final String groupName =
                getInitParameter("name.group", getClass().getPackage().getName(), filterConfig);
        final String typeName =
                getInitParameter("name.type", getClass().getSimpleName(), filterConfig);

        this.metersByStatusCode = new ConcurrentHashMap<Integer, Meter>(meterNamesByStatusCode
                .size());
        for (Entry<Integer, String> entry : meterNamesByStatusCode.entrySet()) {
            metersByStatusCode.put(entry.getKey(),
                    metricsRegistry.newMeter(
                            new MetricName(groupName, typeName, entry.getValue()),
                            "responses",
                            TimeUnit.SECONDS));
        }
        this.otherMeter = metricsRegistry.newMeter(
                new MetricName(groupName, typeName, otherMetricName),
                "responses",
                TimeUnit.SECONDS);
        this.activeRequests = metricsRegistry.newCounter(
                new MetricName(groupName, typeName, "activeRequests"));
        this.requestTimer = metricsRegistry.newTimer(
                new MetricName(groupName, typeName, "requests"),
                TimeUnit.MILLISECONDS,
                TimeUnit.SECONDS);

    }

    private MetricsRegistry getMetricsFactory(FilterConfig filterConfig) {
        final MetricsRegistry metricsRegistry;

        final Object o = filterConfig.getServletContext().getAttribute(this.registryAttribute);
        if (o instanceof MetricsRegistry) {
            metricsRegistry = (MetricsRegistry) o;
        } else {
            metricsRegistry = Metrics.defaultRegistry();
        }
        return metricsRegistry;
    }

    private String getInitParameter(String key, String defaultValue, FilterConfig filterConfig) {

        final String value = filterConfig.getInitParameter(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        } else {
            return value;
        }
    }

    @Override
    public void destroy() {
        
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        final StatusExposingServletResponse wrappedResponse =
                new StatusExposingServletResponse((HttpServletResponse) response);
        activeRequests.inc();
        final TimerContext context = requestTimer.time();
        try {
            chain.doFilter(request, wrappedResponse);
        } finally {
            context.stop();
            activeRequests.dec();
            markMeterForStatusCode(wrappedResponse.getStatus());
        }
    }

    private void markMeterForStatusCode(int status) {
        final Meter metric = metersByStatusCode.get(status);
        if (metric != null) {
            metric.mark();
        } else {
            otherMeter.mark();
        }
    }

    private static class StatusExposingServletResponse extends HttpServletResponseWrapper {
        // The Servlet spec says: calling setStatus is optional, if no status is set, the default is 200.
        private int httpStatus = 200;

        public StatusExposingServletResponse(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void sendError(int sc) throws IOException {
            httpStatus = sc;
            super.sendError(sc);
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            httpStatus = sc;
            super.sendError(sc, msg);
        }

        @Override
        public void setStatus(int sc) {
            httpStatus = sc;
            super.setStatus(sc);
        }

        public int getStatus() {
            return httpStatus;
        }
    }
}
