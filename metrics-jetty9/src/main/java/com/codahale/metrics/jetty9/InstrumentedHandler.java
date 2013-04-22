package com.codahale.metrics.jetty9;

import com.codahale.metrics.*;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpChannelState;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.HandlerWrapper;

import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * A Jetty {@link Handler} which records various metrics about an underlying {@link Handler}
 * instance.
 */
public class InstrumentedHandler extends HandlerWrapper {
    // the requests handled by this handler, excluding active
    private final Timer requests;

    // the number of dispatches seen by this handler, excluding active
    private final Timer dispatches;

    // the number of active requests
    private final Counter activeRequests;

    // the number of active dispatches
    private final Counter activeDispatches;

    // the number of requests currently suspended.
    private final Counter activeSuspended;

    // the number of requests that have been asynchronously dispatched
    private final Meter asyncDispatches;

    // the number of requests that expired while suspended
    private final Meter asyncTimeouts;

    private final Meter[] responses;

    private final Timer getRequests;
    private final Timer postRequests;
    private final Timer headRequests;
    private final Timer putRequests;
    private final Timer deleteRequests;
    private final Timer optionsRequests;
    private final Timer traceRequests;
    private final Timer connectRequests;
    private final Timer otherRequests;

    private final AsyncListener listener;

    /**
     * Create a new instrumented handler using a given metrics registry. The name of the metric will
     * be derived from the class of the Handler.
     *
     * @param registry   the registry for the metrics
     * @param underlying the handler about which metrics will be collected
     */
    public InstrumentedHandler(MetricRegistry registry, Handler underlying) {
        this(registry, underlying, name(underlying.getClass()));
    }

    /**
     * Create a new instrumented handler using a given metrics registry and a custom prefix.
     *
     * @param registry   the registry for the metrics
     * @param underlying the handler about which metrics will be collected
     * @param prefix     the prefix to use for the metrics names
     */
    public InstrumentedHandler(MetricRegistry registry, Handler underlying, String prefix) {
        super();
        this.requests = registry.timer(name(prefix, "requests"));
        this.dispatches = registry.timer(name(prefix, "dispatches"));

        this.activeRequests = registry.counter(name(prefix, "active-requests"));
        this.activeDispatches = registry.counter(name(prefix, "active-dispatches"));
        this.activeSuspended = registry.counter(name(prefix, "active-suspended"));

        this.asyncDispatches = registry.meter(name(prefix, "async-dispatches"));
        this.asyncTimeouts = registry.meter(name(prefix, "async-timeouts"));

        this.responses = new Meter[]{
                registry.meter(name(prefix, "1xx-responses")), // 1xx
                registry.meter(name(prefix, "2xx-responses")), // 2xx
                registry.meter(name(prefix, "3xx-responses")), // 3xx
                registry.meter(name(prefix, "4xx-responses")), // 4xx
                registry.meter(name(prefix, "5xx-responses"))  // 5xx
        };

        this.getRequests = registry.timer(name(prefix, "get-requests"));
        this.postRequests = registry.timer(name(prefix, "post-requests"));
        this.headRequests = registry.timer(name(prefix, "head-requests"));
        this.putRequests = registry.timer(name(prefix, "put-requests"));
        this.deleteRequests = registry.timer(name(prefix, "delete-requests"));
        this.optionsRequests = registry.timer(name(prefix, "options-requests"));
        this.traceRequests = registry.timer(name(prefix, "trace-requests"));
        this.connectRequests = registry.timer(name(prefix, "connect-requests"));
        this.otherRequests = registry.timer(name(prefix, "other-requests"));

        this.listener = new AsyncListener() {
            @Override
            public void onTimeout(AsyncEvent event) throws IOException {
                asyncTimeouts.mark();
            }

            @Override
            public void onStartAsync(AsyncEvent event) throws IOException {
                event.getAsyncContext().addListener(this);
            }

            @Override
            public void onError(AsyncEvent event) throws IOException {
            }

            @Override
            public void onComplete(AsyncEvent event) throws IOException {
                final HttpChannelState state = (HttpChannelState) event.getAsyncContext();
                final Request request = state.getBaseRequest();
                activeRequests.dec();
                updateResponses(request);
                if (!state.isDispatched()) {
                    activeSuspended.dec();
                }
            }
        };

        setHandler(underlying);
    }

    @Override
    public void handle(String path,
                       Request request,
                       HttpServletRequest httpRequest,
                       HttpServletResponse httpResponse) throws IOException, ServletException {

        activeDispatches.inc();

        final long start;
        final HttpChannelState state = request.getHttpChannelState();
        if (state.isInitial()) {
            // new request
            activeRequests.inc();
            start = request.getTimeStamp();
        } else {
            // resumed request
            start = System.currentTimeMillis();
            activeSuspended.dec();
            if (state.isDispatched()) {
                asyncDispatches.mark();
            }
        }

        try {
            super.handle(path, request, httpRequest, httpResponse);
        } finally {
            final long now = System.currentTimeMillis();
            final long dispatched = now - start;

            activeDispatches.dec();
            dispatches.update(dispatched, TimeUnit.MILLISECONDS);

            if (state.isSuspended()) {
                if (state.isInitial()) {
                    state.addListener(listener);
                }
                activeSuspended.inc();
            } else if (state.isInitial()) {
                activeRequests.dec();
                requests.update(dispatched, TimeUnit.MILLISECONDS);
                updateResponses(request);
            }
            // else onCompletion will handle it.
        }
    }

    private Timer requestTimer(String method) {
        final HttpMethod m = HttpMethod.fromString(method);
        if (m == null) {
            return otherRequests;
        } else {
            switch (m) {
                case GET:
                    return getRequests;
                case POST:
                    return postRequests;
                case PUT:
                    return putRequests;
                case HEAD:
                    return headRequests;
                case DELETE:
                    return deleteRequests;
                case OPTIONS:
                    return optionsRequests;
                case TRACE:
                    return traceRequests;
                case CONNECT:
                    return connectRequests;
                default:
                    return otherRequests;
            }
        }
    }

    private void updateResponses(Request request) {
        final int response = request.getResponse().getStatus() / 100;
        if (response >= 1 && response <= 5) {
            responses[response - 1].mark();
        }
        activeRequests.dec();
        final long elapsedTime = System.currentTimeMillis() - request.getTimeStamp();
        requests.update(elapsedTime, TimeUnit.MILLISECONDS);
        requestTimer(request.getMethod()).update(elapsedTime, TimeUnit.MILLISECONDS);
    }
}
