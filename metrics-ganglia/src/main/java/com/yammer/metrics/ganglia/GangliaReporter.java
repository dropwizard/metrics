package com.yammer.metrics.ganglia;

import com.yammer.metrics.*;
import info.ganglia.gmetric4j.gmetric.GMetric;
import info.ganglia.gmetric4j.gmetric.GMetricSlope;
import info.ganglia.gmetric4j.gmetric.GMetricType;
import info.ganglia.gmetric4j.gmetric.GangliaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import static com.yammer.metrics.MetricRegistry.name;

/**
 * A reporter which announces metric values to a Ganglia cluster.
 *
 * @see <a href="http://ganglia.sourceforge.net/">Ganglia Monitoring System</a>
 */
public class GangliaReporter extends AbstractPollingReporter {
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
     * A builder for {@link CsvReporter} instances. Defaults to using a {@code tmax} of {@code 60},
     * a {@code dmax} of {@code 0}, converting rates to events/second, converting durations to
     * milliseconds, and not filtering metrics.
     */
    public static class Builder {
        private final MetricRegistry registry;
        private int tMax;
        private int dMax;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.tMax = 60;
            this.dMax = 0;
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
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
         * @param ganglia the client to use for announcing metrics
         * @return a {@link GangliaReporter}
         */
        public GangliaReporter build(GMetric ganglia) {
            return new GangliaReporter(registry,
                                       ganglia, tMax, dMax, rateUnit, durationUnit, filter);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(GangliaReporter.class);

    private final GMetric ganglia;
    private final double durationFactor;
    private final String durationUnit;
    private final double rateFactor;
    private final String rateUnit;
    private final int tMax;
    private final int dMax;

    private GangliaReporter(MetricRegistry registry,
                            GMetric ganglia,
                            int tMax,
                            int dMax,
                            TimeUnit rateUnit,
                            TimeUnit durationUnit,
                            MetricFilter filter) {
        super(registry, "ganglia-reporter", filter);
        this.ganglia = ganglia;
        this.tMax = tMax;
        this.dMax = dMax;
        this.rateFactor = rateUnit.toSeconds(1);
        this.rateUnit = calculateRateUnit(rateUnit);
        this.durationFactor = 1.0 / durationUnit.toNanos(1);
        this.durationUnit = durationUnit.toString().toLowerCase(Locale.US);
    }

    private String calculateRateUnit(TimeUnit unit) {
        final String s = unit.toString().toLowerCase(Locale.US);
        return s.substring(0, s.length() - 1);
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
        final String group = group(name);
        try {
            final Snapshot snapshot = timer.getSnapshot();

            announce(name(name, "max"), group, snapshot.getMax() * durationFactor, durationUnit);
            announce(name(name, "mean"), group, snapshot.getMean() * durationFactor, durationUnit);
            announce(name(name, "min"), group, snapshot.getMin() * durationFactor, durationUnit);
            announce(name(name, "stddev"), group, snapshot.getStdDev() * durationFactor, durationUnit);

            announce(name(name, "p50"), group, snapshot.getMedian() * durationFactor, durationUnit);
            announce(name(name, "p75"),
                     group,
                     snapshot.get75thPercentile() * durationFactor,
                     durationUnit);
            announce(name(name, "p95"),
                     group,
                     snapshot.get95thPercentile() * durationFactor,
                     durationUnit);
            announce(name(name, "p98"),
                     group,
                     snapshot.get98thPercentile() * durationFactor,
                     durationUnit);
            announce(name(name, "p99"),
                     group,
                     snapshot.get99thPercentile() * durationFactor,
                     durationUnit);
            announce(name(name, "p999"),
                     group,
                     snapshot.get999thPercentile() * durationFactor,
                     durationUnit);

            reportMetered(name, timer, group, "calls");
        } catch (GangliaException e) {
            LOGGER.warn("Unable to report timer {}", name, e);
        }
    }

    private void reportMeter(String name, Meter meter) {
        final String group = group(name);
        try {
            reportMetered(name, meter, group, "events");
        } catch (GangliaException e) {
            LOGGER.warn("Unable to report meter {}", name, e);
        }
    }

    private void reportMetered(String name, Metered meter, String group, String eventName) throws GangliaException {
        final String unit = eventName + '/' + rateUnit;
        announce(name(name, "count"), group, meter.getCount(), eventName);
        announce(name(name, "m1_rate"), group, meter.getOneMinuteRate() * rateFactor, unit);
        announce(name(name, "m5_rate"), group, meter.getFiveMinuteRate() * rateFactor, unit);
        announce(name(name, "m15_rate"), group, meter.getFifteenMinuteRate() * rateFactor, unit);
        announce(name(name, "mean_rate"), group, meter.getMeanRate() * rateFactor, unit);
    }

    private void reportHistogram(String name, Histogram histogram) {
        final String group = group(name);
        try {
            final Snapshot snapshot = histogram.getSnapshot();

            announce(name(name, "count"), group, histogram.getCount(), "");
            announce(name(name, "max"), group, snapshot.getMax(), "");
            announce(name(name, "mean"), group, snapshot.getMean(), "");
            announce(name(name, "min"), group, snapshot.getMin(), "");
            announce(name(name, "stddev"), group, snapshot.getStdDev(), "");
            announce(name(name, "p50"), group, snapshot.getMedian(), "");
            announce(name(name, "p75"), group, snapshot.get75thPercentile(), "");
            announce(name(name, "p95"), group, snapshot.get95thPercentile(), "");
            announce(name(name, "p98"), group, snapshot.get98thPercentile(), "");
            announce(name(name, "p99"), group, snapshot.get99thPercentile(), "");
            announce(name(name, "p999"), group, snapshot.get999thPercentile(), "");
        } catch (GangliaException e) {
            LOGGER.warn("Unable to report histogram {}", name, e);
        }
    }

    private void reportCounter(String name, Counter counter) {
        final String group = group(name);
        try {
            announce(name(name, "count"), group, counter.getCount(), "");
        } catch (GangliaException e) {
            LOGGER.warn("Unable to report counter {}", name, e);
        }
    }

    private void reportGauge(String name, Gauge gauge) {
        final String group = group(name);
        final Object obj = gauge.getValue();
        try {
            ganglia.announce(name, String.valueOf(obj), detectType(obj), "",
                             GMetricSlope.BOTH, tMax, dMax, group);
        } catch (GangliaException e) {
            LOGGER.warn("Unable to report gauge {}", name, e);
        }
    }

    private void announce(String name, String group, double value, String units) throws GangliaException {
        ganglia.announce(name,
                         Double.toString(value),
                         GMetricType.DOUBLE,
                         units,
                         GMetricSlope.BOTH,
                         tMax,
                         dMax,
                         group);
    }

    private void announce(String name, String group, long value, String units) throws GangliaException {
        final String v = Long.toString(value);
        ganglia.announce(name,
                         v,
                         GMetricType.DOUBLE,
                         units,
                         GMetricSlope.BOTH,
                         tMax,
                         dMax,
                         group);
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
}
