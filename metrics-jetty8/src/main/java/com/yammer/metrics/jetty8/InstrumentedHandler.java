package com.yammer.metrics.jetty8;

import com.yammer.metrics.*;
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

import static com.yammer.metrics.MetricRegistry.name;
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
     * Create a new instrumented handler using a given metrics registry. The name of the metric
     * will be derived from the class of the Handler.
     *
     * @param registry the registry for the metrics
     * @param underlying the handler about which metrics will be collected
     */
    public InstrumentedHandler(MetricRegistry registry, Handler underlying) {
    	this(registry, underlying, underlying.getClass());
    }

    /**
     * Create a new instrumented handler using a given metrics registry and a custom prefix.
     *
     * @param registry the registry for the metrics
     * @param underlying the handler about which metrics will be collected
     * @param prefix the prefix to use for the metrics names
     */    
    public InstrumentedHandler(MetricRegistry registry, Handler underlying, String prefix) {
        super();
        this.dispatches = registry.timer(name(prefix, "dispatches"));
        this.requests = registry.meter(name(prefix, "requests"));
        this.resumes = registry.meter(name(prefix, "resumes"));
        this.suspends = registry.meter(name(prefix, "suspends"));
        this.expires = registry.meter(name(prefix, "expires"));

        this.activeRequests = registry.counter(name(prefix, "active-requests"));
        this.activeSuspendedRequests = registry.counter(name(prefix,
                                                             "active-suspended-requests"));
        this.activeDispatches = registry.counter(name(prefix, "active-dispatches"));

        this.responses = new Meter[]{
                registry.meter(name(prefix, "1xx-responses")), // 1xx
                registry.meter(name(prefix, "2xx-responses")), // 2xx
                registry.meter(name(prefix, "3xx-responses")), // 3xx
                registry.meter(name(prefix, "4xx-responses")), // 4xx
                registry.meter(name(prefix, "5xx-responses"))  // 5xx
        };

        registry.register(name(prefix, "percent-4xx-1m"), new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(responses[3].getOneMinuteRate(),
                                requests.getOneMinuteRate());
            }
        });

        registry.register(name(prefix, "percent-4xx-5m"), new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(responses[3].getFiveMinuteRate(),
                                requests.getFiveMinuteRate());
            }
        });

        registry.register(name(prefix, "percent-4xx-15m"), new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(responses[3].getFifteenMinuteRate(),
                                requests.getFifteenMinuteRate());
            }
        });

        registry.register(name(prefix, "percent-5xx-1m"), new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(responses[4].getOneMinuteRate(),
                                requests.getOneMinuteRate());
            }
        });

        registry.register(name(prefix, "percent-5xx-5m"), new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(responses[4].getFiveMinuteRate(),
                                requests.getFiveMinuteRate());
            }
        });

        registry.register(name(prefix, "percent-5xx-15m"), new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(responses[4].getFifteenMinuteRate(),
                                requests.getFifteenMinuteRate());
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

        this.getRequests = registry.timer(name(prefix, "get-requests"));
        this.postRequests = registry.timer(name(prefix, "post-requests"));
        this.headRequests = registry.timer(name(prefix, "head-requests"));
        this.putRequests = registry.timer(name(prefix, "put-requests"));
        this.deleteRequests = registry.timer(name(prefix, "delete-requests"));
        this.optionsRequests = registry.timer(name(prefix, "options-requests"));
        this.traceRequests = registry.timer(name(prefix, "trace-requests"));
        this.connectRequests = registry.timer(name(prefix, "connect-requests"));
        this.patchRequests = registry.timer(name(prefix, "patch-requests"));
        this.otherRequests = registry.timer(name(prefix, "other-requests"));

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
