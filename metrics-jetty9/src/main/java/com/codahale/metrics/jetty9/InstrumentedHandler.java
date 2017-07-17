package com.codahale.metrics.jetty9;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.RatioGauge;
import com.codahale.metrics.Timer;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.AsyncContextState;
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
    private final MetricRegistry metricRegistry;

    private String name;
    private final String prefix;

    // the requests handled by this handler, excluding active
    private Timer requests;

    // the number of dispatches seen by this handler, excluding active
    private Timer dispatches;

    // the number of active requests
    private Counter activeRequests;

    // the number of active dispatches
    private Counter activeDispatches;

    // the number of requests currently suspended.
    private Counter activeSuspended;

    // the number of requests that have been asynchronously dispatched
    private Meter asyncDispatches;

    // the number of requests that expired while suspended
    private Meter asyncTimeouts;

    private Meter[] responses;

    private Timer getRequests;
    private Timer postRequests;
    private Timer headRequests;
    private Timer putRequests;
    private Timer deleteRequests;
    private Timer optionsRequests;
    private Timer traceRequests;
    private Timer connectRequests;
    private Timer moveRequests;
    private Timer otherRequests;

    private AsyncListener listener;

    /**
     * Create a new instrumented handler using a given metrics registry.
     *
     * @param registry   the registry for the metrics
     *
     */
    public InstrumentedHandler(MetricRegistry registry) {
        this(registry, null);
    }

	/**
	 * Create a new instrumented handler using a given metrics registry.
	 *
	 * @param registry   the registry for the metrics
	 * @param prefix     the prefix to use for the metrics names
	 *
	 */
	public InstrumentedHandler(MetricRegistry registry, String prefix) {
		this.metricRegistry = registry;
		this.prefix = prefix;
	}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        final String prefix = this.prefix == null ? name(getHandler().getClass(), name) : name(this.prefix, name);

        this.requests = metricRegistry.timer(name(prefix, "requests"));
        this.dispatches = metricRegistry.timer(name(prefix, "dispatches"));

        this.activeRequests = metricRegistry.counter(name(prefix, "active-requests"));
        this.activeDispatches = metricRegistry.counter(name(prefix, "active-dispatches"));
        this.activeSuspended = metricRegistry.counter(name(prefix, "active-suspended"));

        this.asyncDispatches = metricRegistry.meter(name(prefix, "async-dispatches"));
        this.asyncTimeouts = metricRegistry.meter(name(prefix, "async-timeouts"));

        this.responses = new Meter[]{
                metricRegistry.meter(name(prefix, "1xx-responses")), // 1xx
                metricRegistry.meter(name(prefix, "2xx-responses")), // 2xx
                metricRegistry.meter(name(prefix, "3xx-responses")), // 3xx
                metricRegistry.meter(name(prefix, "4xx-responses")), // 4xx
                metricRegistry.meter(name(prefix, "5xx-responses"))  // 5xx
        };

        this.getRequests = metricRegistry.timer(name(prefix, "get-requests"));
        this.postRequests = metricRegistry.timer(name(prefix, "post-requests"));
        this.headRequests = metricRegistry.timer(name(prefix, "head-requests"));
        this.putRequests = metricRegistry.timer(name(prefix, "put-requests"));
        this.deleteRequests = metricRegistry.timer(name(prefix, "delete-requests"));
        this.optionsRequests = metricRegistry.timer(name(prefix, "options-requests"));
        this.traceRequests = metricRegistry.timer(name(prefix, "trace-requests"));
        this.connectRequests = metricRegistry.timer(name(prefix, "connect-requests"));
        this.moveRequests = metricRegistry.timer(name(prefix, "move-requests"));
        this.otherRequests = metricRegistry.timer(name(prefix, "other-requests"));

        metricRegistry.register(name(prefix, "percent-4xx-1m"), new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(responses[3].getOneMinuteRate(),
                        requests.getOneMinuteRate());
            }
        });

        metricRegistry.register(name(prefix, "percent-4xx-5m"), new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(responses[3].getFiveMinuteRate(),
                        requests.getFiveMinuteRate());
            }
        });

        metricRegistry.register(name(prefix, "percent-4xx-15m"), new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(responses[3].getFifteenMinuteRate(),
                        requests.getFifteenMinuteRate());
            }
        });

        metricRegistry.register(name(prefix, "percent-5xx-1m"), new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(responses[4].getOneMinuteRate(),
                        requests.getOneMinuteRate());
            }
        });

        metricRegistry.register(name(prefix, "percent-5xx-5m"), new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(responses[4].getFiveMinuteRate(),
                        requests.getFiveMinuteRate());
            }
        });

        metricRegistry.register(name(prefix, "percent-5xx-15m"), new RatioGauge() {
            @Override
            public Ratio getRatio() {
                return Ratio.of(responses[4].getFifteenMinuteRate(),
                        requests.getFifteenMinuteRate());
            }
        });


        this.listener = new AsyncListener() {
            private long startTime;

            @Override
            public void onTimeout(AsyncEvent event) throws IOException {
                asyncTimeouts.mark();
            }

            @Override
            public void onStartAsync(AsyncEvent event) throws IOException {
                startTime = System.currentTimeMillis();
                event.getAsyncContext().addListener(this);
            }

            @Override
            public void onError(AsyncEvent event) throws IOException {
            }

            @Override
            public void onComplete(AsyncEvent event) throws IOException {
                final AsyncContextState state = (AsyncContextState) event.getAsyncContext();
                final HttpServletRequest request = (HttpServletRequest) state.getRequest();
                final HttpServletResponse response = (HttpServletResponse) state.getResponse();
                updateResponses(request, response, startTime);
                if (state.getHttpChannelState().getState() != HttpChannelState.State.DISPATCHED) {
                    activeSuspended.dec();
                }
            }
        };
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
            state.addListener(listener);
        } else {
            // resumed request
            start = System.currentTimeMillis();
            activeSuspended.dec();
            if (state.getState() == HttpChannelState.State.DISPATCHED) {
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
                activeSuspended.inc();
            } else if (state.isInitial()) {
                updateResponses(httpRequest, httpResponse, start);
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
                case MOVE:
                    return moveRequests;
                default:
                    return otherRequests;
            }
        }
    }

    private void updateResponses(HttpServletRequest request, HttpServletResponse response, long start) {
        final int responseStatus = response.getStatus() / 100;
        if (responseStatus >= 1 && responseStatus <= 5) {
            responses[responseStatus - 1].mark();
        }
        activeRequests.dec();
        final long elapsedTime = System.currentTimeMillis() - start;
        requests.update(elapsedTime, TimeUnit.MILLISECONDS);
        requestTimer(request.getMethod()).update(elapsedTime, TimeUnit.MILLISECONDS);
    }
}
