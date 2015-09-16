package io.dropwizard.metrics.atsd;

import io.dropwizard.metrics.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

/**
 * A reporter which publishes metric values to a ATSD server.
 *
 * @see <a href="https://https://axibase.com//">ATSD - Axibase Time Series Database</a>
 */
public class AtsdReporter extends ScheduledReporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AtsdReporter.class);

    private static final String MAX = "max";
    private static final String MIN = "min";
    private static final String MEAN = "mean";
    private static final String STDDEV = "stddev";
    private static final String P50 = "p50";
    private static final String P75 = "p75";
    private static final String P95 = "p95";
    private static final String P98 = "p98";
    private static final String P99 = "p99";
    private static final String P999 = "p999";
    private static final String COUNT = "count";
    private static final String M1_RATE = "m1_rate";
    private static final String M5_RATE = "m5_rate";
    private static final String M15_RATE = "m15_rate";
    private static final String MEAN_RATE = "mean_rate";
    private static final String REPORTER_NAME = "atsd-reporter";

    private final AtsdSender sender;
    private final Clock clock;
    private final String prefix;
    private final String entity;


    private AtsdReporter(MetricRegistry registry,
                         AtsdSender sender,
                         Clock clock,
                         String prefix,
                         TimeUnit rateUnit,
                         TimeUnit durationUnit,
                         MetricFilter filter,
                         String entity) {
        super(registry, REPORTER_NAME, filter, rateUnit, durationUnit);
        this.sender = sender;
        this.clock = clock;
        this.prefix = prefix;
        this.entity = entity;
    }

    @Override
    public void report(SortedMap<MetricName, Gauge> gauges,
                       SortedMap<MetricName, Counter> counters,
                       SortedMap<MetricName, Histogram> histograms,
                       SortedMap<MetricName, Meter> meters,
                       SortedMap<MetricName, Timer> timers) {
        final long timestamp = clock.getTime();

        try {
            if (!sender.isConnected()) {
                sender.connect();
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

            sender.flush();
        } catch (IOException e) {
            LOGGER.warn("Unable to send metric into atsd", e);
            try {
                sender.close();
            } catch (IOException e1) {
                LOGGER.warn("Error closing atsd", e1);
            }
        }
    }

    @Override
    public void stop() {
        try {
            super.stop();
        } finally {
            try {
                sender.close();
            } catch (IOException e) {
                LOGGER.warn("Error disconnecting from ATSD", e);
            }
        }
    }

    private void reportTimer(MetricName metric, Timer timer, long timestamp) throws IOException {
        final Snapshot snapshot = timer.getSnapshot();
        sender.send(entity, prefix(metric, MAX),
                format(convertDuration(snapshot.getMax())), timestamp);
        sender.send(entity, prefix(metric, MEAN),
                format(convertDuration(snapshot.getMean())), timestamp);
        sender.send(entity, prefix(metric, MIN),
                format(convertDuration(snapshot.getMin())), timestamp);
        sender.send(entity, prefix(metric, STDDEV),
                format(convertDuration(snapshot.getStdDev())), timestamp);
        sender.send(entity, prefix(metric, P50),
                format(convertDuration(snapshot.getMedian())), timestamp);
        sender.send(entity, prefix(metric, P75),
                format(convertDuration(snapshot.get75thPercentile())), timestamp);
        sender.send(entity, prefix(metric, P95),
                format(convertDuration(snapshot.get95thPercentile())), timestamp);
        sender.send(entity, prefix(metric, P98),
                format(convertDuration(snapshot.get98thPercentile())), timestamp);
        sender.send(entity, prefix(metric, P99),
                format(convertDuration(snapshot.get99thPercentile())), timestamp);
        sender.send(entity, prefix(metric, P999),
                format(convertDuration(snapshot.get999thPercentile())), timestamp);

        reportMetered(metric, timer, timestamp);
    }

    private void reportMetered(MetricName metric, Metered meter, long timestamp) throws IOException {
        sender.send(entity, prefix(metric, COUNT),
                format(meter.getCount()), timestamp);
        sender.send(entity, prefix(metric, M1_RATE),
                format(convertRate(meter.getOneMinuteRate())), timestamp);
        sender.send(entity, prefix(metric, M5_RATE),
                format(convertRate(meter.getFiveMinuteRate())), timestamp);
        sender.send(entity, prefix(metric, M15_RATE),
                format(convertRate(meter.getFifteenMinuteRate())), timestamp);
        sender.send(entity, prefix(metric, MEAN_RATE),
                format(convertRate(meter.getMeanRate())), timestamp);
    }

    private void reportHistogram(MetricName metric, Histogram histogram, long timestamp) throws IOException {
        final Snapshot snapshot = histogram.getSnapshot();
        sender.send(entity, prefix(metric, COUNT),
                format(histogram.getCount()), timestamp);
        sender.send(entity, prefix(metric, MAX),
                format(snapshot.getMax()), timestamp);
        sender.send(entity, prefix(metric, MEAN),
                format(snapshot.getMean()), timestamp);
        sender.send(entity, prefix(metric, MIN),
                format(snapshot.getMin()), timestamp);
        sender.send(entity, prefix(metric, STDDEV),
                format(snapshot.getStdDev()), timestamp);
        sender.send(entity, prefix(metric, P50),
                format(snapshot.getMedian()), timestamp);
        sender.send(entity, prefix(metric, P75),
                format(snapshot.get75thPercentile()), timestamp);
        sender.send(entity, prefix(metric, P95),
                format(snapshot.get95thPercentile()), timestamp);
        sender.send(entity, prefix(metric, P98),
                format(snapshot.get98thPercentile()), timestamp);
        sender.send(entity, prefix(metric, P99),
                format(snapshot.get99thPercentile()), timestamp);
        sender.send(entity, prefix(metric, P999),
                format(snapshot.get999thPercentile()), timestamp);
    }

    private void reportCounter(MetricName metric, Counter counter, long timestamp) throws IOException {
        sender.send(entity, prefix(metric, COUNT),
                format(counter.getCount()), timestamp);
    }

    private void reportGauge(MetricName metric, Gauge gauge, long timestamp) throws IOException {
        final String value = format(gauge.getValue());
        if (value != null) {
            sender.send(entity, prefix(metric), value, timestamp);
        }
    }

    private String format(Object o) {
        if (o instanceof Float) {
            return o.toString();
        } else if (o instanceof Double) {
            return o.toString();
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

    private MetricName prefix(MetricName metric, String component) {
        String key = new StringBuilder(this.prefix)
                .append(".")
                .append(metric.getKey())
                .append(".")
                .append(component)
                .toString();
        return new MetricName(key, metric.getTags());
    }

    private MetricName prefix(MetricName metric) {
        String key = new StringBuilder(this.prefix)
                .append(".")
                .append(metric.getKey())
                .toString();
        return new MetricName(key, metric.getTags());
    }

    private String format(long n) {
        return Long.toString(n);
    }

    /**
     * Returns a new {@link Builder} for {@link AtsdReporter}.
     *
     * @param registry the registry to report
     * @return a {@link Builder} instance for a {@link AtsdReporter}
     */
    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }

    /**
     * A builder for {@link AtsdReporter} instances. Defaults to not using a prefix, using the
     * default clock, converting rates to events/second, converting durations to milliseconds, and
     * not filtering metrics.
     */
    public static class Builder {
        private static final String DEFAULT_HOST_NAME = "undefined_host";
        private static final int DEFAULT_BUFFER_SIZE = 64;
        private static final int EOF = -1;
        private static final String HOSTNAME = getHostname();

        private final MetricRegistry registry;
        private Clock clock;
        private String prefix;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;
        private String entity;

        private static String getHostname() {
            InputStream inputStream = null;
            try {
                Process process = Runtime.getRuntime().exec("hostname");
                process.waitFor();
                StringBuilder hostname = new StringBuilder();
                inputStream = process.getInputStream();
                InputStreamReader input = new InputStreamReader(inputStream, Charset.defaultCharset());
                char[] buffer = new char[DEFAULT_BUFFER_SIZE];
                int n;
                while (EOF != (n = input.read(buffer))) {
                    hostname.append(buffer, 0, n);
                }
                return hostname.toString().trim();
            } catch (Exception e) {
                LOGGER.error("Unable to get hostname, using 'defaultEntity'", e);
                return "defaultEntity";
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Exception ex) {
                        LOGGER.warn("Unable to close stream", ex);
                    }
                }
            }
        }

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.clock = Clock.defaultClock();
            this.prefix = null;
            this.entity = HOSTNAME;
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
         * Use the given string as entity name.
         *
         * @param entity the entity name
         * @return {@code this}
         */
        public Builder setEntity(String entity) {
            this.entity = entity;
            return this;
        }

        /**
         * Prefix all metric names with the given string.
         *
         * @param prefix the prefix for all metric names
         * @return {@code this}
         */
        public Builder setMetricPrefix(String prefix) {
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
         * Builds a {@link AtsdReporter} with the given properties, sending metrics using the
         * given {@link AtsdSender}.
         *
         * @param sender a {@link AtsdSender}
         * @return a {@link AtsdReporter}
         */
        public AtsdReporter build(AtsdSender sender) {
            return new AtsdReporter(registry,
                    sender,
                    clock,
                    prefix,
                    rateUnit,
                    durationUnit,
                    filter,
                    entity);
        }

    }
}
