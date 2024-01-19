package io.dropwizard.metrics.jetty12;

import io.dropwizard.metrics5.Counter;
import io.dropwizard.metrics5.Meter;
import io.dropwizard.metrics5.MetricName;
import io.dropwizard.metrics5.MetricRegistry;
import io.dropwizard.metrics5.RatioGauge;
import io.dropwizard.metrics5.Timer;
import io.dropwizard.metrics5.annotation.ResponseMeteredLevel;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;

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

        final MetricName prefix = getMetricPrefix();

        this.requests = metricRegistry.timer(prefix.resolve(NAME_REQUESTS));
        this.dispatches = metricRegistry.timer(prefix.resolve(NAME_DISPATCHES));

        this.activeRequests = metricRegistry.counter(prefix.resolve(NAME_ACTIVE_REQUESTS));
        this.activeDispatches = metricRegistry.counter(prefix.resolve(NAME_ACTIVE_DISPATCHES));
        this.activeSuspended = metricRegistry.counter(prefix.resolve(NAME_ACTIVE_SUSPENDED));

        this.asyncDispatches = metricRegistry.meter(prefix.resolve(NAME_ASYNC_DISPATCHES));
        this.asyncTimeouts = metricRegistry.meter(prefix.resolve(NAME_ASYNC_TIMEOUTS));

        this.responseCodeMeters = DETAILED_METER_LEVELS.contains(responseMeteredLevel) ? new ConcurrentHashMap<>() : Collections.emptyMap();

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

        if (COARSE_METER_LEVELS.contains(responseMeteredLevel)) {
            this.responses = Collections.unmodifiableList(Arrays.asList(
                            metricRegistry.meter(prefix.resolve(NAME_1XX_RESPONSES)), // 1xx
                            metricRegistry.meter(prefix.resolve(NAME_2XX_RESPONSES)), // 2xx
                            metricRegistry.meter(prefix.resolve(NAME_3XX_RESPONSES)), // 3xx
                            metricRegistry.meter(prefix.resolve(NAME_4XX_RESPONSES)), // 4xx
                            metricRegistry.meter(prefix.resolve(NAME_5XX_RESPONSES))  // 5xx
                    ));

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
        } else {
             this.responses = Collections.emptyList();
        }
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
                        .meter(getMetricPrefix().resolve(String.format("%d-responses", sc))));
    }

    protected MetricName getMetricPrefix() {
        return this.prefix == null ? name(getHandler().getClass(), name) : name(this.prefix, name);
    }
}
