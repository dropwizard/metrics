package io.dropwizard.metrics.jetty11;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.RatioGauge;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.ResponseMeteredLevel;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.AsyncContextState;
import org.eclipse.jetty.server.HttpChannel.Listener;
import org.eclipse.jetty.server.HttpChannelState;
import org.eclipse.jetty.server.Request;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;
import static com.codahale.metrics.annotation.ResponseMeteredLevel.ALL;
import static com.codahale.metrics.annotation.ResponseMeteredLevel.COARSE;
import static com.codahale.metrics.annotation.ResponseMeteredLevel.DETAILED;

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
    private static final Set<ResponseMeteredLevel> COARSE_METER_LEVELS = EnumSet.of(COARSE, ALL);
    private static final Set<ResponseMeteredLevel> DETAILED_METER_LEVELS = EnumSet.of(DETAILED, ALL);

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

    private final ResponseMeteredLevel responseMeteredLevel;
    private final List<Meter> responses;
    private final Map<Integer, Meter> responseCodeMeters;
    private final String prefix;
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
        this(registry, null, COARSE);
    }

    /**
     * Create a new instrumented handler using a given metrics registry.
     *
     * @param registry the registry for the metrics
     * @param pref     the prefix to use for the metrics names
     */
    public InstrumentedHttpChannelListener(MetricRegistry registry, String pref) {
        this(registry, pref, COARSE);
    }

    /**
     * Create a new instrumented handler using a given metrics registry.
     *
     * @param registry the registry for the metrics
     * @param pref     the prefix to use for the metrics names
     * @param responseMeteredLevel the level to determine individual/aggregate response codes that are instrumented
     */
    public InstrumentedHttpChannelListener(MetricRegistry registry, String pref, ResponseMeteredLevel responseMeteredLevel) {
        this.metricRegistry = registry;

        this.prefix = (pref == null) ? getClass().getName() : pref;

        this.requests = metricRegistry.timer(name(prefix, "requests"));
        this.dispatches = metricRegistry.timer(name(prefix, "dispatches"));

        this.activeRequests = metricRegistry.counter(name(prefix, "active-requests"));
        this.activeDispatches = metricRegistry.counter(name(prefix, "active-dispatches"));
        this.activeSuspended = metricRegistry.counter(name(prefix, "active-suspended"));

        this.asyncDispatches = metricRegistry.meter(name(prefix, "async-dispatches"));
        this.asyncTimeouts = metricRegistry.meter(name(prefix, "async-timeouts"));

        this.responseMeteredLevel = responseMeteredLevel;
        this.responseCodeMeters = DETAILED_METER_LEVELS.contains(responseMeteredLevel) ? new ConcurrentHashMap<>() : Collections.emptyMap();
        this.responses = COARSE_METER_LEVELS.contains(responseMeteredLevel) ?
                Collections.unmodifiableList(Arrays.asList(
                        registry.meter(name(prefix, "1xx-responses")), // 1xx
                        registry.meter(name(prefix, "2xx-responses")), // 2xx
                        registry.meter(name(prefix, "3xx-responses")), // 3xx
                        registry.meter(name(prefix, "4xx-responses")), // 4xx
                        registry.meter(name(prefix, "5xx-responses"))  // 5xx
                )) : Collections.emptyList();

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
                return Ratio.of(responses.get(3).getOneMinuteRate(),
                        requests.getOneMinuteRate());
            }
        });

        metricRegistry.register(name(prefix, "percent-4xx-5m"), new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(responses.get(3).getFiveMinuteRate(),
                        requests.getFiveMinuteRate());
            }
        });

        metricRegistry.register(name(prefix, "percent-4xx-15m"), new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(responses.get(3).getFifteenMinuteRate(),
                        requests.getFifteenMinuteRate());
            }
        });

        metricRegistry.register(name(prefix, "percent-5xx-1m"), new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(responses.get(4).getOneMinuteRate(),
                        requests.getOneMinuteRate());
            }
        });

        metricRegistry.register(name(prefix, "percent-5xx-5m"), new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(responses.get(4).getFiveMinuteRate(),
                        requests.getFiveMinuteRate());
            }
        });

        metricRegistry.register(name(prefix, "percent-5xx-15m"), new RatioGauge() {
            @Override
            public Ratio getRatio() {
                return Ratio.of(responses.get(4).getFifteenMinuteRate(),
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
        if (isHandled) {
            mark(response.getStatus());
        } else {
            mark(404); // will end up with a 404 response sent by HttpChannel.handle
        }
        activeRequests.dec();
        final long elapsedTime = System.currentTimeMillis() - start;
        requests.update(elapsedTime, TimeUnit.MILLISECONDS);
        requestTimer(request.getMethod()).update(elapsedTime, TimeUnit.MILLISECONDS);
    }

    private void mark(int statusCode) {
        if (DETAILED_METER_LEVELS.contains(responseMeteredLevel)) {
            getResponseCodeMeter(statusCode).mark();
        }

        if (COARSE_METER_LEVELS.contains(responseMeteredLevel)) {
            final int responseStatus = statusCode / 100;
            if (responseStatus >= 1 && responseStatus <= 5) {
                responses.get(responseStatus - 1).mark();
            }
        }
    }

    private Meter getResponseCodeMeter(int statusCode) {
        return responseCodeMeters
                .computeIfAbsent(statusCode, sc -> metricRegistry
                        .meter(name(prefix, String.format("%d-responses", sc))));
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
