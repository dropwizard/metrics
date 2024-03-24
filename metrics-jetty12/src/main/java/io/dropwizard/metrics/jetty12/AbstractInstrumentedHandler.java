package io.dropwizard.metrics.jetty12;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.RatioGauge;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.ResponseMeteredLevel;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.codahale.metrics.MetricRegistry.name;
import static com.codahale.metrics.annotation.ResponseMeteredLevel.ALL;
import static com.codahale.metrics.annotation.ResponseMeteredLevel.COARSE;
import static com.codahale.metrics.annotation.ResponseMeteredLevel.DETAILED;

/**
 * An abstract base class of a Jetty {@link Handler} which records various metrics about an underlying {@link Handler}
 * instance.
 */
public abstract class AbstractInstrumentedHandler extends Handler.Wrapper {
    protected static final String NAME_REQUESTS = "requests";
    protected static final String NAME_DISPATCHES = "dispatches";
    protected static final String NAME_ACTIVE_REQUESTS = "active-requests";
    protected static final String NAME_ACTIVE_DISPATCHES = "active-dispatches";
    protected static final String NAME_ACTIVE_SUSPENDED = "active-suspended";
    protected static final String NAME_ASYNC_DISPATCHES = "async-dispatches";
    protected static final String NAME_ASYNC_TIMEOUTS = "async-timeouts";
    protected static final String NAME_1XX_RESPONSES = "1xx-responses";
    protected static final String NAME_2XX_RESPONSES = "2xx-responses";
    protected static final String NAME_3XX_RESPONSES = "3xx-responses";
    protected static final String NAME_4XX_RESPONSES = "4xx-responses";
    protected static final String NAME_5XX_RESPONSES = "5xx-responses";
    protected static final String NAME_GET_REQUESTS = "get-requests";
    protected static final String NAME_POST_REQUESTS = "post-requests";
    protected static final String NAME_HEAD_REQUESTS = "head-requests";
    protected static final String NAME_PUT_REQUESTS = "put-requests";
    protected static final String NAME_DELETE_REQUESTS = "delete-requests";
    protected static final String NAME_OPTIONS_REQUESTS = "options-requests";
    protected static final String NAME_TRACE_REQUESTS = "trace-requests";
    protected static final String NAME_CONNECT_REQUESTS = "connect-requests";
    protected static final String NAME_MOVE_REQUESTS = "move-requests";
    protected static final String NAME_OTHER_REQUESTS = "other-requests";
    protected static final String NAME_PERCENT_4XX_1M = "percent-4xx-1m";
    protected static final String NAME_PERCENT_4XX_5M = "percent-4xx-5m";
    protected static final String NAME_PERCENT_4XX_15M = "percent-4xx-15m";
    protected static final String NAME_PERCENT_5XX_1M = "percent-5xx-1m";
    protected static final String NAME_PERCENT_5XX_5M = "percent-5xx-5m";
    protected static final String NAME_PERCENT_5XX_15M = "percent-5xx-15m";
    protected static final Set<ResponseMeteredLevel> COARSE_METER_LEVELS = EnumSet.of(COARSE, ALL);
    protected static final Set<ResponseMeteredLevel> DETAILED_METER_LEVELS = EnumSet.of(DETAILED, ALL);

    protected final MetricRegistry metricRegistry;

    private String name;
    protected final String prefix;

    // the requests handled by this handler, excluding active
    protected Timer requests;

    // the number of dispatches seen by this handler, excluding active
    protected Timer dispatches;

    // the number of active requests
    protected Counter activeRequests;

    // the number of active dispatches
    protected Counter activeDispatches;

    // the number of requests currently suspended.
    protected Counter activeSuspended;

    // the number of requests that have been asynchronously dispatched
    protected Meter asyncDispatches;

    // the number of requests that expired while suspended
    protected Meter asyncTimeouts;

    protected final ResponseMeteredLevel responseMeteredLevel;
    protected List<Meter> responses;
    protected Map<Integer, Meter> responseCodeMeters;

    protected Timer getRequests;
    protected Timer postRequests;
    protected Timer headRequests;
    protected Timer putRequests;
    protected Timer deleteRequests;
    protected Timer optionsRequests;
    protected Timer traceRequests;
    protected Timer connectRequests;
    protected Timer moveRequests;
    protected Timer otherRequests;

    /**
     * Create a new instrumented handler using a given metrics registry.
     *
     * @param registry the registry for the metrics
     */
    protected AbstractInstrumentedHandler(MetricRegistry registry) {
        this(registry, null);
    }

    /**
     * Create a new instrumented handler using a given metrics registry.
     *
     * @param registry the registry for the metrics
     * @param prefix   the prefix to use for the metrics names
     */
    protected AbstractInstrumentedHandler(MetricRegistry registry, String prefix) {
        this(registry, prefix, COARSE);
    }

