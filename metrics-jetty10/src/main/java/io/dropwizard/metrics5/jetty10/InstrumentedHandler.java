package io.dropwizard.metrics5.jetty10;

import io.dropwizard.metrics5.Counter;
import io.dropwizard.metrics5.Meter;
import io.dropwizard.metrics5.MetricName;
import io.dropwizard.metrics5.MetricRegistry;
import io.dropwizard.metrics5.RatioGauge;
import io.dropwizard.metrics5.Timer;
import io.dropwizard.metrics5.annotation.ResponseMeteredLevel;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static io.dropwizard.metrics5.MetricRegistry.name;
import static io.dropwizard.metrics5.annotation.ResponseMeteredLevel.ALL;
import static io.dropwizard.metrics5.annotation.ResponseMeteredLevel.COARSE;
import static io.dropwizard.metrics5.annotation.ResponseMeteredLevel.DETAILED;

/**
 * A Jetty {@link Handler} which records various metrics about an underlying {@link Handler}
 * instance.
 */
public class InstrumentedHandler extends HandlerWrapper {
    private static final String NAME_REQUESTS = "requests";
    private static final String NAME_DISPATCHES = "dispatches";
    private static final String NAME_ACTIVE_REQUESTS = "active-requests";
    private static final String NAME_ACTIVE_DISPATCHES = "active-dispatches";
    private static final String NAME_ACTIVE_SUSPENDED = "active-suspended";
    private static final String NAME_ASYNC_DISPATCHES = "async-dispatches";
    private static final String NAME_ASYNC_TIMEOUTS = "async-timeouts";
    private static final String NAME_1XX_RESPONSES = "1xx-responses";
    private static final String NAME_2XX_RESPONSES = "2xx-responses";
    private static final String NAME_3XX_RESPONSES = "3xx-responses";
    private static final String NAME_4XX_RESPONSES = "4xx-responses";
    private static final String NAME_5XX_RESPONSES = "5xx-responses";
    private static final String NAME_GET_REQUESTS = "get-requests";
    private static final String NAME_POST_REQUESTS = "post-requests";
    private static final String NAME_HEAD_REQUESTS = "head-requests";
    private static final String NAME_PUT_REQUESTS = "put-requests";
    private static final String NAME_DELETE_REQUESTS = "delete-requests";
    private static final String NAME_OPTIONS_REQUESTS = "options-requests";
    private static final String NAME_TRACE_REQUESTS = "trace-requests";
    private static final String NAME_CONNECT_REQUESTS = "connect-requests";
    private static final String NAME_MOVE_REQUESTS = "move-requests";
    private static final String NAME_OTHER_REQUESTS = "other-requests";
    private static final String NAME_PERCENT_4XX_1M = "percent-4xx-1m";
    private static final String NAME_PERCENT_4XX_5M = "percent-4xx-5m";
    private static final String NAME_PERCENT_4XX_15M = "percent-4xx-15m";
    private static final String NAME_PERCENT_5XX_1M = "percent-5xx-1m";
    private static final String NAME_PERCENT_5XX_5M = "percent-5xx-5m";
    private static final String NAME_PERCENT_5XX_15M = "percent-5xx-15m";
    private static final Set<ResponseMeteredLevel> COARSE_METER_LEVELS = EnumSet.of(COARSE, ALL);
    private static final Set<ResponseMeteredLevel> DETAILED_METER_LEVELS = EnumSet.of(DETAILED, ALL);

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

    private final ResponseMeteredLevel responseMeteredLevel;
    private List<Meter> responses;
    private Map<Integer, Meter> responseCodeMeters;

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
     * @param registry the registry for the metrics
     */
    public InstrumentedHandler(MetricRegistry registry) {
        this(registry, null);
    }

    /**
     * Create a new instrumented handler using a given metrics registry.
     *
     * @param registry the registry for the metrics
     * @param prefix   the prefix to use for the metrics names
     */
    public InstrumentedHandler(MetricRegistry registry, String prefix) {
        this(registry, prefix, COARSE);
    }

