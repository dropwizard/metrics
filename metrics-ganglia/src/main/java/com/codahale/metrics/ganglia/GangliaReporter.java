package com.codahale.metrics.ganglia;

import com.codahale.metrics.*;

import info.ganglia.gmetric4j.gmetric.GMetric;
import info.ganglia.gmetric4j.gmetric.GMetricSlope;
import info.ganglia.gmetric4j.gmetric.GMetricType;
import info.ganglia.gmetric4j.gmetric.GangliaException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * A reporter which announces metric values to a Ganglia cluster.
 *
 * @see <a href="http://ganglia.sourceforge.net/">Ganglia Monitoring System</a>
 */
public class GangliaReporter extends ScheduledReporter {

    private static final Pattern SLASHES = Pattern.compile("\\\\");

    /**
     * Returns a new {@link Builder} for {@link GangliaReporter}.
     *
     * @param registry the registry to report
     * @return a {@link Builder} instance for a {@link GangliaReporter}
     */
    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }

    /**
     * A builder for {@link GangliaReporter} instances. Defaults to using a {@code tmax} of {@code 60},
     * a {@code dmax} of {@code 0}, converting rates to events/second, converting durations to
     * milliseconds, and not filtering metrics.
     */
    public static class Builder {
        private final MetricRegistry registry;
        private String prefix;
        private int tMax;
        private int dMax;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;
        private ScheduledExecutorService executor;
        private boolean shutdownExecutorOnStop;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.tMax = 60;
            this.dMax = 0;
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
            this.executor = null;
            this.shutdownExecutorOnStop = true;
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
         * Use the given {@code tmax} value when announcing metrics.
         *
         * @param tMax the desired gmond {@code tmax} value
         * @return {@code this}
         */
        public Builder withTMax(int tMax) {
            this.tMax = tMax;
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
         * Use the given {@code dmax} value when announcing metrics.
         *
         * @param dMax the desired gmond {@code dmax} value
         * @return {@code this}
         */
        public Builder withDMax(int dMax) {
            this.dMax = dMax;
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
         * Builds a {@link GangliaReporter} with the given properties, announcing metrics to the
         * given {@link GMetric} client.
         *
         * @param gmetric the client to use for announcing metrics
         * @return a {@link GangliaReporter}
         */
        public GangliaReporter build(GMetric gmetric) {
            return new GangliaReporter(registry, gmetric, null, prefix, tMax, dMax, rateUnit, durationUnit, filter, executor, shutdownExecutorOnStop);
        }

        /**
         * Builds a {@link GangliaReporter} with the given properties, announcing metrics to the
         * given {@link GMetric} client.
         *
         * @param gmetrics the clients to use for announcing metrics
         * @return a {@link GangliaReporter}
         */
        public GangliaReporter build(GMetric... gmetrics) {
            return new GangliaReporter(registry, null, gmetrics, prefix, tMax, dMax, rateUnit, durationUnit, filter, executor, shutdownExecutorOnStop);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(GangliaReporter.class);

    private final GMetric gmetric;
    private final GMetric[] gmetrics;
    private final String prefix;
    private final int tMax;
    private final int dMax;

    private GangliaReporter(MetricRegistry registry,
                            GMetric gmetric,
                            GMetric[] gmetrics,
                            String prefix,
                            int tMax,
                            int dMax,
                            TimeUnit rateUnit,
                            TimeUnit durationUnit,
                            MetricFilter filter,
                            ScheduledExecutorService executor,
                            boolean shutdownExecutorOnStop) {
        super(registry, "ganglia-reporter", filter, rateUnit, durationUnit, executor, shutdownExecutorOnStop);
        this.gmetric = gmetric;
        this.gmetrics = gmetrics;
        this.prefix = prefix;
        this.tMax = tMax;
        this.dMax = dMax;
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {
        for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
            reportGauge(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Counter> entry : counters.entrySet()) {
            reportCounter(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
            reportHistogram(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Meter> entry : meters.entrySet()) {
            reportMeter(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Timer> entry : timers.entrySet()) {
            reportTimer(entry.getKey(), entry.getValue());
        }
    }

    private void reportTimer(String name, Timer timer) {
        final String sanitizedName = escapeSlashes(name);
        final String group = group(name);
        try {
            final Snapshot snapshot = timer.getSnapshot();

            announce(prefix(sanitizedName, "max"), group, convertDuration(snapshot.getMax()), getDurationUnit());
            announce(prefix(sanitizedName, "mean"), group, convertDuration(snapshot.getMean()), getDurationUnit());
            announce(prefix(sanitizedName, "min"), group, convertDuration(snapshot.getMin()), getDurationUnit());
            announce(prefix(sanitizedName, "stddev"), group, convertDuration(snapshot.getStdDev()), getDurationUnit());

            announce(prefix(sanitizedName, "p50"), group, convertDuration(snapshot.getMedian()), getDurationUnit());
            announce(prefix(sanitizedName, "p75"),
                     group,
                     convertDuration(snapshot.get75thPercentile()),
                     getDurationUnit());
            announce(prefix(sanitizedName, "p95"),
                     group,
                     convertDuration(snapshot.get95thPercentile()),
                     getDurationUnit());
            announce(prefix(sanitizedName, "p98"),
                     group,
                     convertDuration(snapshot.get98thPercentile()),
                     getDurationUnit());
            announce(prefix(sanitizedName, "p99"),
                     group,
                     convertDuration(snapshot.get99thPercentile()),
                     getDurationUnit());
            announce(prefix(sanitizedName, "p999"),
                     group,
                     convertDuration(snapshot.get999thPercentile()),
                     getDurationUnit());

            reportMetered(sanitizedName, timer, group, "calls");
        } catch (GangliaException e) {
            LOGGER.warn("Unable to report timer {}", sanitizedName, e);
        }
    }

    private void reportMeter(String name, Meter meter) {
        final String sanitizedName = escapeSlashes(name);
        final String group = group(name);
        try {
            reportMetered(sanitizedName, meter, group, "events");
        } catch (GangliaException e) {
            LOGGER.warn("Unable to report meter {}", name, e);
        }
    }

    private void reportMetered(String name, Metered meter, String group, String eventName) throws GangliaException {
        final String unit = eventName + '/' + getRateUnit();
        announce(prefix(name, "count"), group, meter.getCount(), eventName);
        announce(prefix(name, "m1_rate"), group, convertRate(meter.getOneMinuteRate()), unit);
        announce(prefix(name, "m5_rate"), group, convertRate(meter.getFiveMinuteRate()), unit);
        announce(prefix(name, "m15_rate"), group, convertRate(meter.getFifteenMinuteRate()), unit);
        announce(prefix(name, "mean_rate"), group, convertRate(meter.getMeanRate()), unit);
    }

    private void reportHistogram(String name, Histogram histogram) {
        final String sanitizedName = escapeSlashes(name);
        final String group = group(name);
        try {
            final Snapshot snapshot = histogram.getSnapshot();

            announce(prefix(sanitizedName, "count"), group, histogram.getCount(), "");
            announce(prefix(sanitizedName, "max"), group, snapshot.getMax(), "");
            announce(prefix(sanitizedName, "mean"), group, snapshot.getMean(), "");
            announce(prefix(sanitizedName, "min"), group, snapshot.getMin(), "");
            announce(prefix(sanitizedName, "stddev"), group, snapshot.getStdDev(), "");
            announce(prefix(sanitizedName, "p50"), group, snapshot.getMedian(), "");
            announce(prefix(sanitizedName, "p75"), group, snapshot.get75thPercentile(), "");
            announce(prefix(sanitizedName, "p95"), group, snapshot.get95thPercentile(), "");
            announce(prefix(sanitizedName, "p98"), group, snapshot.get98thPercentile(), "");
            announce(prefix(sanitizedName, "p99"), group, snapshot.get99thPercentile(), "");
            announce(prefix(sanitizedName, "p999"), group, snapshot.get999thPercentile(), "");
        } catch (GangliaException e) {
            LOGGER.warn("Unable to report histogram {}", sanitizedName, e);
        }
    }

    private void reportCounter(String name, Counter counter) {
        final String sanitizedName = escapeSlashes(name);
        final String group = group(name);
        try {
            announce(prefix(sanitizedName, "count"), group, counter.getCount(), "");
        } catch (GangliaException e) {
            LOGGER.warn("Unable to report counter {}", name, e);
        }
    }

    private void reportGauge(String name, Gauge gauge) {
        final String sanitizedName = escapeSlashes(name);
        final String group = group(name);
        final Object obj = gauge.getValue();
        final String value = String.valueOf(obj);
        final GMetricType type = detectType(obj);
        try {
            announce(name(prefix, sanitizedName), group, value, type, "");
        } catch (GangliaException e) {
            LOGGER.warn("Unable to report gauge {}", name, e);
        }
    }

    private static final double MIN_VAL = 1E-300;
    private void announce(String name, String group, double value, String units) throws GangliaException {
        final String string = Math.abs(value) < MIN_VAL ? "0" : Double.toString(value);
        announce(name, group, string, GMetricType.DOUBLE, units);
    }

    private void announce(String name, String group, long value, String units) throws GangliaException {
        announce(name, group, Long.toString(value), GMetricType.DOUBLE, units);
    }

    private void announce(String name, String group, String value, GMetricType type, String units) throws GangliaException {
        if (gmetric != null) {
            gmetric.announce(name, value, type, units, GMetricSlope.BOTH, tMax, dMax, group);
        } else {
            for (GMetric gmetric : gmetrics) {
                gmetric.announce(name, value, type, units, GMetricSlope.BOTH, tMax, dMax, group);
            }
        }
    }

    private GMetricType detectType(Object o) {
        if (o instanceof Float) {
            return GMetricType.FLOAT;
        } else if (o instanceof Double) {
            return GMetricType.DOUBLE;
        } else if (o instanceof Byte) {
            return GMetricType.INT8;
        } else if (o instanceof Short) {
            return GMetricType.INT16;
        } else if (o instanceof Integer) {
            return GMetricType.INT32;
        } else if (o instanceof Long) {
            return GMetricType.DOUBLE;
        }
        return GMetricType.STRING;
    }

    private String group(String name) {
        final int i = name.lastIndexOf('.');
        if (i < 0) {
            return "";
        }
        return name.substring(0, i);
    }

    private String prefix(String name, String n) {
        return name(prefix, name, n);
    }

    // ganglia metric names can't contain slashes.
    private String escapeSlashes(String name) {
        return SLASHES.matcher(name).replaceAll("_");
    }
}
