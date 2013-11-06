package com.codahale.metrics.graphite;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metered;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

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
        private MetricRegistry reportingRegistry;
        private Clock clock;
        private String prefix;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;
        private String threadName;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.reportingRegistry = registry;
            this.clock = Clock.defaultClock();
            this.prefix = null;
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
            this.threadName = "";
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

        public Builder namedThread(String threadName){
            this.threadName = threadName;
            return this;
        }

        public Builder reportingRegistry(MetricRegistry registry){
            this.reportingRegistry = registry;
            return this;
        }

        /**
         * Builds a {@link GraphiteReporter} with the given properties, sending metrics using the
         * given {@link Graphite} client.
         *
         * @param graphite a {@link Graphite} client
         * @return a {@link GraphiteReporter}
         */
        public GraphiteReporter build(Graphite graphite) {
            return new GraphiteReporter(registry,
                                        reportingRegistry,
                                        graphite,
                                        clock,
                                        prefix,
                                        rateUnit,
                                        durationUnit,
                                        filter,
                                        threadName);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphiteReporter.class);

    private final Graphite graphite;
    private final Clock clock;
    private final String prefix;
    private final MetricRegistry reportingRegistry;

    private final AtomicLong connectDurationMS = new AtomicLong(0);
    private final AtomicLong publishDurationMS = new AtomicLong(0);
    private final AtomicLong payloadSize = new AtomicLong(0);

    private GraphiteReporter(MetricRegistry registry,
                             MetricRegistry reportingRegistry,
                             Graphite graphite,
                             Clock clock,
                             String prefix,
                             TimeUnit rateUnit,
                             TimeUnit durationUnit,
                             MetricFilter filter,
                             String threadName) {
        super(registry, threadName(threadName), filter, rateUnit, durationUnit);
        this.graphite = graphite;
        this.clock = clock;
        this.prefix = prefix;
        this.reportingRegistry = reportingRegistry;

        configurePublishingMetrics(reportingRegistry);
    }

    /**
     * The publisher tracks connection time, publish time, and payload size.  These metrics lag real
     * time by one publishing period.
     *
     * Meaning, at time T, this records how long it took to connect and publish the data that report's
     * size.  At T+1, the values from T are sent with T+1's report.
     */
    private void configurePublishingMetrics(MetricRegistry registry){
        final Gauge<Long> connectionGauge = new Gauge<Long>() {
            @Override
            public Long getValue() {
                return connectDurationMS.getAndSet(0);
            }
        };
        registry.register("GraphiteReporter.connectDurationMS", connectionGauge);

        final Gauge<Long> publishGauge = new Gauge<Long>() {
            /**
             * Returns the metric's current value.
             *
             * @return the metric's current value
             */
            @Override
            public Long getValue() {
                return publishDurationMS.getAndSet(0);
            }
        };
        registry.register("GraphiteReporter.publishDurationMS", publishGauge);

        final Gauge<Long> payloadGauge = new Gauge<Long>() {
            @Override
            public Long getValue() {
                return payloadSize.getAndSet(0);
            }
        };
        registry.register("GraphiteReporter.networkPayloadBytes", payloadGauge);
    }

    private static String threadName(String name){
        String threadName = "graphite-reporter";
        if(name != null && !name.isEmpty()){
            threadName = threadName + "-" + name;
        }

        return threadName;
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {
        final long startTime = clock.getTime();
        final long timestamp = startTime / 1000;

        long bytesSent = 0;

        // oh it'd be lovely to use Java 7 here
        try {
            graphite.connect();
            connectDurationMS.set(clock.getTime() - startTime);

            if(LOGGER.isInfoEnabled()) { LOGGER.info("Gauges     : " + gauges.size()); }
            for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
                bytesSent += reportGauge(entry.getKey(), entry.getValue(), timestamp);
            }

            if(LOGGER.isInfoEnabled()) { LOGGER.info("Counters   : " + counters.size()); }
            for (Map.Entry<String, Counter> entry : counters.entrySet()) {
                bytesSent += reportCounter(entry.getKey(), entry.getValue(), timestamp);
            }

            if(LOGGER.isInfoEnabled()) { LOGGER.info("Histograms : " + histograms.size()); }
            for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
                bytesSent += reportHistogram(entry.getKey(), entry.getValue(), timestamp);
            }

            if(LOGGER.isInfoEnabled()) { LOGGER.info("Meters     : " + meters.size()); }
            for (Map.Entry<String, Meter> entry : meters.entrySet()) {
                bytesSent += reportMetered(entry.getKey(), entry.getValue(), timestamp);
            }

            if(LOGGER.isInfoEnabled()) { LOGGER.info("Timers     : " + timers.size()); }
            for (Map.Entry<String, Timer> entry : timers.entrySet()) {
                bytesSent += reportTimer(entry.getKey(), entry.getValue(), timestamp);
            }
            payloadSize.set(bytesSent);

        } catch (IOException e) {
            reportingRegistry.meter("GraphiteReporter.Errors." + e.getClass().getSimpleName());
            LOGGER.warn("Unable to report to Graphite", graphite, e);
        } catch (Exception e) {
            reportingRegistry.meter("GraphiteReporter.Errors." + e.getClass().getSimpleName());
            LOGGER.error("Unable to report metrics to Graphite", graphite, e);
        } finally {
            try {
                publishDurationMS.set(startTime - clock.getTime());

                graphite.close();
            } catch (IOException e) {
                LOGGER.debug("Error disconnecting from Graphite", graphite, e);
            }
        }
    }

    private int reportTimer(String name, Timer timer, long timestamp) throws IOException {
        final Snapshot snapshot = timer.getSnapshot();
        int byteCount = 0;

        byteCount += graphite.send(prefix(name, "max"), format(convertDuration(snapshot.getMax())), timestamp);
        byteCount += graphite.send(prefix(name, "mean"), format(convertDuration(snapshot.getMean())), timestamp);
        byteCount += graphite.send(prefix(name, "min"), format(convertDuration(snapshot.getMin())), timestamp);
        byteCount += graphite.send(prefix(name, "stddev"),
                      format(convertDuration(snapshot.getStdDev())),
                      timestamp);
        byteCount += graphite.send(prefix(name, "p50"),
                      format(convertDuration(snapshot.getMedian())),
                      timestamp);
        byteCount += graphite.send(prefix(name, "p75"),
                      format(convertDuration(snapshot.get75thPercentile())),
                      timestamp);
        byteCount += graphite.send(prefix(name, "p95"),
                      format(convertDuration(snapshot.get95thPercentile())),
                      timestamp);
        byteCount += graphite.send(prefix(name, "p98"),
                      format(convertDuration(snapshot.get98thPercentile())),
                      timestamp);
        byteCount += graphite.send(prefix(name, "p99"),
                      format(convertDuration(snapshot.get99thPercentile())),
                      timestamp);
        byteCount += graphite.send(prefix(name, "p999"),
                      format(convertDuration(snapshot.get999thPercentile())),
                      timestamp);

        byteCount += reportMetered(name, timer, timestamp);

        return byteCount;
    }

    private int reportMetered(String name, Metered meter, long timestamp) throws IOException {
        int byteCount = 0;
        byteCount += graphite.send(prefix(name, "count"), format(meter.getCount()), timestamp);
        byteCount += graphite.send(prefix(name, "m1_rate"),
                      format(convertRate(meter.getOneMinuteRate())),
                      timestamp);
        byteCount += graphite.send(prefix(name, "m5_rate"),
                      format(convertRate(meter.getFiveMinuteRate())),
                      timestamp);
        byteCount += graphite.send(prefix(name, "m15_rate"),
                      format(convertRate(meter.getFifteenMinuteRate())),
                      timestamp);
        byteCount += graphite.send(prefix(name, "mean_rate"),
                      format(convertRate(meter.getMeanRate())),
                      timestamp);

        return byteCount;
    }

    private int reportHistogram(String name, Histogram histogram, long timestamp) throws IOException {
        final Snapshot snapshot = histogram.getSnapshot();
        int byteCount = 0;
        byteCount += graphite.send(prefix(name, "count"), format(histogram.getCount()), timestamp);
        byteCount += graphite.send(prefix(name, "max"), format(snapshot.getMax()), timestamp);
        byteCount += graphite.send(prefix(name, "mean"), format(snapshot.getMean()), timestamp);
        byteCount += graphite.send(prefix(name, "min"), format(snapshot.getMin()), timestamp);
        byteCount += graphite.send(prefix(name, "stddev"), format(snapshot.getStdDev()), timestamp);
        byteCount += graphite.send(prefix(name, "p50"), format(snapshot.getMedian()), timestamp);
        byteCount += graphite.send(prefix(name, "p75"), format(snapshot.get75thPercentile()), timestamp);
        byteCount += graphite.send(prefix(name, "p95"), format(snapshot.get95thPercentile()), timestamp);
        byteCount += graphite.send(prefix(name, "p98"), format(snapshot.get98thPercentile()), timestamp);
        byteCount += graphite.send(prefix(name, "p99"), format(snapshot.get99thPercentile()), timestamp);
        byteCount += graphite.send(prefix(name, "p999"), format(snapshot.get999thPercentile()), timestamp);

        return byteCount;
    }

    private int reportCounter(String name, Counter counter, long timestamp) throws IOException {
        return graphite.send(prefix(name, "count"), format(counter.getCount()), timestamp);
    }

    private int reportGauge(String name, Gauge gauge, long timestamp) throws IOException {
        final String value = format(gauge.getValue());
        if (value != null) {
            return graphite.send(prefix(name), value, timestamp);
        }
        return 0;
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
