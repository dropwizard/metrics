package com.yammer.metrics.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.CounterMetric;
import com.yammer.metrics.core.MeterMetric;
import com.yammer.metrics.core.TimerMetric;

/**
 * {@link Filter} implementation which captures request information and a breakdown of the response codes being returned.
 */
public abstract class WebappMetricsFilter implements Filter {
	
    private final Map<Integer, MeterMetric> metersByStatusCode = new HashMap<Integer, MeterMetric>();
    private final MeterMetric otherMeterMetric;
    private final CounterMetric activeRequests;
    private final TimerMetric requestTimer;

    public WebappMetricsFilter(Map<Integer, String> meterNamesByStatusCode, String otherMetricName) {
        
        for (Entry<Integer, String> entry : meterNamesByStatusCode.entrySet()) {
            metersByStatusCode.put(entry.getKey(), Metrics.newMeter(WebappMetricsFilter.class, entry.getValue(), "responses", TimeUnit.SECONDS));
        }
        
        otherMeterMetric = Metrics.newMeter(WebappMetricsFilter.class, otherMetricName, "responses", TimeUnit.SECONDS);
        activeRequests = Metrics.newCounter(WebappMetricsFilter.class, "activeRequests");
        requestTimer = Metrics.newTimer(WebappMetricsFilter.class, "requests", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
        
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        
        long start = System.currentTimeMillis();

        StatusExposingServletResponse wrappedResponse = new StatusExposingServletResponse((HttpServletResponse) response);
        
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

        MeterMetric metric = metersByStatusCode.get(status);

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
