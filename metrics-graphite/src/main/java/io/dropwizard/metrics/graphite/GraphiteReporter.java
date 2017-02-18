package io.dropwizard.metrics.graphite;

import io.dropwizard.metrics.Clock;
import io.dropwizard.metrics.Counter;
import io.dropwizard.metrics.Gauge;
import io.dropwizard.metrics.Histogram;
import io.dropwizard.metrics.Meter;
import io.dropwizard.metrics.Metered;
import io.dropwizard.metrics.MetricFilter;
import io.dropwizard.metrics.MetricRegistry;
import io.dropwizard.metrics.ScheduledReporter;
import io.dropwizard.metrics.Snapshot;
import io.dropwizard.metrics.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.metrics.*;

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
        private MetricNameFormatter nameFormatter;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.clock = Clock.defaultClock();
            this.prefix = null;
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
            this.nameFormatter = MetricNameFormatter.NAME_ONLY;
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
         * Format {@link MetricName MetricNames} with the given {@link MetricNameFormatter}
         * @param nameFormatter the nameFormatter
         * @return {@code this}
         */
        public Builder withMetricNameFormatter(MetricNameFormatter nameFormatter) {
        	this.nameFormatter = nameFormatter;
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
                                        nameFormatter);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphiteReporter.class);

    private final GraphiteSender graphite;
    private final Clock clock;
    private final MetricName prefix;
    private final MetricNameFormatter nameFormatter;

    private GraphiteReporter(MetricRegistry registry,
                             GraphiteSender graphite,
                             Clock clock,
                             String prefix,
                             TimeUnit rateUnit,
                             TimeUnit durationUnit,
                             MetricFilter filter,
                             MetricNameFormatter nameFormatter) {
        super(registry, "graphite-reporter", filter, rateUnit, durationUnit);
        this.graphite = graphite;
        this.clock = clock;
        this.prefix = MetricName.build(prefix);
        this.nameFormatter = nameFormatter;
    }

    @Override
    public void report(SortedMap<MetricName, Gauge> gauges,
                       SortedMap<MetricName, Counter> counters,
                       SortedMap<MetricName, Histogram> histograms,
                       SortedMap<MetricName, Meter> meters,
                       SortedMap<MetricName, Timer> timers) {
        final long timestamp = clock.getTime() / 1000;

        // oh it'd be lovely to use Java 7 here
        try {
            if (!graphite.isConnected()) {
    	          graphite.connect();
            }

            for (Map.Entry<MetricName, Gauge> entry : gauges.entrySet()) {
                reportGauge(entry.getKey(), entry.getValue(), timestamp);
            }

            for (Map.Entry<MetricName, Counter> entry : counters.entrySet()) {
                reportCounter(entry.getKey(), entry.getValue(), timestamp);
            }

            for (Map.Entry<MetricName, Histogram> entry : histograms.entrySet()) {
                reportHistogram(entry.getKey(), entry.getValue(), timestamp);
            }

            for (Map.Entry<MetricName, Meter> entry : meters.entrySet()) {
                reportMetered(entry.getKey(), entry.getValue(), timestamp);
            }

            for (Map.Entry<MetricName, Timer> entry : timers.entrySet()) {
                reportTimer(entry.getKey(), entry.getValue(), timestamp);
            }

            graphite.flush();
        } catch (Throwable t) {
            LOGGER.warn("Unable to report to Graphite", graphite, t);
            closeGraphiteConnection();
        }
    }

    private void closeGraphiteConnection() {
        try {
            graphite.close();
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

    private void reportTimer(MetricName name, Timer timer, long timestamp) throws IOException {
        final Snapshot snapshot = timer.getSnapshot();

        graphite.send(formatName(name, "max"), format(convertDuration(snapshot.getMax())), timestamp);
        graphite.send(formatName(name, "mean"), format(convertDuration(snapshot.getMean())), timestamp);
        graphite.send(formatName(name, "min"), format(convertDuration(snapshot.getMin())), timestamp);
        graphite.send(formatName(name, "stddev"),
                      format(convertDuration(snapshot.getStdDev())),
                      timestamp);
        graphite.send(formatName(name, "p50"),
                      format(convertDuration(snapshot.getMedian())),
                      timestamp);
        graphite.send(formatName(name, "p75"),
                      format(convertDuration(snapshot.get75thPercentile())),
                      timestamp);
        graphite.send(formatName(name, "p95"),
                      format(convertDuration(snapshot.get95thPercentile())),
                      timestamp);
        graphite.send(formatName(name, "p98"),
                      format(convertDuration(snapshot.get98thPercentile())),
                      timestamp);
        graphite.send(formatName(name, "p99"),
                      format(convertDuration(snapshot.get99thPercentile())),
                      timestamp);
        graphite.send(formatName(name, "p999"),
                      format(convertDuration(snapshot.get999thPercentile())),
                      timestamp);

        reportMetered(name, timer, timestamp);
    }

    private void reportMetered(MetricName name, Metered meter, long timestamp) throws IOException {
        graphite.send(formatName(name, "count"), format(meter.getCount()), timestamp);
        graphite.send(formatName(name, "m1_rate"),
                      format(convertRate(meter.getOneMinuteRate())),
                      timestamp);
        graphite.send(formatName(name, "m5_rate"),
                      format(convertRate(meter.getFiveMinuteRate())),
                      timestamp);
        graphite.send(formatName(name, "m15_rate"),
                      format(convertRate(meter.getFifteenMinuteRate())),
                      timestamp);
        graphite.send(formatName(name, "mean_rate"),
                      format(convertRate(meter.getMeanRate())),
                      timestamp);
    }

    private void reportHistogram(MetricName name, Histogram histogram, long timestamp) throws IOException {
        final Snapshot snapshot = histogram.getSnapshot();
        graphite.send(formatName(name, "count"), format(histogram.getCount()), timestamp);
        graphite.send(formatName(name, "max"), format(snapshot.getMax()), timestamp);
        graphite.send(formatName(name, "mean"), format(snapshot.getMean()), timestamp);
        graphite.send(formatName(name, "min"), format(snapshot.getMin()), timestamp);
        graphite.send(formatName(name, "stddev"), format(snapshot.getStdDev()), timestamp);
        graphite.send(formatName(name, "p50"), format(snapshot.getMedian()), timestamp);
        graphite.send(formatName(name, "p75"), format(snapshot.get75thPercentile()), timestamp);
        graphite.send(formatName(name, "p95"), format(snapshot.get95thPercentile()), timestamp);
        graphite.send(formatName(name, "p98"), format(snapshot.get98thPercentile()), timestamp);
        graphite.send(formatName(name, "p99"), format(snapshot.get99thPercentile()), timestamp);
        graphite.send(formatName(name, "p999"), format(snapshot.get999thPercentile()), timestamp);
    }

    private void reportCounter(MetricName name, Counter counter, long timestamp) throws IOException {
        graphite.send(formatName(name, "count"), format(counter.getCount()), timestamp);
    }

    private void reportGauge(MetricName name, Gauge gauge, long timestamp) throws IOException {

        String valueToReport = format(gauge.getValue());
        if (valueToReport != null) {
            graphite.send(formatName(name), valueToReport, timestamp);
        }

    }

    private String format(Object o) {
        String value = null;
        if (o instanceof Number) {
            Number n = (Number) o;
            if (o instanceof Float || o instanceof Double) {
                // the Carbon plaintext format is pretty underspecified, but it seems like it just wants
                // US-formatted digits
                value = String.format(Locale.US, "%2.2f", n.doubleValue());
            } else {
                value = Long.toString(n.longValue());
            }
        } else if (o instanceof Boolean) {
            return format(((Boolean) o) ? 1 : 0);
        } else {
            LOGGER.warn("Unable to format value for Graphite.", o);
        }
        return value;
    }

    private String formatName(MetricName name, String... components) {
    	String formattedNameStr = this.nameFormatter.formatMetricName(name);
    	MetricName formattedName = new MetricName(formattedNameStr);
        return MetricName.join(MetricName.join(prefix, formattedName), MetricName.build(components)).getKey();
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