    /**
     * Create a new instrumented handler using a given metrics registry.
     *
     * @param registry the registry for the metrics
     * @param prefix   the prefix to use for the metrics names
     * @param responseMeteredLevel the level to determine individual/aggregate response codes that are instrumented
     */
    protected AbstractInstrumentedHandler(MetricRegistry registry, String prefix, ResponseMeteredLevel responseMeteredLevel) {
        this.responseMeteredLevel = responseMeteredLevel;
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

        final String prefix = getMetricPrefix();

        this.requests = metricRegistry.timer(name(prefix, NAME_REQUESTS));
        this.dispatches = metricRegistry.timer(name(prefix, NAME_DISPATCHES));

        this.activeRequests = metricRegistry.counter(name(prefix, NAME_ACTIVE_REQUESTS));
        this.activeDispatches = metricRegistry.counter(name(prefix, NAME_ACTIVE_DISPATCHES));
        this.activeSuspended = metricRegistry.counter(name(prefix, NAME_ACTIVE_SUSPENDED));

        this.asyncDispatches = metricRegistry.meter(name(prefix, NAME_ASYNC_DISPATCHES));
        this.asyncTimeouts = metricRegistry.meter(name(prefix, NAME_ASYNC_TIMEOUTS));

        this.responseCodeMeters = DETAILED_METER_LEVELS.contains(responseMeteredLevel) ? new ConcurrentHashMap<>() : Collections.emptyMap();

        this.getRequests = metricRegistry.timer(name(prefix, NAME_GET_REQUESTS));
        this.postRequests = metricRegistry.timer(name(prefix, NAME_POST_REQUESTS));
        this.headRequests = metricRegistry.timer(name(prefix, NAME_HEAD_REQUESTS));
        this.putRequests = metricRegistry.timer(name(prefix, NAME_PUT_REQUESTS));
        this.deleteRequests = metricRegistry.timer(name(prefix, NAME_DELETE_REQUESTS));
        this.optionsRequests = metricRegistry.timer(name(prefix, NAME_OPTIONS_REQUESTS));
        this.traceRequests = metricRegistry.timer(name(prefix, NAME_TRACE_REQUESTS));
        this.connectRequests = metricRegistry.timer(name(prefix, NAME_CONNECT_REQUESTS));
        this.moveRequests = metricRegistry.timer(name(prefix, NAME_MOVE_REQUESTS));
        this.otherRequests = metricRegistry.timer(name(prefix, NAME_OTHER_REQUESTS));

        if (COARSE_METER_LEVELS.contains(responseMeteredLevel)) {
            this.responses = Collections.unmodifiableList(Arrays.asList(
                            metricRegistry.meter(name(prefix, NAME_1XX_RESPONSES)), // 1xx
                            metricRegistry.meter(name(prefix, NAME_2XX_RESPONSES)), // 2xx
                            metricRegistry.meter(name(prefix, NAME_3XX_RESPONSES)), // 3xx
                            metricRegistry.meter(name(prefix, NAME_4XX_RESPONSES)), // 4xx
                            metricRegistry.meter(name(prefix, NAME_5XX_RESPONSES))  // 5xx
                    ));

            metricRegistry.register(name(prefix, NAME_PERCENT_4XX_1M), new RatioGauge() {
                @Override
                protected Ratio getRatio() {
                    return Ratio.of(responses.get(3).getOneMinuteRate(),
                            requests.getOneMinuteRate());
                }
            });

            metricRegistry.register(name(prefix, NAME_PERCENT_4XX_5M), new RatioGauge() {
                @Override
                protected Ratio getRatio() {
                    return Ratio.of(responses.get(3).getFiveMinuteRate(),
                            requests.getFiveMinuteRate());
                }
            });

            metricRegistry.register(name(prefix, NAME_PERCENT_4XX_15M), new RatioGauge() {
                @Override
                protected Ratio getRatio() {
                    return Ratio.of(responses.get(3).getFifteenMinuteRate(),
                            requests.getFifteenMinuteRate());
                }
            });

            metricRegistry.register(name(prefix, NAME_PERCENT_5XX_1M), new RatioGauge() {
                @Override
                protected Ratio getRatio() {
                    return Ratio.of(responses.get(4).getOneMinuteRate(),
                            requests.getOneMinuteRate());
                }
            });

            metricRegistry.register(name(prefix, NAME_PERCENT_5XX_5M), new RatioGauge() {
                @Override
                protected Ratio getRatio() {
                    return Ratio.of(responses.get(4).getFiveMinuteRate(),
                            requests.getFiveMinuteRate());
                }
            });

            metricRegistry.register(name(prefix, NAME_PERCENT_5XX_15M), new RatioGauge() {
                @Override
                public Ratio getRatio() {
                    return Ratio.of(responses.get(4).getFifteenMinuteRate(),
                            requests.getFifteenMinuteRate());
                }
            });
        } else {
             this.responses = Collections.emptyList();
        }
    }

