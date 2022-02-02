package io.dropwizard.metrics5.jetty10;

import io.dropwizard.metrics5.Counter;
import io.dropwizard.metrics5.Meter;
import io.dropwizard.metrics5.MetricName;
import io.dropwizard.metrics5.MetricRegistry;
import io.dropwizard.metrics5.RatioGauge;
import io.dropwizard.metrics5.Timer;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.AsyncContextState;
import org.eclipse.jetty.server.HttpChannel.Listener;
import org.eclipse.jetty.server.HttpChannelState;
import org.eclipse.jetty.server.Request;

import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

/**
 * A Jetty {@link org.eclipse.jetty.server.HttpChannel.Listener} implementation which records various metrics about
 * underlying channel instance. Unlike {@link InstrumentedHandler} that uses internal API, this class should be
 * future proof. To install it, just add instance of this class to {@link org.eclipse.jetty.server.Connector} as bean.
 *
 * @since TBD
 */
public class InstrumentedHttpChannelListener
        implements Listener {
    private static final String START_ATTR = InstrumentedHttpChannelListener.class.getName() + ".start";

    private final MetricRegistry metricRegistry;

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
    private final Timer moveRequests;
    private final Timer otherRequests;

    private final AsyncListener listener;

    /**
     * Create a new instrumented handler using a given metrics registry.
     *
     * @param registry the registry for the metrics
     */
    public InstrumentedHttpChannelListener(MetricRegistry registry) {
        this(registry, null);
    }

    /**
     * Create a new instrumented handler using a given metrics registry.
     *
     * @param registry the registry for the metrics
     * @param pref     the prefix to use for the metrics names
     */
    public InstrumentedHttpChannelListener(MetricRegistry registry, MetricName pref) {
        this.metricRegistry = registry;

        MetricName prefix = (pref == null) ? MetricName.build(getClass().getName()) : pref;

        this.requests = metricRegistry.timer(prefix.resolve("requests"));
        this.dispatches = metricRegistry.timer(prefix.resolve("dispatches"));

        this.activeRequests = metricRegistry.counter(prefix.resolve("active-requests"));
        this.activeDispatches = metricRegistry.counter(prefix.resolve("active-dispatches"));
        this.activeSuspended = metricRegistry.counter(prefix.resolve("active-suspended"));

        this.asyncDispatches = metricRegistry.meter(prefix.resolve("async-dispatches"));
        this.asyncTimeouts = metricRegistry.meter(prefix.resolve("async-timeouts"));

        this.responses = new Meter[]{
                metricRegistry.meter(prefix.resolve("1xx-responses")), // 1xx
                metricRegistry.meter(prefix.resolve("2xx-responses")), // 2xx
                metricRegistry.meter(prefix.resolve("3xx-responses")), // 3xx
                metricRegistry.meter(prefix.resolve("4xx-responses")), // 4xx
                metricRegistry.meter(prefix.resolve("5xx-responses"))  // 5xx
        };

        this.getRequests = metricRegistry.timer(prefix.resolve("get-requests"));
        this.postRequests = metricRegistry.timer(prefix.resolve("post-requests"));
        this.headRequests = metricRegistry.timer(prefix.resolve("head-requests"));
        this.putRequests = metricRegistry.timer(prefix.resolve("put-requests"));
        this.deleteRequests = metricRegistry.timer(prefix.resolve("delete-requests"));
        this.optionsRequests = metricRegistry.timer(prefix.resolve("options-requests"));
        this.traceRequests = metricRegistry.timer(prefix.resolve("trace-requests"));
        this.connectRequests = metricRegistry.timer(prefix.resolve("connect-requests"));
        this.moveRequests = metricRegistry.timer(prefix.resolve("move-requests"));
        this.otherRequests = metricRegistry.timer(prefix.resolve("other-requests"));

        metricRegistry.register(prefix.resolve("percent-4xx-1m"), new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(responses[3].getOneMinuteRate(),
                        requests.getOneMinuteRate());
            }
        });

        metricRegistry.register(prefix.resolve("percent-4xx-5m"), new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(responses[3].getFiveMinuteRate(),
                        requests.getFiveMinuteRate());
            }
        });

        metricRegistry.register(prefix.resolve("percent-4xx-15m"), new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(responses[3].getFifteenMinuteRate(),
                        requests.getFifteenMinuteRate());
            }
        });

        metricRegistry.register(prefix.resolve("percent-5xx-1m"), new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(responses[4].getOneMinuteRate(),
                        requests.getOneMinuteRate());
            }
        });

        metricRegistry.register(prefix.resolve("percent-5xx-5m"), new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(responses[4].getFiveMinuteRate(),
                        requests.getFiveMinuteRate());
            }
        });

        metricRegistry.register(prefix.resolve("percent-5xx-15m"), new RatioGauge() {
            @Override
            public RatioGauge.Ratio getRatio() {
                return Ratio.of(responses[4].getFifteenMinuteRate(),
                        requests.getFifteenMinuteRate());
            }
        });

        this.listener = new AsyncAttachingListener();
    }

    @Override
    public void onRequestBegin(final Request request) {

    }

    @Override
    public void onBeforeDispatch(final Request request) {
        before(request);
    }

    @Override
    public void onDispatchFailure(final Request request, final Throwable failure) {

    }

    @Override
    public void onAfterDispatch(final Request request) {
        after(request);
    }

    @Override
    public void onRequestContent(final Request request, final ByteBuffer content) {

    }

    @Override
    public void onRequestContentEnd(final Request request) {

    }

    @Override
    public void onRequestTrailers(final Request request) {

    }

    @Override
    public void onRequestEnd(final Request request) {

    }

    @Override
    public void onRequestFailure(final Request request, final Throwable failure) {

    }

    @Override
    public void onResponseBegin(final Request request) {

    }

    @Override
    public void onResponseCommit(final Request request) {

    }

    @Override
    public void onResponseContent(final Request request, final ByteBuffer content) {

    }

    @Override
    public void onResponseEnd(final Request request) {

    }

    @Override
    public void onResponseFailure(final Request request, final Throwable failure) {

    }

    @Override
    public void onComplete(final Request request) {

    }

    private void before(final Request request) {
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
            if (state.isAsyncStarted()) {
                asyncDispatches.mark();
            }
        }
        request.setAttribute(START_ATTR, start);
    }

    private void after(final Request request) {
        final long start = (long) request.getAttribute(START_ATTR);
        final long now = System.currentTimeMillis();
        final long dispatched = now - start;

        activeDispatches.dec();
        dispatches.update(dispatched, TimeUnit.MILLISECONDS);

        final HttpChannelState state = request.getHttpChannelState();
        if (state.isSuspended()) {
            activeSuspended.inc();
        } else if (state.isInitial()) {
            updateResponses(request, request.getResponse(), start, request.isHandled());
        }
        // else onCompletion will handle it.
    }

    private void updateResponses(HttpServletRequest request, HttpServletResponse response, long start, boolean isHandled) {
        final int responseStatus;
        if (isHandled) {
            responseStatus = response.getStatus() / 100;
        } else {
            responseStatus = 4; // will end up with a 404 response sent by HttpChannel.handle
        }
        if (responseStatus >= 1 && responseStatus <= 5) {
            responses[responseStatus - 1].mark();
        }
        activeRequests.dec();
        final long elapsedTime = System.currentTimeMillis() - start;
        requests.update(elapsedTime, TimeUnit.MILLISECONDS);
        requestTimer(request.getMethod()).update(elapsedTime, TimeUnit.MILLISECONDS);
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

    private class AsyncAttachingListener implements AsyncListener {

        @Override
        public void onTimeout(AsyncEvent event) throws IOException {}

        @Override
        public void onStartAsync(AsyncEvent event) throws IOException {
            event.getAsyncContext().addListener(new InstrumentedAsyncListener());
        }

        @Override
        public void onError(AsyncEvent event) throws IOException {}

        @Override
        public void onComplete(AsyncEvent event) throws IOException {}
    };

    private class InstrumentedAsyncListener implements AsyncListener {
        private final long startTime;

        InstrumentedAsyncListener() {
            this.startTime = System.currentTimeMillis();
        }

        @Override
        public void onTimeout(AsyncEvent event) throws IOException {
            asyncTimeouts.mark();
        }

        @Override
        public void onStartAsync(AsyncEvent event) throws IOException {
        }

        @Override
        public void onError(AsyncEvent event) throws IOException {
        }

        @Override
        public void onComplete(AsyncEvent event) throws IOException {
            final AsyncContextState state = (AsyncContextState) event.getAsyncContext();
            final HttpServletRequest request = (HttpServletRequest) state.getRequest();
            final HttpServletResponse response = (HttpServletResponse) state.getResponse();
            updateResponses(request, response, startTime, true);
            if (!state.getHttpChannelState().isSuspended()) {
                activeSuspended.dec();
            }
        }
    }
}
