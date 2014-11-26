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
import com.codahale.metrics.graphite.deadqueue.DeadQueue;
import com.codahale.metrics.graphite.deadqueue.Entry;
import com.codahale.metrics.graphite.deadqueue.NoOperationDeadQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
        private DeadQueue deadQueue;
        private ExecutorService executorService;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.clock = Clock.defaultClock();
            this.prefix = null;
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
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

        public Builder deadQueue(DeadQueue deadQueue) {
            this.deadQueue = deadQueue;
            return this;
        }

        public Builder executorService(ExecutorService executorService) {
            this.executorService = executorService;
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
                    deadQueue != null ? deadQueue : new NoOperationDeadQueue(),
                    executorService != null ? executorService : Executors.newSingleThreadExecutor());
        }
    }


    private static final Logger LOGGER = LoggerFactory.getLogger(GraphiteReporter.class);

    private final GraphiteSender graphite;
    private final Clock clock;
    private final String prefix;
    private final DeadQueue deadQueue;
    private final ExecutorService executorService;

    private GraphiteReporter(MetricRegistry registry,
                             GraphiteSender graphite,
                             Clock clock,
                             String prefix,
                             TimeUnit rateUnit,
                             TimeUnit durationUnit,
                             MetricFilter filter,
                             DeadQueue deadQueue,
                             ExecutorService executorService) {
        super(registry, "graphite-reporter", filter, rateUnit, durationUnit);
        this.graphite = graphite;
        this.clock = clock;
        this.prefix = prefix;
        this.deadQueue = deadQueue;
        this.executorService = executorService;
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

    private void reportTimer(String name, Timer timer, long timestamp) {
        final Snapshot snapshot = timer.getSnapshot();

        send(prefix(name, "max"), format(convertDuration(snapshot.getMax())), timestamp);
        send(prefix(name, "mean"), format(convertDuration(snapshot.getMean())), timestamp);
        send(prefix(name, "min"), format(convertDuration(snapshot.getMin())), timestamp);
        send(prefix(name, "stddev"),
                format(convertDuration(snapshot.getStdDev())),
                timestamp);
        send(prefix(name, "p50"),
                format(convertDuration(snapshot.getMedian())),
                timestamp);
        send(prefix(name, "p75"),
                format(convertDuration(snapshot.get75thPercentile())),
                timestamp);
        send(prefix(name, "p95"),
                format(convertDuration(snapshot.get95thPercentile())),
                timestamp);
        send(prefix(name, "p98"),
                format(convertDuration(snapshot.get98thPercentile())),
                timestamp);
        send(prefix(name, "p99"),
                format(convertDuration(snapshot.get99thPercentile())),
                timestamp);
        send(prefix(name, "p999"),
                format(convertDuration(snapshot.get999thPercentile())),
                timestamp);

        reportMetered(name, timer, timestamp);
    }

    private void reportMetered(String name, Metered meter, long timestamp) {
        send(prefix(name, "count"), format(meter.getCount()), timestamp);
        send(prefix(name, "m1_rate"),
                format(convertRate(meter.getOneMinuteRate())),
                timestamp);
        send(prefix(name, "m5_rate"),
                format(convertRate(meter.getFiveMinuteRate())),
                timestamp);
        send(prefix(name, "m15_rate"),
                format(convertRate(meter.getFifteenMinuteRate())),
                timestamp);
        send(prefix(name, "mean_rate"),
                format(convertRate(meter.getMeanRate())),
                timestamp);
    }

    private void reportHistogram(String name, Histogram histogram, long timestamp) throws IOException {
        final Snapshot snapshot = histogram.getSnapshot();
        send(prefix(name, "count"), format(histogram.getCount()), timestamp);
        send(prefix(name, "max"), format(snapshot.getMax()), timestamp);
        send(prefix(name, "mean"), format(snapshot.getMean()), timestamp);
        send(prefix(name, "min"), format(snapshot.getMin()), timestamp);
        send(prefix(name, "stddev"), format(snapshot.getStdDev()), timestamp);
        send(prefix(name, "p50"), format(snapshot.getMedian()), timestamp);
        send(prefix(name, "p75"), format(snapshot.get75thPercentile()), timestamp);
        send(prefix(name, "p95"), format(snapshot.get95thPercentile()), timestamp);
        send(prefix(name, "p98"), format(snapshot.get98thPercentile()), timestamp);
        send(prefix(name, "p99"), format(snapshot.get99thPercentile()), timestamp);
        send(prefix(name, "p999"), format(snapshot.get999thPercentile()), timestamp);
    }

    private void reportCounter(String name, Counter counter, long timestamp) throws IOException {
        send(prefix(name, "count"), format(counter.getCount()), timestamp);
    }

    private void reportGauge(String name, Gauge gauge, long timestamp) {
        final String value = format(gauge.getValue());
        if (value != null) {
            send(prefix(name), value, timestamp);
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

    private void send(String name, String value, long timestamp) {
        try {
            if (!graphite.isConnected()) {
                graphite.connect();
                if (!deadQueue.isEmpty()) {
                    flushDeadQueue();
                }
            }

            graphite.send(name, value, timestamp);
        } catch (IOException e) {
            deadQueue.add(new Entry(name, value, timestamp));
            try {
                graphite.close();
            } catch (IOException e1) {
                LOGGER.warn("Error closing Graphite", graphite, e1);
            }
        }
    }

    private void flushDeadQueue() {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                while (!deadQueue.isEmpty()) {
                    try {
                        Entry entry = deadQueue.poll();
                        graphite.send(entry.getName(), entry.getValue(), entry.getTimestamp());

                    } catch (IOException ex) {
                        LOGGER.warn("Unable to report to Graphite", graphite, ex);
                    }
                }
            }
        });
    }
}
