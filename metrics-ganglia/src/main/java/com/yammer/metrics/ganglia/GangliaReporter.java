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

public class GangliaReporter extends AbstractPollingReporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(GangliaReporter.class);

    private final GMetric ganglia;
    private final double durationFactor;
    private final String durationUnit;
    private final double rateFactor;
    private final String rateUnit;
    private final int tMax;
    private final int dMax;

    private GangliaReporter(Builder builder) {
        super(builder.registry, "ganglia-reporter", builder.filter);

        this.ganglia = builder.ganglia;
        this.tMax = builder.tMax;
        this.dMax = builder.dMax;
        this.rateFactor = builder.rateUnit.toSeconds(1);
        this.rateUnit = calculateRateUnit(builder.rateUnit);
        this.durationFactor = 1.0 / builder.durationUnit.toNanos(1);
        this.durationUnit = builder.durationUnit.toString().toLowerCase(Locale.US);
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
            announce(name(name, "max"), group, timer.getMax() * durationFactor, durationUnit);
            announce(name(name, "mean"), group, timer.getMean() * durationFactor, durationUnit);
            announce(name(name, "min"), group, timer.getMin() * durationFactor, durationUnit);
            announce(name(name, "stddev"), group, timer.getStdDev() * durationFactor, durationUnit);

            final Snapshot snapshot = timer.getSnapshot();
            announce(name(name, "p50"), group, snapshot.getMedian() * durationFactor, durationUnit);
            announce(name(name, "p75"), group, snapshot.get75thPercentile() * durationFactor, durationUnit);
            announce(name(name, "p95"), group, snapshot.get95thPercentile() * durationFactor, durationUnit);
            announce(name(name, "p98"), group, snapshot.get98thPercentile() * durationFactor, durationUnit);
            announce(name(name, "p99"), group, snapshot.get99thPercentile() * durationFactor, durationUnit);
            announce(name(name, "p999"), group, snapshot.get999thPercentile() * durationFactor, durationUnit);

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
            announce(name(name, "max"), group, histogram.getMax(), "");
            announce(name(name, "mean"), group, histogram.getMean(), "");
            announce(name(name, "min"), group, histogram.getMin(), "");
            announce(name(name, "stddev"), group, histogram.getStdDev(), "");

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

    public static class Builder {
        private MetricRegistry registry;
        private GMetric ganglia;
        private int tMax;
        private int dMax;
        private TimeUnit rateUnit = TimeUnit.SECONDS;
        private TimeUnit durationUnit = TimeUnit.MILLISECONDS;
        private MetricFilter filter;

        public Builder(GMetric ganglia, MetricRegistry registry, MetricFilter filter) {
            if(ganglia == null) {
                throw new IllegalArgumentException("GMetric cannot be null.");
            }

            this.ganglia = ganglia;
            this.registry = registry;
            this.filter = filter;
        }

        /**
         * Builds a new {@link GangliaReporter}.
         *
         * @return an instance of the configured GangliaReporter
         */
        public GangliaReporter build() {
            return new GangliaReporter(this);
        }

        /**
         * Sets the tMax value
         *
         * @param val integer representing the tMax value
         * @return
         */
        public Builder tMax(int val) {
            tMax = val;
            return this;
        }

        /**
         * Sets the dMax value
         *
         * @param val integer representing the dMax value
         * @return
         */
        public Builder dMax(int val) {
            dMax = val;
            return this;
        }

        /**
         * Sets the rate unit. Default value is TimeUnit.SECONDS
         *
         * @param val the {@link TimeUnit}
         * @return
         */
        public Builder rateUnit(TimeUnit val) {
            rateUnit = val;
            return this;
        }

        /**
         * Sets the duration unit. Default value is TimeUnit.MILLISECONDS
         *
         * @param val the {@link TimeUnit}
         * @return
         */
        public Builder durationUnit(TimeUnit val) {
            durationUnit = val;
            return this;
        }
    }
}
