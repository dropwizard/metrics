package com.codahale.metrics.graphite;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metered;
import com.codahale.metrics.MetricAttribute;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.DoubleFunction;

import static com.codahale.metrics.MetricAttribute.COUNT;
import static com.codahale.metrics.MetricAttribute.M15_RATE;
import static com.codahale.metrics.MetricAttribute.M1_RATE;
import static com.codahale.metrics.MetricAttribute.M5_RATE;
import static com.codahale.metrics.MetricAttribute.MAX;
import static com.codahale.metrics.MetricAttribute.MEAN;
import static com.codahale.metrics.MetricAttribute.MEAN_RATE;
import static com.codahale.metrics.MetricAttribute.MIN;
import static com.codahale.metrics.MetricAttribute.P50;
import static com.codahale.metrics.MetricAttribute.P75;
import static com.codahale.metrics.MetricAttribute.P95;
import static com.codahale.metrics.MetricAttribute.P98;
import static com.codahale.metrics.MetricAttribute.P99;
import static com.codahale.metrics.MetricAttribute.P999;
import static com.codahale.metrics.MetricAttribute.STDDEV;

/**
 * A reporter which publishes metric values to a Graphite server.
 *
 * @see <a href="http://graphite.wikidot.com/">Graphite - Scalable Realtime Graphing</a>
 */