    /**
     * Create a new instrumented handler using a given metrics registry.
     *
     * @param registry the registry for the metrics
     * @param prefix   the prefix to use for the metrics names
     * @param responseMeteredLevel the level to determine individual/aggregate response codes that are instrumented
     */
    public InstrumentedHandler(MetricRegistry registry, String prefix, ResponseMeteredLevel responseMeteredLevel) {
        this.metricRegistry = registry;
        this.prefix = prefix;
        this.responseMeteredLevel = responseMeteredLevel;
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

        final MetricName prefix = getMetricPrefix();

        this.requests = metricRegistry.timer(prefix.resolve(NAME_REQUESTS));
        this.dispatches = metricRegistry.timer(prefix.resolve(NAME_DISPATCHES));

        this.activeRequests = metricRegistry.counter(prefix.resolve(NAME_ACTIVE_REQUESTS));
        this.activeDispatches = metricRegistry.counter(prefix.resolve(NAME_ACTIVE_DISPATCHES));
        this.activeSuspended = metricRegistry.counter(prefix.resolve(NAME_ACTIVE_SUSPENDED));

        this.asyncDispatches = metricRegistry.meter(prefix.resolve(NAME_ASYNC_DISPATCHES));
        this.asyncTimeouts = metricRegistry.meter(prefix.resolve(NAME_ASYNC_TIMEOUTS));

        this.responseCodeMeters = DETAILED_METER_LEVELS.contains(responseMeteredLevel) ? new ConcurrentHashMap<>() : Collections.emptyMap();
        this.responses = COARSE_METER_LEVELS.contains(responseMeteredLevel) ?
                Collections.unmodifiableList(Arrays.asList(
                        metricRegistry.meter(prefix.resolve(NAME_1XX_RESPONSES)), // 1xx
                        metricRegistry.meter(prefix.resolve(NAME_2XX_RESPONSES)), // 2xx
                        metricRegistry.meter(prefix.resolve(NAME_3XX_RESPONSES)), // 3xx
                        metricRegistry.meter(prefix.resolve(NAME_4XX_RESPONSES)), // 4xx
                        metricRegistry.meter(prefix.resolve(NAME_5XX_RESPONSES))  // 5xx
                )) : Collections.emptyList();

        this.getRequests = metricRegistry.timer(prefix.resolve(NAME_GET_REQUESTS));
        this.postRequests = metricRegistry.timer(prefix.resolve(NAME_POST_REQUESTS));
        this.headRequests = metricRegistry.timer(prefix.resolve(NAME_HEAD_REQUESTS));
        this.putRequests = metricRegistry.timer(prefix.resolve(NAME_PUT_REQUESTS));
        this.deleteRequests = metricRegistry.timer(prefix.resolve(NAME_DELETE_REQUESTS));
        this.optionsRequests = metricRegistry.timer(prefix.resolve(NAME_OPTIONS_REQUESTS));
        this.traceRequests = metricRegistry.timer(prefix.resolve(NAME_TRACE_REQUESTS));
        this.connectRequests = metricRegistry.timer(prefix.resolve(NAME_CONNECT_REQUESTS));
        this.moveRequests = metricRegistry.timer(prefix.resolve(NAME_MOVE_REQUESTS));
        this.otherRequests = metricRegistry.timer(prefix.resolve(NAME_OTHER_REQUESTS));

        metricRegistry.register(prefix.resolve(NAME_PERCENT_4XX_1M), new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(responses.get(3).getOneMinuteRate(),
                        requests.getOneMinuteRate());
            }
        });

        metricRegistry.register(prefix.resolve(NAME_PERCENT_4XX_5M), new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(responses.get(3).getFiveMinuteRate(),
                        requests.getFiveMinuteRate());
            }
        });

        metricRegistry.register(prefix.resolve(NAME_PERCENT_4XX_15M), new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(responses.get(3).getFifteenMinuteRate(),
                        requests.getFifteenMinuteRate());
            }
        });

        metricRegistry.register(prefix.resolve(NAME_PERCENT_5XX_1M), new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(responses.get(4).getOneMinuteRate(),
                        requests.getOneMinuteRate());
            }
        });

        metricRegistry.register(prefix.resolve(NAME_PERCENT_5XX_5M), new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(responses.get(4).getFiveMinuteRate(),
                        requests.getFiveMinuteRate());
            }
        });

        metricRegistry.register(prefix.resolve(NAME_PERCENT_5XX_15M), new RatioGauge() {
            @Override
            public Ratio getRatio() {
                return Ratio.of(responses.get(4).getFifteenMinuteRate(),
                        requests.getFifteenMinuteRate());
            }
        });


        this.listener = new AsyncAttachingListener();
    }

    @Override
    protected void doStop() throws Exception {
        final MetricName prefix = getMetricPrefix();
        
        metricRegistry.remove(prefix.resolve(NAME_REQUESTS));
        metricRegistry.remove(prefix.resolve(NAME_DISPATCHES));
        metricRegistry.remove(prefix.resolve(NAME_ACTIVE_REQUESTS));
        metricRegistry.remove(prefix.resolve(NAME_ACTIVE_DISPATCHES));
        metricRegistry.remove(prefix.resolve(NAME_ACTIVE_SUSPENDED));
        metricRegistry.remove(prefix.resolve(NAME_ASYNC_DISPATCHES));
        metricRegistry.remove(prefix.resolve(NAME_ASYNC_TIMEOUTS));
        metricRegistry.remove(prefix.resolve(NAME_1XX_RESPONSES));
        metricRegistry.remove(prefix.resolve(NAME_2XX_RESPONSES));
        metricRegistry.remove(prefix.resolve(NAME_3XX_RESPONSES));
        metricRegistry.remove(prefix.resolve(NAME_4XX_RESPONSES));
        metricRegistry.remove(prefix.resolve(NAME_5XX_RESPONSES));
        metricRegistry.remove(prefix.resolve(NAME_GET_REQUESTS));
        metricRegistry.remove(prefix.resolve(NAME_POST_REQUESTS));
        metricRegistry.remove(prefix.resolve(NAME_HEAD_REQUESTS));
        metricRegistry.remove(prefix.resolve(NAME_PUT_REQUESTS));
        metricRegistry.remove(prefix.resolve(NAME_DELETE_REQUESTS));
        metricRegistry.remove(prefix.resolve(NAME_OPTIONS_REQUESTS));
        metricRegistry.remove(prefix.resolve(NAME_TRACE_REQUESTS));
        metricRegistry.remove(prefix.resolve(NAME_CONNECT_REQUESTS));
        metricRegistry.remove(prefix.resolve(NAME_MOVE_REQUESTS));
        metricRegistry.remove(prefix.resolve(NAME_OTHER_REQUESTS));
        metricRegistry.remove(prefix.resolve(NAME_PERCENT_4XX_1M));
        metricRegistry.remove(prefix.resolve(NAME_PERCENT_4XX_5M));
        metricRegistry.remove(prefix.resolve(NAME_PERCENT_4XX_15M));
        metricRegistry.remove(prefix.resolve(NAME_PERCENT_5XX_1M));
        metricRegistry.remove(prefix.resolve(NAME_PERCENT_5XX_5M));
        metricRegistry.remove(prefix.resolve(NAME_PERCENT_5XX_15M));

        if (responseCodeMeters != null) {
            responseCodeMeters.keySet().stream()
                    .map(sc -> getMetricPrefix().resolve(String.format("%d-responses", sc)))
                    .forEach(metricRegistry::remove);
        }

        super.doStop();
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
            if (state.getState() == HttpChannelState.State.HANDLING) {
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
                updateResponses(httpRequest, httpResponse, start, request.isHandled());
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

    private void updateResponses(HttpServletRequest request, HttpServletResponse response, long start, boolean isHandled) {
        if (isHandled) {
            mark(response.getStatus());
        } else {
            mark(404);; // will end up with a 404 response sent by HttpChannel.handle
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
                        .meter(getMetricPrefix().resolve(String.format("%d-responses", sc))));
    }

    private MetricName getMetricPrefix() {
        return this.prefix == null ? name(getHandler().getClass(), name) : name(this.prefix, name);
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
