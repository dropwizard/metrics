package com.yammer.metrics.web;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.CounterMetric;
import com.yammer.metrics.core.MeterMetric;
import com.yammer.metrics.core.TimerMetric;

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
    private final ConcurrentMap<Integer, MeterMetric> metersByStatusCode;
    private final MeterMetric otherMeterMetric;
    private final CounterMetric activeRequests;
    private final TimerMetric requestTimer;

    /**
     * Creates a new instance of the filter.
     *
     * @param meterNamesByStatusCode A map, keyed by status code, of meter names that we are
     *                               interested in.
     * @param otherMetricName        The name used for the catch-all meter.
     */
    public WebappMetricsFilter(Map<Integer, String> meterNamesByStatusCode,
                               String otherMetricName) {
        this.metersByStatusCode = new ConcurrentHashMap<Integer, MeterMetric>(meterNamesByStatusCode.size());
        for (Entry<Integer, String> entry : meterNamesByStatusCode.entrySet()) {
            metersByStatusCode.put(entry.getKey(),
                                   Metrics.newMeter(WebappMetricsFilter.class,
                                                    entry.getValue(),
                                                    "responses",
                                                    TimeUnit.SECONDS));
        }
        this.otherMeterMetric = Metrics.newMeter(WebappMetricsFilter.class,
                                                 otherMetricName,
                                                 "responses",
                                                 TimeUnit.SECONDS);
        this.activeRequests = Metrics.newCounter(WebappMetricsFilter.class, "activeRequests");
        this.requestTimer = Metrics.newTimer(WebappMetricsFilter.class,
                                             "requests",
                                             TimeUnit.MILLISECONDS,
                                             TimeUnit.SECONDS);

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        final long start = System.currentTimeMillis();
        final StatusExposingServletResponse wrappedResponse =
                new StatusExposingServletResponse((HttpServletResponse) response);
        activeRequests.inc();
        try {
            chain.doFilter(request, wrappedResponse);
        } finally {
            activeRequests.dec();
            markMeterForStatusCode(wrappedResponse.getStatus());
            requestTimer.update(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
        }
    }

    private void markMeterForStatusCode(int status) {
        final MeterMetric metric = metersByStatusCode.get(status);
        if (metric != null) {
            metric.mark();
        } else {
            otherMeterMetric.mark();
        }
    }

    private static class StatusExposingServletResponse extends HttpServletResponseWrapper {
        private int httpStatus;

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