    @Override
    protected void doStop() throws Exception {
        final String prefix = getMetricPrefix();

        metricRegistry.remove(name(prefix, NAME_REQUESTS));
        metricRegistry.remove(name(prefix, NAME_DISPATCHES));
        metricRegistry.remove(name(prefix, NAME_ACTIVE_REQUESTS));
        metricRegistry.remove(name(prefix, NAME_ACTIVE_DISPATCHES));
        metricRegistry.remove(name(prefix, NAME_ACTIVE_SUSPENDED));
        metricRegistry.remove(name(prefix, NAME_ASYNC_DISPATCHES));
        metricRegistry.remove(name(prefix, NAME_ASYNC_TIMEOUTS));
        metricRegistry.remove(name(prefix, NAME_1XX_RESPONSES));
        metricRegistry.remove(name(prefix, NAME_2XX_RESPONSES));
        metricRegistry.remove(name(prefix, NAME_3XX_RESPONSES));
        metricRegistry.remove(name(prefix, NAME_4XX_RESPONSES));
        metricRegistry.remove(name(prefix, NAME_5XX_RESPONSES));
        metricRegistry.remove(name(prefix, NAME_GET_REQUESTS));
        metricRegistry.remove(name(prefix, NAME_POST_REQUESTS));
        metricRegistry.remove(name(prefix, NAME_HEAD_REQUESTS));
        metricRegistry.remove(name(prefix, NAME_PUT_REQUESTS));
        metricRegistry.remove(name(prefix, NAME_DELETE_REQUESTS));
        metricRegistry.remove(name(prefix, NAME_OPTIONS_REQUESTS));
        metricRegistry.remove(name(prefix, NAME_TRACE_REQUESTS));
        metricRegistry.remove(name(prefix, NAME_CONNECT_REQUESTS));
        metricRegistry.remove(name(prefix, NAME_MOVE_REQUESTS));
        metricRegistry.remove(name(prefix, NAME_OTHER_REQUESTS));
        metricRegistry.remove(name(prefix, NAME_PERCENT_4XX_1M));
        metricRegistry.remove(name(prefix, NAME_PERCENT_4XX_5M));
        metricRegistry.remove(name(prefix, NAME_PERCENT_4XX_15M));
        metricRegistry.remove(name(prefix, NAME_PERCENT_5XX_1M));
        metricRegistry.remove(name(prefix, NAME_PERCENT_5XX_5M));
        metricRegistry.remove(name(prefix, NAME_PERCENT_5XX_15M));

        if (responseCodeMeters != null) {
            responseCodeMeters.keySet().stream()
                    .map(sc -> name(getMetricPrefix(), String.format("%d-responses", sc)))
                    .forEach(metricRegistry::remove);
        }
        super.doStop();
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        activeDispatches.inc();
        activeRequests.inc();
        final long start = Request.getTimeStamp(request);

        final AtomicBoolean suspended = new AtomicBoolean(false);

        final Runnable metricUpdater = () -> {
            updateResponses(request, response, start, true);
            if (suspended.get()) {
                activeSuspended.dec();
            }
        };

        final Callback metricUpdaterCallback = Callback.from(callback, metricUpdater);
        boolean handled = false;

        setupServletListeners(request, response);

        try {
            handled = super.handle(request, response, metricUpdaterCallback);
        } finally {
            final long now = System.currentTimeMillis();
            final long dispatched = now - start;

            activeDispatches.dec();
            dispatches.update(dispatched, TimeUnit.MILLISECONDS);

            if (isSuspended(request, response) && suspended.compareAndSet(false, true)) {
                activeSuspended.inc();
            }

            if (!handled) {
                updateResponses(request, response, start, false);
            }
        }

        return handled;
    }

    protected Timer requestTimer(String method) {
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

    protected void updateResponses(Request request, Response response, long start, boolean isHandled) {
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

    protected void mark(int statusCode) {
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

    protected Meter getResponseCodeMeter(int statusCode) {
        return responseCodeMeters
                .computeIfAbsent(statusCode, sc -> metricRegistry
                        .meter(name(getMetricPrefix(), String.format("%d-responses", sc))));
    }

    protected String getMetricPrefix() {
        return this.prefix == null ? name(getHandler().getClass(), name) : name(this.prefix, name);
    }

    protected abstract void setupServletListeners(Request request, Response response);

    protected final Meter getAsyncDispatches() {
        return asyncDispatches;
    }

    protected final Meter getAsyncTimeouts() {
        return asyncTimeouts;
    }

    protected abstract boolean isSuspended(Request request, Response response);
}
