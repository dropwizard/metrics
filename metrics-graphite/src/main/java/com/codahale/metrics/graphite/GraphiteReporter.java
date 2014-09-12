package com.codahale.metrics.graphite;

import com.codahale.metrics.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

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
        private MetricValueFilter valueFilter;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.clock = Clock.defaultClock();
            this.prefix = null;
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
            this.valueFilter = MetricValueFilter.ALL;
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
         * Only report values which match the given filter.
         * See {@link Value}, {@link ValueFilter}
         *
         * @param valueFilter a {@link MetricValueFilter}
         * @return {@code this}
         */
        public Builder valueFilter(MetricValueFilter valueFilter) {
            this.valueFilter = valueFilter;
            return this;
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
                                        valueFilter);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphiteReporter.class);

    private final GraphiteSender graphite;
    private final Clock clock;
    private final String prefix;
    private final MetricValueFilter valueFilter;

    private GraphiteReporter(MetricRegistry registry,
                             GraphiteSender graphite,
                             Clock clock,
                             String prefix,
                             TimeUnit rateUnit,
                             TimeUnit durationUnit,
                             MetricFilter filter,
                             MetricValueFilter valueFilter) {
        super(registry, "graphite-reporter", filter, rateUnit, durationUnit);
        this.graphite = graphite;
        this.clock = clock;
        this.prefix = prefix;
        this.valueFilter = valueFilter;
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {
        final long timestamp = clock.getTime() / 1000;

        // oh it'd be lovely to use Java 7 here
        try {
            if (!graphite.isConnected()) {
    	          graphite.connect();
            }

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
            try {
                graphite.close();
            } catch (IOException e1) {
                LOGGER.warn("Error closing Graphite", graphite, e);
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

        if (valueFilter.isReportingOn(Value.TIMER_MAX))
            graphite.send(prefix(name, "max"), format(convertDuration(snapshot.getMax())), timestamp);
        if (valueFilter.isReportingOn(Value.TIMER_MEAN))
            graphite.send(prefix(name, "mean"), format(convertDuration(snapshot.getMean())), timestamp);
        if (valueFilter.isReportingOn(Value.TIMER_MIN))
            graphite.send(prefix(name, "min"), format(convertDuration(snapshot.getMin())), timestamp);
        if (valueFilter.isReportingOn(Value.TIMER_STDDEV))
            graphite.send(prefix(name, "stddev"),format(convertDuration(snapshot.getStdDev())),timestamp);
        if (valueFilter.isReportingOn(Value.TIMER_P50))
            graphite.send(prefix(name, "p50"),format(convertDuration(snapshot.getMedian())),timestamp);
        if (valueFilter.isReportingOn(Value.TIMER_P75))
            graphite.send(prefix(name, "p75"),format(convertDuration(snapshot.get75thPercentile())),timestamp);
        if (valueFilter.isReportingOn(Value.TIMER_P95))
            graphite.send(prefix(name, "p95"),format(convertDuration(snapshot.get95thPercentile())),timestamp);
        if (valueFilter.isReportingOn(Value.TIMER_P98))
            graphite.send(prefix(name, "p98"),format(convertDuration(snapshot.get98thPercentile())),timestamp);
        if (valueFilter.isReportingOn(Value.TIMER_P99))
            graphite.send(prefix(name, "p99"),format(convertDuration(snapshot.get99thPercentile())),timestamp);
        if (valueFilter.isReportingOn(Value.TIMER_P999))
            graphite.send(prefix(name, "p999"),format(convertDuration(snapshot.get999thPercentile())),timestamp);
        if (valueFilter.isReportingOn(Value.TIMER_COUNT))
            graphite.send(prefix(name, "count"), format(timer.getCount()), timestamp);
        if (valueFilter.isReportingOn(Value.TIMER_M1_RATE))
            graphite.send(prefix(name, "m1_rate"),format(convertRate(timer.getOneMinuteRate())),timestamp);
        if (valueFilter.isReportingOn(Value.TIMER_M5_RATE))
            graphite.send(prefix(name, "m5_rate"),format(convertRate(timer.getFiveMinuteRate())),timestamp);
        if (valueFilter.isReportingOn(Value.TIMER_M15_RATE))
            graphite.send(prefix(name, "m15_rate"),format(convertRate(timer.getFifteenMinuteRate())),timestamp);
        if (valueFilter.isReportingOn(Value.TIMER_MEAN_RATE))
            graphite.send(prefix(name, "mean_rate"),format(convertRate(timer.getMeanRate())),timestamp);
    }

    private void reportMetered(String name, Metered meter, long timestamp) throws IOException {
        if (valueFilter.isReportingOn(Value.METER_COUNT))
            graphite.send(prefix(name, "count"), format(meter.getCount()), timestamp);
        if (valueFilter.isReportingOn(Value.METER_M1_RATE))
            graphite.send(prefix(name, "m1_rate"),format(convertRate(meter.getOneMinuteRate())),timestamp);
        if (valueFilter.isReportingOn(Value.METER_M5_RATE))
            graphite.send(prefix(name, "m5_rate"),format(convertRate(meter.getFiveMinuteRate())),timestamp);
        if (valueFilter.isReportingOn(Value.METER_M15_RATE))
            graphite.send(prefix(name, "m15_rate"),format(convertRate(meter.getFifteenMinuteRate())),timestamp);
        if (valueFilter.isReportingOn(Value.METER_MEAN_RATE))
            graphite.send(prefix(name, "mean_rate"),format(convertRate(meter.getMeanRate())),timestamp);
    }

    private void reportHistogram(String name, Histogram histogram, long timestamp) throws IOException {
        final Snapshot snapshot = histogram.getSnapshot();
        if (valueFilter.isReportingOn(Value.HISTOGRAM_COUNT))
            graphite.send(prefix(name, "count"), format(histogram.getCount()), timestamp);
        if (valueFilter.isReportingOn(Value.HISTOGRAM_MAX))
            graphite.send(prefix(name, "max"), format(snapshot.getMax()), timestamp);
        if (valueFilter.isReportingOn(Value.HISTOGRAM_MEAN))
            graphite.send(prefix(name, "mean"), format(snapshot.getMean()), timestamp);
        if (valueFilter.isReportingOn(Value.HISTOGRAM_MIN))
            graphite.send(prefix(name, "min"), format(snapshot.getMin()), timestamp);
        if (valueFilter.isReportingOn(Value.HISTOGRAM_STDDEV))
            graphite.send(prefix(name, "stddev"), format(snapshot.getStdDev()), timestamp);
        if (valueFilter.isReportingOn(Value.HISTOGRAM_P50))
            graphite.send(prefix(name, "p50"), format(snapshot.getMedian()), timestamp);
        if (valueFilter.isReportingOn(Value.HISTOGRAM_P75))
            graphite.send(prefix(name, "p75"), format(snapshot.get75thPercentile()), timestamp);
        if (valueFilter.isReportingOn(Value.HISTOGRAM_P95))
            graphite.send(prefix(name, "p95"), format(snapshot.get95thPercentile()), timestamp);
        if (valueFilter.isReportingOn(Value.HISTOGRAM_P98))
            graphite.send(prefix(name, "p98"), format(snapshot.get98thPercentile()), timestamp);
        if (valueFilter.isReportingOn(Value.HISTOGRAM_P99))
            graphite.send(prefix(name, "p99"), format(snapshot.get99thPercentile()), timestamp);
        if (valueFilter.isReportingOn(Value.HISTOGRAM_P999))
            graphite.send(prefix(name, "p999"), format(snapshot.get999thPercentile()), timestamp);
    }

    private void reportCounter(String name, Counter counter, long timestamp) throws IOException {
        graphite.send(prefix(name, "count"), format(counter.getCount()), timestamp);
    }

    private void reportGauge(String name, Gauge gauge, long timestamp) throws IOException {
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
        }
        return null;
    }

    private String prefix(String... components) {
        return MetricRegistry.name(prefix, components);
    }

    private String format(long n) {
        return Long.toString(n);
    }

    private String format(double v) {
        // the Carbon plaintext format is pretty underspecified, but it seems like it just wants
        // US-formatted digits
        return String.format(Locale.US, "%2.2f", v);
    }
}
