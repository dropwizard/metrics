package com.yammer.metrics.web;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;

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
    private ConcurrentMap<Integer, Meter> metersByStatusCode;
    private Meter otherMeter;
    private Counter activeRequests;
    private Timer requestTimer;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Map<Integer, String> meterNamesByStatusCode = getMeterNamesByStatusCode();
        this.metersByStatusCode = new ConcurrentHashMap<Integer, Meter>(meterNamesByStatusCode 
                                                                                      .size());
        for (Entry<Integer, String> entry : meterNamesByStatusCode.entrySet()) {
            metersByStatusCode.put(entry.getKey(),
                                   Metrics.newMeter(createMetricName(entry.getValue()),
                                                    "responses",
                                                    TimeUnit.SECONDS));
        }
        this.otherMeter = Metrics.newMeter(createMetricName(getOtherMetricName()),
                                                 "responses",
                                                 TimeUnit.SECONDS);
        this.activeRequests = Metrics.newCounter(createMetricName("activeRequests"));
        this.requestTimer = Metrics.newTimer(createMetricName("requests"),
                                             TimeUnit.MILLISECONDS,
                                             TimeUnit.SECONDS);
    }
    
    /**
     * @returns A map, keyed by status code, of meter names that we are
     *          interested in.
     */
    protected abstract Map<Integer, String> getMeterNamesByStatusCode();
    
    /**
     * 
     * @return the name used for the catch-all meter.
     */
    protected abstract String getOtherMetricName();
    
    /**
     * Creates a complete name based on the supplied simple metric name. This implementation
     * returns a MetricName instance where the group is the package name of this class, the type is
     * the simple name of this class, and the name is the supplied name.
     * 
     * @param name a metric name, for example "requests".
     * @return the new name object
     */
    protected MetricName createMetricName(String name) {
        return new MetricName(WebappMetricsFilter.class, name);
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
            markMeterForStatusCode(wrappedResponse.getStatus());
        } catch (IOException e) {
            markMeterForStatusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw e;
        } catch (ServletException e) {
            markMeterForStatusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw e;
        } catch (RuntimeException e) {
            markMeterForStatusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw e;
        } finally {
            context.stop();
            activeRequests.dec();
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
        private int httpStatus = HttpServletResponse.SC_OK;

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

		@Override
		public void setStatus(int sc, String sm) {
			httpStatus = sc;
			super.setStatus(sc, sm);
		}
		
		@Override
		public void sendRedirect(String location) throws IOException {
			httpStatus = HttpServletResponse.SC_FOUND;
			super.sendRedirect(location);
		}
		
        public int getStatus() {
            return httpStatus;
        }


    }
}
