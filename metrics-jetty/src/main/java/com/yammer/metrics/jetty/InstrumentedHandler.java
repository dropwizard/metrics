package com.yammer.metrics.jetty;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.CounterMetric;
import com.yammer.metrics.core.GaugeMetric;
import com.yammer.metrics.core.MeterMetric;
import com.yammer.metrics.core.TimerMetric;
import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationListener;
import org.eclipse.jetty.server.AsyncContinuation;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.HandlerWrapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * A Jetty {@link Handler} which records various metrics about an underlying
 * {@link Handler} instance.
 *
 * @author coda
 */
public class InstrumentedHandler extends HandlerWrapper {
    private final TimerMetric dispatches;
    private final MeterMetric requests;
    private final MeterMetric resumes;
    private final MeterMetric suspends;
    private final MeterMetric expires;

    private final CounterMetric activeRequests;
    private final CounterMetric activeSuspendedRequests;
    private final CounterMetric activeDispatches;

    private final MeterMetric[] responses;

    private final ContinuationListener listener;

    /**
     * Create a new instrumented handler.
     *
     * @param underlying the handler about which metrics will be collected
     */
    public InstrumentedHandler(Handler underlying) {
        super();
        this.dispatches = Metrics.newTimer(underlying.getClass(), "dispatches", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
        this.requests = Metrics.newMeter(underlying.getClass(), "requests", "requests", TimeUnit.SECONDS);
        this.resumes = Metrics.newMeter(underlying.getClass(), "resumes", "requests", TimeUnit.SECONDS);
        this.suspends = Metrics.newMeter(underlying.getClass(), "suspends", "requests", TimeUnit.SECONDS);
        this.expires = Metrics.newMeter(underlying.getClass(), "expires", "requests", TimeUnit.SECONDS);

        this.activeRequests = Metrics.newCounter(underlying.getClass(), "active-requests");
        this.activeSuspendedRequests = Metrics.newCounter(underlying.getClass(), "active-suspended-requests");
        this.activeDispatches = Metrics.newCounter(underlying.getClass(), "active-dispatches");

        this.responses = new MeterMetric[]{
                Metrics.newMeter(underlying.getClass(), "1xx-responses", "responses", TimeUnit.SECONDS), // 1xx
                Metrics.newMeter(underlying.getClass(), "2xx-responses", "responses", TimeUnit.SECONDS), // 2xx
                Metrics.newMeter(underlying.getClass(), "3xx-responses", "responses", TimeUnit.SECONDS), // 3xx
                Metrics.newMeter(underlying.getClass(), "4xx-responses", "responses", TimeUnit.SECONDS), // 4xx
                Metrics.newMeter(underlying.getClass(), "5xx-responses", "responses", TimeUnit.SECONDS)  // 5xx
        };

        Metrics.newGauge(underlying.getClass(), "percent-4xx-1m", new GaugeMetric<Double>() {
            @Override
            public Double value() {
                if (requests.count() > 0) {
                    return responses[3].oneMinuteRate() / requests.oneMinuteRate();
                }
                return 0.0;
            }
        });

        Metrics.newGauge(underlying.getClass(), "percent-4xx-5m", new GaugeMetric<Double>() {
            @Override
            public Double value() {
                if (requests.count() > 0) {
                    return responses[3].fiveMinuteRate() / requests.fiveMinuteRate();
                }
                return 0.0;
            }
        });

        Metrics.newGauge(underlying.getClass(), "percent-4xx-15m", new GaugeMetric<Double>() {
            @Override
            public Double value() {
                if (requests.count() > 0) {
                    return responses[3].fifteenMinuteRate() / requests.fifteenMinuteRate();
                }
                return 0.0;
            }
        });

        Metrics.newGauge(underlying.getClass(), "percent-5xx-1m", new GaugeMetric<Double>() {
            @Override
            public Double value() {
                if (requests.count() > 0) {
                    return responses[4].oneMinuteRate() / requests.oneMinuteRate();
                }
                return 0.0;
            }
        });

        Metrics.newGauge(underlying.getClass(), "percent-5xx-5m", new GaugeMetric<Double>() {
            @Override
            public Double value() {
                if (requests.count() > 0) {
                    return responses[4].fiveMinuteRate() / requests.fiveMinuteRate();
                }
                return 0.0;
            }
        });

        Metrics.newGauge(underlying.getClass(), "percent-5xx-15m", new GaugeMetric<Double>() {
            @Override
            public Double value() {
                if (requests.count() > 0) {
                    return responses[4].fifteenMinuteRate() / requests.fifteenMinuteRate();
                }
                return 0.0;
            }
        });

        this.listener = new ContinuationListener() {
            @Override
            public void onComplete(Continuation continuation) {
                expires.mark();
            }

            @Override
            public void onTimeout(Continuation continuation) {
                final Request request = ((AsyncContinuation) continuation).getBaseRequest();
                updateResponses(request);
                if (!continuation.isResumed()) {
                    activeSuspendedRequests.dec();
                }
            }
        };

        setHandler(underlying);
    }

    @Override
    public void handle(String target, Request request,
                       HttpServletRequest httpRequest, HttpServletResponse httpResponse)
            throws IOException, ServletException {
        activeDispatches.inc();

        final AsyncContinuation continuation = request.getAsyncContinuation();

        long start;
        boolean isMilliseconds;

        if (continuation.isInitial()) {
            activeRequests.inc();
            start = request.getTimeStamp();
            isMilliseconds = true;
        } else {
            activeSuspendedRequests.dec();
            if (continuation.isResumed()) {
                resumes.mark();
            }
            isMilliseconds = false;
            start = System.nanoTime();
        }

        try {
            super.handle(target, request, httpRequest, httpResponse);
        } finally {
            if (isMilliseconds) {
                dispatches.update(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
            } else {
                dispatches.update(System.nanoTime() - start, TimeUnit.NANOSECONDS);
            }

            activeDispatches.dec();
            if (continuation.isSuspended()) {
                if (continuation.isInitial()) {
                    continuation.addContinuationListener(listener);
                }
                suspends.mark();
                activeSuspendedRequests.inc();
            } else if (continuation.isInitial()) {
                updateResponses(request);
            }
        }
    }

    private void updateResponses(Request request) {
        final int response = request.getResponse().getStatus() / 100;
        if (response >= 1 && response <= 5) {
            responses[response - 1].mark();
        }
        activeRequests.dec();
        requests.mark();
    }
}
