package com.codahale.metrics.graphite;

import com.codahale.metrics.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.codahale.metrics.MetricRegistry.name;

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

        private ScheduledExecutorService executorService;
        private MetricRegistry reportingRegistry;
        private Clock clock;
        private String prefix;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;

        private GraphiteMeterProfile meterProfile;
        private GraphiteHistogramProfile histogramProfile;
        private GraphiteTimerProfile timerProfile;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.reportingRegistry = registry;
            this.clock = Clock.defaultClock();
            this.prefix = null;
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
            this.meterProfile = new GraphiteMeterProfile.Builder().build();
            this.histogramProfile = new GraphiteHistogramProfile.Builder().build();
            this.timerProfile = new GraphiteTimerProfile.Builder().build();
        }

        public Builder executorService(ScheduledExecutorService executorService) {
            this.executorService = executorService;
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

        public Builder reportingRegistry(MetricRegistry registry) {
            this.reportingRegistry = registry;
            return this;
        }

        public Builder meterProfile(GraphiteMeterProfile profile) {
            this.meterProfile = profile;
            return this;
        }

        public Builder histogramProfile(GraphiteHistogramProfile profile) {
            this.histogramProfile = profile;
            return this;
        }

        public Builder timerProfile(GraphiteTimerProfile profile) {
            this.timerProfile = profile;
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
            return new GraphiteReporter(
                    executorService,
                    registry,
                    reportingRegistry,
                    graphite,
                    clock,
                    prefix,
                    rateUnit,
                    durationUnit,
                    filter,
                    meterProfile,
                    histogramProfile,
                    timerProfile
            );
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphiteReporter.class);

    private final Graphite graphite;
    private final Clock clock;
    private final String prefix;

    private final AtomicLong connectDurationMS = new AtomicLong(0);
    private final AtomicLong publishDurationMS = new AtomicLong(0);
    private final AtomicLong payloadSize = new AtomicLong(0);

    private final GraphiteMeterProfile meterProfile;
    private final GraphiteHistogramProfile histogramProfile;
    private final GraphiteTimerProfile timerProfile;

    private GraphiteReporter(ScheduledExecutorService executor,
                                    MetricRegistry registry,
                                    MetricRegistry reportingRegistry,
                                    Graphite graphite,
                                    Clock clock,
                                    String prefix,
                                    TimeUnit rateUnit,
                                    TimeUnit durationUnit,
                                    MetricFilter filter,
                                    GraphiteMeterProfile meterProfile,
                                    GraphiteHistogramProfile histogramProfile,
                                    GraphiteTimerProfile timerProfile) {
        super(registry, executor, filter, rateUnit, durationUnit);
        this.graphite = graphite;
        this.clock = clock;
        this.prefix = prefix;
        this.meterProfile = meterProfile;
        this.histogramProfile = histogramProfile;
        this.timerProfile = timerProfile;

        configurePublishingMetrics(reportingRegistry);
    }

    /**
     * The publisher tracks connection time, publish time, and payload size.  These metrics lag real
     * time by one publishing period.
     * <p/>
     * Meaning, at time T, this records how long it took to connect and publish the data that report's
     * size.  At T+1, the values from T are sent with T+1's report.
     */
    private void configurePublishingMetrics(MetricRegistry registry) {
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

    @Override
    public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {
        final long startTime = clock.getTime();
        final long timestamp = startTime / 1000;

        long bytesSent = 0;
        long connectTime = 0;

        // oh it'd be lovely to use Java 7 here
        try {
            graphite.connect();
            connectTime = clock.getTime();

            if (LOGGER.isInfoEnabled()) { LOGGER.info("Gauges     : " + gauges.size()); }
            for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
                final String name = entry.getKey();
                try {
                    bytesSent += reportGauge(name, entry.getValue(), timestamp);
                } catch (IOException e) {
                    throw e;
                } catch (Exception e) {
                    LOGGER.warn("Unable to report " + name, e);
                }
            }

            if (LOGGER.isInfoEnabled()) { LOGGER.info("Counters   : " + counters.size()); }
            for (Map.Entry<String, Counter> entry : counters.entrySet()) {
                final String name = entry.getKey();
                try {
                    bytesSent += reportCounter(name, entry.getValue(), timestamp);
                } catch (IOException e) {
                    throw e;
                } catch (Exception e) {
                    LOGGER.warn("Unable to report " + name, e);
                }
            }

            if (LOGGER.isInfoEnabled()) { LOGGER.info("Meters     : " + meters.size()); }
            for (Map.Entry<String, Meter> entry : meters.entrySet()) {
                final String name = entry.getKey();
                try {
                    bytesSent += reportMeter(name, entry.getValue(), timestamp);
                } catch (IOException e) {
                    throw e;
                } catch (Exception e) {
                    LOGGER.warn("Unable to report " + name, e);
                }
            }

            if (LOGGER.isInfoEnabled()) { LOGGER.info("Histograms : " + histograms.size()); }
            for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
                final String name = entry.getKey();
                try {
                    bytesSent += reportHistogram(name, entry.getValue(), timestamp);
                } catch (IOException e) {
                    throw e;
                } catch (Exception e) {
                    LOGGER.warn("Unable to report " + name, e);
                }
            }

            if (LOGGER.isInfoEnabled()) { LOGGER.info("Timers     : " + timers.size()); }
            for (Map.Entry<String, Timer> entry : timers.entrySet()) {
                final String name = entry.getKey();
                try {
                    bytesSent += reportTimer(name, entry.getValue(), timestamp);
                } catch (IOException e) {
                    throw e;
                } catch (Exception e) {
                    LOGGER.warn("Unable to report " + name, e);
                }
            }

            payloadSize.set(bytesSent);

        } catch (IOException e) {
            LOGGER.warn("Unable to report to Graphite", graphite, e);

        } finally {
            try {
                publishDurationMS.set(clock.getTime() - startTime);
                if (connectTime > 0) {
                    connectDurationMS.set(clock.getTime() - connectTime);
                }

                graphite.close();
            } catch (IOException e) {
                LOGGER.warn("Error disconnecting from Graphite", graphite, e);
            }
        }
    }

    private int reportGauge(String name, Gauge gauge, long timestamp) throws IOException {
        final String value = format(gauge.getValue());
        if (value != null) {
            return graphite.send(prefix(name), value, timestamp);
        }
        return 0;
    }

    private int reportCounter(String name, Counter counter, long timestamp) throws IOException {
        return graphite.send(prefix(name, "count"), format(counter.getCount()), timestamp);
    }

    private int reportMeter(String name, Metered meter, long timestamp) throws IOException {
        int byteCount = 0;

        if (meterProfile.isCount()) {
            byteCount += graphite.send(prefix(name, "count"), format(meter.getCount()), timestamp);
        }

        if (meterProfile.isOneMinuteRate()) {
            byteCount += graphite.send(prefix(name, "m1_rate"), format(convertRate(meter.getOneMinuteRate())), timestamp);
        }

        if (meterProfile.isFiveMinuteRate()) {
            byteCount += graphite.send(prefix(name, "m5_rate"), format(convertRate(meter.getFiveMinuteRate())), timestamp);
        }

        if (meterProfile.isFifteenMinuteRate()) {
            byteCount += graphite.send(prefix(name, "m15_rate"), format(convertRate(meter.getFifteenMinuteRate())), timestamp);
        }

        if (meterProfile.isMeanRate()) {
            byteCount += graphite.send(prefix(name, "mean_rate"), format(convertRate(meter.getMeanRate())), timestamp);
        }

        return byteCount;
    }

    private int reportHistogram(String name, Histogram histogram, long timestamp) throws IOException {
        final Snapshot snapshot = histogram.getSnapshot();
        int byteCount = 0;

        if (histogramProfile.isCount()) {
            byteCount += graphite.send(prefix(name, "count"), format(histogram.getCount()), timestamp);
        }

        if (histogramProfile.isMax()) {
            byteCount += graphite.send(prefix(name, "max"), format(snapshot.getMax()), timestamp);
        }

        if (histogramProfile.isMean()) {
            byteCount += graphite.send(prefix(name, "mean"), format(snapshot.getMean()), timestamp);
        }

        if (histogramProfile.isMin()) {
            byteCount += graphite.send(prefix(name, "min"), format(snapshot.getMin()), timestamp);
        }

        if (histogramProfile.isStdDev()) {
            byteCount += graphite.send(prefix(name, "stddev"), format(snapshot.getStdDev()), timestamp);
        }

        if (histogramProfile.is50thPercentile()) {
            byteCount += graphite.send(prefix(name, "p50"), format(snapshot.getMedian()), timestamp);
        }

        if (histogramProfile.is75thPercentile()) {
            byteCount += graphite.send(prefix(name, "p75"), format(snapshot.get75thPercentile()), timestamp);
        }

        if (histogramProfile.is95thPercentile()) {
            byteCount += graphite.send(prefix(name, "p95"), format(snapshot.get95thPercentile()), timestamp);
        }

        if (histogramProfile.is98thPercentile()) {
            byteCount += graphite.send(prefix(name, "p98"), format(snapshot.get98thPercentile()), timestamp);
        }

        if (histogramProfile.is99thPercentile()) {
            byteCount += graphite.send(prefix(name, "p99"), format(snapshot.get99thPercentile()), timestamp);
        }

        if (histogramProfile.is999thPercentile()) {
            byteCount += graphite.send(prefix(name, "p999"), format(snapshot.get999thPercentile()), timestamp);
        }

        return byteCount;
    }

    private int reportTimer(String name, Timer timer, long timestamp) throws IOException {
        final Snapshot snapshot = timer.getSnapshot();
        int byteCount = 0;

        if (timerProfile.isCount()) {
            byteCount += graphite.send(prefix(name, "count"), format(timer.getCount()), timestamp);
        }

        if (timerProfile.isOneMinuteRate()) {
            byteCount += graphite.send(prefix(name, "m1_rate"), format(convertRate(timer.getOneMinuteRate())), timestamp);
        }

        if (timerProfile.isFiveMinuteRate()) {
            byteCount += graphite.send(prefix(name, "m5_rate"), format(convertRate(timer.getFiveMinuteRate())), timestamp);
        }

        if (timerProfile.isFifteenMinuteRate()) {
            byteCount += graphite.send(prefix(name, "m15_rate"), format(convertRate(timer.getFifteenMinuteRate())), timestamp);
        }

        if (timerProfile.isMeanRate()) {
            byteCount += graphite.send(prefix(name, "mean_rate"), format(convertRate(timer.getMeanRate())), timestamp);
        }

        if (timerProfile.isMax()) {
            byteCount += graphite.send(prefix(name, "max"), format(convertDuration(snapshot.getMax())), timestamp);
        }

        if (timerProfile.isMean()) {
            byteCount += graphite.send(prefix(name, "mean"), format(convertDuration(snapshot.getMean())), timestamp);
        }

        if (timerProfile.isMin()) {
            byteCount += graphite.send(prefix(name, "min"), format(convertDuration(snapshot.getMin())), timestamp);
        }

        if (timerProfile.isStdDev()) {
            byteCount += graphite.send(prefix(name, "stddev"), format(convertDuration(snapshot.getStdDev())), timestamp);
        }

        if (timerProfile.is50thPercentile()) {
            byteCount += graphite.send(prefix(name, "p50"), format(convertDuration(snapshot.getMedian())), timestamp);
        }

        if (timerProfile.is75thPercentile()) {
            byteCount += graphite.send(prefix(name, "p75"), format(convertDuration(snapshot.get75thPercentile())), timestamp);
        }

        if (timerProfile.is95thPercentile()) {
            byteCount += graphite.send(prefix(name, "p95"), format(convertDuration(snapshot.get95thPercentile())), timestamp);
        }

        if (timerProfile.is98thPercentile()) {
            byteCount += graphite.send(prefix(name, "p98"), format(convertDuration(snapshot.get98thPercentile())), timestamp);
        }

        if (timerProfile.is99thPercentile()) {
            byteCount += graphite.send(prefix(name, "p99"), format(convertDuration(snapshot.get99thPercentile())), timestamp);
        }

        if (timerProfile.is999thPercentile()) {
            byteCount += graphite.send(prefix(name, "p999"), format(convertDuration(snapshot.get999thPercentile())), timestamp);
        }

        return byteCount;
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
        return name(prefix, components);
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