public class GraphiteReporter extends ScheduledReporter {
    /**
     * Returns a new {@link Builder} for {@link GraphiteReporter}.
     *
     * @param registry the registry to report
     * @return a {@link Builder} instance for a {@link GraphiteReporter}
     */
    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }

    /**
     * A builder for {@link GraphiteReporter} instances. Defaults to not using a prefix, using the
     * default clock, converting rates to events/second, converting durations to milliseconds, and
     * not filtering metrics.
     */
    public static class Builder {
        private final MetricRegistry registry;
        private Clock clock;
        private String prefix;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;
        private ScheduledExecutorService executor;
        private boolean shutdownExecutorOnStop;
        private Set<MetricAttribute> disabledMetricAttributes;
        private boolean addMetricAttributesAsTags;
        private DoubleFunction<String> floatingPointFormatter;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.clock = Clock.defaultClock();
            this.prefix = null;
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
            this.executor = null;
            this.shutdownExecutorOnStop = true;
            this.disabledMetricAttributes = Collections.emptySet();
            this.addMetricAttributesAsTags = false;
            this.floatingPointFormatter = DEFAULT_FP_FORMATTER;
        }

        /**
         * Specifies whether or not, the executor (used for reporting) will be stopped with same time with reporter.
         * Default value is true.
         * Setting this parameter to false, has the sense in combining with providing external managed executor via {@link #scheduleOn(ScheduledExecutorService)}.
         *
         * @param shutdownExecutorOnStop if true, then executor will be stopped in same time with this reporter
         * @return {@code this}
         */
        public Builder shutdownExecutorOnStop(boolean shutdownExecutorOnStop) {
            this.shutdownExecutorOnStop = shutdownExecutorOnStop;
            return this;
        }

        /**
         * Specifies the executor to use while scheduling reporting of metrics.
         * Default value is null.
         * Null value leads to executor will be auto created on start.
         *
         * @param executor the executor to use while scheduling reporting of metrics.
         * @return {@code this}
         */
        public Builder scheduleOn(ScheduledExecutorService executor) {
            this.executor = executor;
            return this;
        }

        /**
         * Use the given {@link Clock} instance for the time.
         *
         * @param clock a {@link Clock} instance
         * @return {@code this}
         */
        public Builder withClock(Clock clock) {
            this.clock = clock;
            return this;
        }

        /**
         * Prefix all metric names with the given string.
         *
         * @param prefix the prefix for all metric names
         * @return {@code this}
         */
        public Builder prefixedWith(String prefix) {
            this.prefix = prefix;
            return this;
        }

        /**
         * Convert rates to the given time unit.
         *
         * @param rateUnit a unit of time
         * @return {@code this}
         */
        public Builder convertRatesTo(TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        /**
         * Convert durations to the given time unit.
         *
         * @param durationUnit a unit of time
         * @return {@code this}
         */
        public Builder convertDurationsTo(TimeUnit durationUnit) {
            this.durationUnit = durationUnit;
            return this;
        }

        /**
         * Only report metrics which match the given filter.
         *
         * @param filter a {@link MetricFilter}
         * @return {@code this}
         */
        public Builder filter(MetricFilter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Don't report the passed metric attributes for all metrics (e.g. "p999", "stddev" or "m15").
         * See {@link MetricAttribute}.
         *
         * @param disabledMetricAttributes a set of {@link MetricAttribute}
         * @return {@code this}
         */
        public Builder disabledMetricAttributes(Set<MetricAttribute> disabledMetricAttributes) {
            this.disabledMetricAttributes = disabledMetricAttributes;
            return this;
        }


        /**
         * Specifies whether or not metric attributes (e.g. "p999", "stddev" or "m15") should be reported in the traditional dot delimited format or in the tag based format.
         * Without tags (default): `my.metric.p99`
         * With tags: `my.metric;metricattribute=p99`
         *
         * Note that this setting only modifies the metric attribute, and will not convert any other portion of the metric name to use tags.
         * For mor information on Graphite tag support see https://graphite.readthedocs.io/en/latest/tags.html
         * See {@link MetricAttribute}.
         *
         * @param addMetricAttributesAsTags if true, then metric attributes will be added as tags
         * @return {@code this}
         */
        public Builder addMetricAttributesAsTags(boolean addMetricAttributesAsTags) {
            this.addMetricAttributesAsTags = addMetricAttributesAsTags;
            return this;
        }

        /**
         * Use custom floating point formatter.
         *
         * @param floatingPointFormatter a custom formatter for floating point values
         * @return {@code this}
         */
        public Builder withFloatingPointFormatter(DoubleFunction<String> floatingPointFormatter) {
            this.floatingPointFormatter = floatingPointFormatter;
            return this;
        }

        /**
         * Builds a {@link GraphiteReporter} with the given properties, sending metrics using the
         * given {@link GraphiteSender}.
         * <p>
         * Present for binary compatibility
         *
         * @param graphite a {@link Graphite}
         * @return a {@link GraphiteReporter}
         */
        public GraphiteReporter build(Graphite graphite) {
            return build((GraphiteSender) graphite);
        }

        /**
         * Builds a {@link GraphiteReporter} with the given properties, sending metrics using the
         * given {@link GraphiteSender}.
         *
         * @param graphite a {@link GraphiteSender}
         * @return a {@link GraphiteReporter}
         */
        public GraphiteReporter build(GraphiteSender graphite) {
            return new GraphiteReporter(registry,
                    graphite,
                    clock,
                    prefix,
                    rateUnit,
                    durationUnit,
                    filter,
                    executor,
                    shutdownExecutorOnStop,
                    disabledMetricAttributes,
                    addMetricAttributesAsTags,
                    floatingPointFormatter);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphiteReporter.class);
    // the Carbon plaintext format is pretty underspecified, but it seems like it just wants US-formatted digits
    private static final DoubleFunction<String> DEFAULT_FP_FORMATTER = fp -> String.format(Locale.US, "%2.2f", fp);

    private final GraphiteSender graphite;
    private final Clock clock;
    private final String prefix;
    private final boolean addMetricAttributesAsTags;
    private final DoubleFunction<String> floatingPointFormatter;
  
  
    /**
     * Creates a new {@link GraphiteReporter} instance.
     *
     * @param registry                  the {@link MetricRegistry} containing the metrics this
     *                                  reporter will report
     * @param graphite                  the {@link GraphiteSender} which is responsible for sending metrics to a Carbon server
     *                                  via a transport protocol
     * @param clock                     the instance of the time. Use {@link Clock#defaultClock()} for the default
     * @param prefix                    the prefix of all metric names (may be null)
     * @param rateUnit                  the time unit of in which rates will be converted
     * @param durationUnit              the time unit of in which durations will be converted
     * @param filter                    the filter for which metrics to report
     * @param executor                  the executor to use while scheduling reporting of metrics (may be null).
     * @param shutdownExecutorOnStop    if true, then executor will be stopped in same time with this reporter
     * @param disabledMetricAttributes  do not report specific metric attributes
     */
    protected GraphiteReporter(MetricRegistry registry,
                               GraphiteSender graphite,
                               Clock clock,
                               String prefix,
                               TimeUnit rateUnit,
                               TimeUnit durationUnit,
                               MetricFilter filter,
                               ScheduledExecutorService executor,
                               boolean shutdownExecutorOnStop,
                               Set<MetricAttribute> disabledMetricAttributes) {
        this(registry, graphite, clock, prefix, rateUnit, durationUnit, filter, executor, shutdownExecutorOnStop,
                disabledMetricAttributes, false);
    }


    /**
     * Creates a new {@link GraphiteReporter} instance.
     *
     * @param registry                  the {@link MetricRegistry} containing the metrics this
     *                                  reporter will report
     * @param graphite                  the {@link GraphiteSender} which is responsible for sending metrics to a Carbon server
     *                                  via a transport protocol
     * @param clock                     the instance of the time. Use {@link Clock#defaultClock()} for the default
     * @param prefix                    the prefix of all metric names (may be null)
     * @param rateUnit                  the time unit of in which rates will be converted
     * @param durationUnit              the time unit of in which durations will be converted
     * @param filter                    the filter for which metrics to report
     * @param executor                  the executor to use while scheduling reporting of metrics (may be null).
     * @param shutdownExecutorOnStop    if true, then executor will be stopped in same time with this reporter
     * @param disabledMetricAttributes  do not report specific metric attributes
     * @param addMetricAttributesAsTags if true, then add metric attributes as tags instead of suffixes
     */
    protected GraphiteReporter(MetricRegistry registry,
                               GraphiteSender graphite,
                               Clock clock,
                               String prefix,
                               TimeUnit rateUnit,
                               TimeUnit durationUnit,
                               MetricFilter filter,
                               ScheduledExecutorService executor,
                               boolean shutdownExecutorOnStop,
                               Set<MetricAttribute> disabledMetricAttributes,
                               boolean addMetricAttributesAsTags) {
        this(registry, graphite, clock, prefix, rateUnit, durationUnit, filter, executor, shutdownExecutorOnStop,
                disabledMetricAttributes, addMetricAttributesAsTags, DEFAULT_FP_FORMATTER);
    }

    /**
     * Creates a new {@link GraphiteReporter} instance.
     *
     * @param registry                  the {@link MetricRegistry} containing the metrics this
     *                                  reporter will report
     * @param graphite                  the {@link GraphiteSender} which is responsible for sending metrics to a Carbon server
     *                                  via a transport protocol
     * @param clock                     the instance of the time. Use {@link Clock#defaultClock()} for the default
     * @param prefix                    the prefix of all metric names (may be null)
     * @param rateUnit                  the time unit of in which rates will be converted
     * @param durationUnit              the time unit of in which durations will be converted
     * @param filter                    the filter for which metrics to report
     * @param executor                  the executor to use while scheduling reporting of metrics (may be null).
     * @param shutdownExecutorOnStop    if true, then executor will be stopped in same time with this reporter
     * @param disabledMetricAttributes  do not report specific metric attributes
     * @param addMetricAttributesAsTags if true, then add metric attributes as tags instead of suffixes
     * @param floatingPointFormatter    custom floating point formatter
     */
    protected GraphiteReporter(MetricRegistry registry,
                               GraphiteSender graphite,
                               Clock clock,
                               String prefix,
                               TimeUnit rateUnit,
                               TimeUnit durationUnit,
                               MetricFilter filter,
                               ScheduledExecutorService executor,
                               boolean shutdownExecutorOnStop,
                               Set<MetricAttribute> disabledMetricAttributes,
                               boolean addMetricAttributesAsTags,
                               DoubleFunction<String> floatingPointFormatter) {
        super(registry, "graphite-reporter", filter, rateUnit, durationUnit, executor, shutdownExecutorOnStop,
                disabledMetricAttributes);
        this.graphite = graphite;
        this.clock = clock;
        this.prefix = prefix;
        this.addMetricAttributesAsTags = addMetricAttributesAsTags;
        this.floatingPointFormatter = floatingPointFormatter;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {
        final long timestamp = clock.getTime() / 1000;

        // oh it'd be lovely to use Java 7 here
        try {
            graphite.connect();

            for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
                reportGauge(entry.getKey(), entry.getValue(), timestamp);
            }

            for (Map.Entry<String, Counter> entry : counters.entrySet()) {
                reportCounter(entry.getKey(), entry.getValue(), timestamp);
            }

            for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
                reportHistogram(entry.getKey(), entry.getValue(), timestamp);
            }

            for (Map.Entry<String, Meter> entry : meters.entrySet()) {
                reportMetered(entry.getKey(), entry.getValue(), timestamp);
            }

            for (Map.Entry<String, Timer> entry : timers.entrySet()) {
                reportTimer(entry.getKey(), entry.getValue(), timestamp);
            }
            graphite.flush();
        } catch (IOException e) {
            LOGGER.warn("Unable to report to Graphite", graphite, e);
        } finally {
            try {
                graphite.close();
            } catch (IOException e1) {
                LOGGER.warn("Error closing Graphite", graphite, e1);
            }
        }
    }

    @Override
    public void stop() {
        try {
            super.stop();
        } finally {
            try {
                graphite.close();
            } catch (IOException e) {
                LOGGER.debug("Error disconnecting from Graphite", graphite, e);
            }
        }
    }

    private void reportTimer(String name, Timer timer, long timestamp) throws IOException {
        final Snapshot snapshot = timer.getSnapshot();
        sendIfEnabled(MAX, name, convertDuration(snapshot.getMax()), timestamp);
        sendIfEnabled(MEAN, name, convertDuration(snapshot.getMean()), timestamp);
        sendIfEnabled(MIN, name, convertDuration(snapshot.getMin()), timestamp);
        sendIfEnabled(STDDEV, name, convertDuration(snapshot.getStdDev()), timestamp);
        sendIfEnabled(P50, name, convertDuration(snapshot.getMedian()), timestamp);
        sendIfEnabled(P75, name, convertDuration(snapshot.get75thPercentile()), timestamp);
        sendIfEnabled(P95, name, convertDuration(snapshot.get95thPercentile()), timestamp);
        sendIfEnabled(P98, name, convertDuration(snapshot.get98thPercentile()), timestamp);
        sendIfEnabled(P99, name, convertDuration(snapshot.get99thPercentile()), timestamp);
        sendIfEnabled(P999, name, convertDuration(snapshot.get999thPercentile()), timestamp);
        reportMetered(name, timer, timestamp);
    }

    private void reportMetered(String name, Metered meter, long timestamp) throws IOException {
        sendIfEnabled(COUNT, name, meter.getCount(), timestamp);
        sendIfEnabled(M1_RATE, name, convertRate(meter.getOneMinuteRate()), timestamp);
        sendIfEnabled(M5_RATE, name, convertRate(meter.getFiveMinuteRate()), timestamp);
        sendIfEnabled(M15_RATE, name, convertRate(meter.getFifteenMinuteRate()), timestamp);
        sendIfEnabled(MEAN_RATE, name, convertRate(meter.getMeanRate()), timestamp);
    }

    private void reportHistogram(String name, Histogram histogram, long timestamp) throws IOException {
        final Snapshot snapshot = histogram.getSnapshot();
        sendIfEnabled(COUNT, name, histogram.getCount(), timestamp);
        sendIfEnabled(MAX, name, snapshot.getMax(), timestamp);
        sendIfEnabled(MEAN, name, snapshot.getMean(), timestamp);
        sendIfEnabled(MIN, name, snapshot.getMin(), timestamp);
        sendIfEnabled(STDDEV, name, snapshot.getStdDev(), timestamp);
        sendIfEnabled(P50, name, snapshot.getMedian(), timestamp);
        sendIfEnabled(P75, name, snapshot.get75thPercentile(), timestamp);
        sendIfEnabled(P95, name, snapshot.get95thPercentile(), timestamp);
        sendIfEnabled(P98, name, snapshot.get98thPercentile(), timestamp);
        sendIfEnabled(P99, name, snapshot.get99thPercentile(), timestamp);
        sendIfEnabled(P999, name, snapshot.get999thPercentile(), timestamp);
    }

    private void sendIfEnabled(MetricAttribute type, String name, double value, long timestamp) throws IOException {
        if (getDisabledMetricAttributes().contains(type)) {
            return;
        }
        graphite.send(prefix(appendMetricAttribute(name, type.getCode())), format(value), timestamp);
    }

    private void sendIfEnabled(MetricAttribute type, String name, long value, long timestamp) throws IOException {
        if (getDisabledMetricAttributes().contains(type)) {
            return;
        }
        graphite.send(prefix(appendMetricAttribute(name, type.getCode())), format(value), timestamp);
    }

    private void reportCounter(String name, Counter counter, long timestamp) throws IOException {
        graphite.send(prefix(appendMetricAttribute(name, COUNT.getCode())), format(counter.getCount()), timestamp);
    }

    private void reportGauge(String name, Gauge<?> gauge, long timestamp) throws IOException {
        final String value = format(gauge.getValue());
        if (value != null) {
            graphite.send(prefix(name), value, timestamp);
        }
    }

    private String format(Object o) {
        if (o instanceof Float) {
            return format(((Float) o).doubleValue());
        } else if (o instanceof Double) {
            return format(((Double) o).doubleValue());
        } else if (o instanceof Byte) {
            return format(((Byte) o).longValue());
        } else if (o instanceof Short) {
            return format(((Short) o).longValue());
        } else if (o instanceof Integer) {
            return format(((Integer) o).longValue());
        } else if (o instanceof Long) {
            return format(((Long) o).longValue());
        } else if (o instanceof Number) {
            return format(((Number) o).doubleValue());
        } else if (o instanceof Boolean) {
            return format(((Boolean) o) ? 1 : 0);
        }
        return null;
    }

    private String prefix(String name) {
        return MetricRegistry.name(prefix, name);
    }

    private String appendMetricAttribute(String name, String metricAttribute){
        if (addMetricAttributesAsTags){
            return name + ";metricattribute=" + metricAttribute;
        }
        return name + "." + metricAttribute;
    }

    private String format(long n) {
        return Long.toString(n);
    }

    protected String format(double v) {
        return floatingPointFormatter.apply(v);
    }
}
