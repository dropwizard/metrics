package com.yammer.metrics.jetty;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.util.RatioGauge;
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

import static org.eclipse.jetty.http.HttpMethods.*;

/**
 * A Jetty {@link Handler} which records various metrics about an underlying
 * {@link Handler} instance.
 */
public class InstrumentedHandler extends HandlerWrapper {
    private static final String PATCH = "PATCH";

    private final Timer dispatches;
    private final Meter requests;
    private final Meter resumes;
    private final Meter suspends;
    private final Meter expires;

    private final Counter activeRequests;
    private final Counter activeSuspendedRequests;
    private final Counter activeDispatches;

    private final Meter[] responses;

    private final Timer getRequests, postRequests, headRequests,
            putRequests, deleteRequests, optionsRequests, traceRequests,
            connectRequests, patchRequests, otherRequests;

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

        this.responses = new Meter[]{
                Metrics.newMeter(underlying.getClass(), "1xx-responses", "responses", TimeUnit.SECONDS), // 1xx
                Metrics.newMeter(underlying.getClass(), "2xx-responses", "responses", TimeUnit.SECONDS), // 2xx
                Metrics.newMeter(underlying.getClass(), "3xx-responses", "responses", TimeUnit.SECONDS), // 3xx
                Metrics.newMeter(underlying.getClass(), "4xx-responses", "responses", TimeUnit.SECONDS), // 4xx
                Metrics.newMeter(underlying.getClass(), "5xx-responses", "responses", TimeUnit.SECONDS)  // 5xx
        };

        Metrics.newGauge(underlying.getClass(), "percent-4xx-1m", new RatioGauge() {
            @Override
            protected double getNumerator() {
                return responses[3].oneMinuteRate();
            }

            @Override
            protected double getDenominator() {
                return requests.oneMinuteRate();
            }
        });

        Metrics.newGauge(underlying.getClass(), "percent-4xx-5m", new RatioGauge() {
            @Override
            protected double getNumerator() {
                return responses[3].fiveMinuteRate();
            }

            @Override
            protected double getDenominator() {
                return requests.fiveMinuteRate();
            }
        });

        Metrics.newGauge(underlying.getClass(), "percent-4xx-15m", new RatioGauge() {
            @Override
            protected double getNumerator() {
                return responses[3].fifteenMinuteRate();
            }

            @Override
            protected double getDenominator() {
                return requests.fifteenMinuteRate();
            }
        });

        Metrics.newGauge(underlying.getClass(), "percent-5xx-1m", new RatioGauge() {
            @Override
            protected double getNumerator() {
                return responses[4].oneMinuteRate();
            }

            @Override
            protected double getDenominator() {
                return requests.oneMinuteRate();
            }
        });

        Metrics.newGauge(underlying.getClass(), "percent-5xx-5m", new RatioGauge() {
            @Override
            protected double getNumerator() {
                return responses[4].fiveMinuteRate();
            }

            @Override
            protected double getDenominator() {
                return requests.fiveMinuteRate();
            }
        });

        Metrics.newGauge(underlying.getClass(), "percent-5xx-15m", new RatioGauge() {
            @Override
            protected double getNumerator() {
                return responses[4].fifteenMinuteRate();
            }

            @Override
            protected double getDenominator() {
                return requests.fifteenMinuteRate();
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

        this.getRequests = Metrics.newTimer(underlying.getClass(), "get-requests", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
        this.postRequests = Metrics.newTimer(underlying.getClass(), "post-requests", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
        this.headRequests = Metrics.newTimer(underlying.getClass(), "head-requests", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
        this.putRequests = Metrics.newTimer(underlying.getClass(), "put-requests", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
        this.deleteRequests = Metrics.newTimer(underlying.getClass(), "delete-requests", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
        this.optionsRequests = Metrics.newTimer(underlying.getClass(), "options-requests", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
        this.traceRequests = Metrics.newTimer(underlying.getClass(), "trace-requests", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
        this.connectRequests = Metrics.newTimer(underlying.getClass(), "connect-requests", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
        this.patchRequests = Metrics.newTimer(underlying.getClass(), "patch-requests", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
        this.otherRequests = Metrics.newTimer(underlying.getClass(), "other-requests", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);

        setHandler(underlying);
    }

    @Override
    public void handle(String target, Request request,
                       HttpServletRequest httpRequest, HttpServletResponse httpResponse)
            throws IOException, ServletException {
        activeDispatches.inc();

        final AsyncContinuation continuation = request.getAsyncContinuation();

        final long start;
        final boolean isMilliseconds;

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
                final long duration = System.currentTimeMillis() - start;
                dispatches.update(duration, TimeUnit.MILLISECONDS);
                requestTimer(request.getMethod()).update(duration, TimeUnit.MILLISECONDS);
            } else {
                final long duration = System.nanoTime() - start;
                dispatches.update(duration, TimeUnit.NANOSECONDS);
                requestTimer(request.getMethod()).update(duration, TimeUnit.NANOSECONDS);
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

    private Timer requestTimer(String method) {
        if (GET.equalsIgnoreCase(method)) {
            return getRequests;
        } else if (POST.equalsIgnoreCase(method)) {
            return postRequests;
        } else if (PUT.equalsIgnoreCase(method)) {
            return putRequests;
        } else if (HEAD.equalsIgnoreCase(method)) {
            return headRequests;
        } else if (DELETE.equalsIgnoreCase(method)) {
            return deleteRequests;
        } else if (OPTIONS.equalsIgnoreCase(method)) {
            return optionsRequests;
        } else if (TRACE.equalsIgnoreCase(method)) {
            return traceRequests;
        } else if (CONNECT.equalsIgnoreCase(method)) {
            return connectRequests;
        } else if (PATCH.equalsIgnoreCase(method)) {
            return patchRequests;
        }
        return otherRequests;
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
