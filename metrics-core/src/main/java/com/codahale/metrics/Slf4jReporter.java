package com.codahale.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import java.util.Collections;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

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
 * A reporter class for logging metrics values to a SLF4J {@link Logger} periodically, similar to
 * {@link ConsoleReporter} or {@link CsvReporter}, but using the SLF4J framework instead. It also
 * supports specifying a {@link Marker} instance that can be used by custom appenders and filters
 * for the bound logging toolkit to further process metrics reports.
 */
public class Slf4jReporter extends ScheduledReporter {
    /**
     * Returns a new {@link Builder} for {@link Slf4jReporter}.
     *
     * @param registry the registry to report
     * @return a {@link Builder} instance for a {@link Slf4jReporter}
     */
    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }

    public enum LoggingLevel { TRACE, DEBUG, INFO, WARN, ERROR }

    /**
     * A builder for {@link Slf4jReporter} instances. Defaults to logging to {@code metrics}, not
     * using a marker, converting rates to events/second, converting durations to milliseconds, and
     * not filtering metrics.
     */
    public static class Builder {
        private final MetricRegistry registry;
        private Logger logger;
        private LoggingLevel loggingLevel;
        private Marker marker;
        private String prefix;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;
        private ScheduledExecutorService executor;
        private boolean shutdownExecutorOnStop;
        private Set<MetricAttribute> disabledMetricAttributes;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.logger = LoggerFactory.getLogger("metrics");
            this.marker = null;
            this.prefix = "";
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
            this.loggingLevel = LoggingLevel.INFO;
            this.executor = null;
            this.shutdownExecutorOnStop = true;
            this.disabledMetricAttributes = Collections.emptySet();
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
         * Log metrics to the given logger.
         *
         * @param logger an SLF4J {@link Logger}
         * @return {@code this}
         */
        public Builder outputTo(Logger logger) {
            this.logger = logger;
            return this;
        }

        /**
         * Mark all logged metrics with the given marker.
         *
         * @param marker an SLF4J {@link Marker}
         * @return {@code this}
         */
        public Builder markWith(Marker marker) {
            this.marker = marker;
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
         * Use Logging Level when reporting.
         *
         * @param loggingLevel a (@link Slf4jReporter.LoggingLevel}
         * @return {@code this}
         */
        public Builder withLoggingLevel(LoggingLevel loggingLevel) {
            this.loggingLevel = loggingLevel;
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
         * Builds a {@link Slf4jReporter} with the given properties.
         *
         * @return a {@link Slf4jReporter}
         */
        public Slf4jReporter build() {
            LoggerProxy loggerProxy;
            switch (loggingLevel) {
                case TRACE:
                    loggerProxy = new TraceLoggerProxy(logger);
                    break;
                case INFO:
                    loggerProxy = new InfoLoggerProxy(logger);
                    break;
                case WARN:
                    loggerProxy = new WarnLoggerProxy(logger);
                    break;
                case ERROR:
                    loggerProxy = new ErrorLoggerProxy(logger);
                    break;
                default:
                case DEBUG:
                    loggerProxy = new DebugLoggerProxy(logger);
                    break;
            }
            return new Slf4jReporter(registry, loggerProxy, marker, prefix, rateUnit, durationUnit, filter, executor, shutdownExecutorOnStop, disabledMetricAttributes);
        }
    }

    private final LoggerProxy loggerProxy;
    private final Marker marker;
    private final String prefix;

    private Slf4jReporter(MetricRegistry registry,
                          LoggerProxy loggerProxy,
                          Marker marker,
                          String prefix,
                          TimeUnit rateUnit,
                          TimeUnit durationUnit,
                          MetricFilter filter,
                          ScheduledExecutorService executor,
                          boolean shutdownExecutorOnStop,
                          Set<MetricAttribute> disabledMetricAttributes) {
        super(registry, "logger-reporter", filter, rateUnit, durationUnit, executor, shutdownExecutorOnStop, disabledMetricAttributes);
        this.loggerProxy = loggerProxy;
        this.marker = marker;
        this.prefix = prefix;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {
        if (loggerProxy.isEnabled(marker)) {
            StringBuilder b = new StringBuilder();
            for (Entry<String, Gauge> entry : gauges.entrySet()) {
                logGauge(b, entry.getKey(), entry.getValue());
            }

            for (Entry<String, Counter> entry : counters.entrySet()) {
                logCounter(b, entry.getKey(), entry.getValue());
            }

            for (Entry<String, Histogram> entry : histograms.entrySet()) {
                logHistogram(b, entry.getKey(), entry.getValue());
            }

            for (Entry<String, Meter> entry : meters.entrySet()) {
                logMeter(b, entry.getKey(), entry.getValue());
            }

            for (Entry<String, Timer> entry : timers.entrySet()) {
                logTimer(b, entry.getKey(), entry.getValue());
            }
        }
    }

    private void logTimer(StringBuilder b, String name, Timer timer) {
        final Snapshot snapshot = timer.getSnapshot();
        b.setLength(0);
        b.append("type=TIMER");
        append(b, "name", prefix(name));
        appendCountIfEnabled(b, timer);
        appendLongDurationIfEnabled(b, MIN, snapshot::getMin);
        appendLongDurationIfEnabled(b, MAX, snapshot::getMax);
        appendDoubleDurationIfEnabled(b, MEAN, snapshot::getMean);
        appendDoubleDurationIfEnabled(b, STDDEV, snapshot::getStdDev);
        appendDoubleDurationIfEnabled(b, P50, snapshot::getMedian);
        appendDoubleDurationIfEnabled(b, P75, snapshot::get75thPercentile);
        appendDoubleDurationIfEnabled(b, P95, snapshot::get95thPercentile);
        appendDoubleDurationIfEnabled(b, P98, snapshot::get98thPercentile);
        appendDoubleDurationIfEnabled(b, P99, snapshot::get99thPercentile);
        appendDoubleDurationIfEnabled(b, P999, snapshot::get999thPercentile);
        appendMetered(b, timer);
        append(b, "rate_unit", getRateUnit());
        append(b, "duration_unit", getDurationUnit());
        loggerProxy.log(marker, b.toString());
    }

    private void logMeter(StringBuilder b, String name, Meter meter) {
        b.setLength(0);
        b.append("type=METER");
        append(b, "name", prefix(name));
        appendCountIfEnabled(b, meter);
        appendMetered(b, meter);
        append(b, "rate_unit", getRateUnit());
        loggerProxy.log(marker, b.toString());
    }

    private void logHistogram(StringBuilder b, String name, Histogram histogram) {
        final Snapshot snapshot = histogram.getSnapshot();
        b.setLength(0);
        b.append("type=HISTOGRAM");
        append(b, "name", prefix(name));
        appendCountIfEnabled(b, histogram);
        appendLongIfEnabled(b, MIN, snapshot::getMin);
        appendLongIfEnabled(b, MAX, snapshot::getMax);
        appendDoubleIfEnabled(b, MEAN, snapshot::getMean);
        appendDoubleIfEnabled(b, STDDEV, snapshot::getStdDev);
        appendDoubleIfEnabled(b, P50, snapshot::getMedian);
        appendDoubleIfEnabled(b, P75, snapshot::get75thPercentile);
        appendDoubleIfEnabled(b, P95, snapshot::get95thPercentile);
        appendDoubleIfEnabled(b, P98, snapshot::get98thPercentile);
        appendDoubleIfEnabled(b, P99, snapshot::get99thPercentile);
        appendDoubleIfEnabled(b, P999, snapshot::get999thPercentile);
        loggerProxy.log(marker, b.toString());
    }

    private void logCounter(StringBuilder b, String name, Counter counter) {
        b.setLength(0);
        b.append("type=COUNTER");
        append(b, "name", prefix(name));
        append(b, COUNT.getCode(), counter.getCount());
        loggerProxy.log(marker, b.toString());
    }

    private void logGauge(StringBuilder b, String name, Gauge<?> gauge) {
        b.setLength(0);
        b.append("type=GAUGE");
        append(b, "name", prefix(name));
        append(b, "value", gauge.getValue());
        loggerProxy.log(marker, b.toString());
    }

    private void appendLongDurationIfEnabled(StringBuilder b, MetricAttribute metricAttribute, Supplier<Long> durationSupplier) {
        if (!getDisabledMetricAttributes().contains(metricAttribute)) {
            append(b, metricAttribute.getCode(), convertDuration(durationSupplier.get()));
        }
    }

    private void appendDoubleDurationIfEnabled(StringBuilder b, MetricAttribute metricAttribute, Supplier<Double> durationSupplier) {
        if (!getDisabledMetricAttributes().contains(metricAttribute)) {
            append(b, metricAttribute.getCode(), convertDuration(durationSupplier.get()));
        }
    }

    private void appendLongIfEnabled(StringBuilder b, MetricAttribute metricAttribute, Supplier<Long> valueSupplier) {
        if (!getDisabledMetricAttributes().contains(metricAttribute)) {
            append(b, metricAttribute.getCode(), valueSupplier.get());
        }
    }

    private void appendDoubleIfEnabled(StringBuilder b, MetricAttribute metricAttribute, Supplier<Double> valueSupplier) {
        if (!getDisabledMetricAttributes().contains(metricAttribute)) {
            append(b, metricAttribute.getCode(), valueSupplier.get());
        }
    }

    private void appendCountIfEnabled(StringBuilder b, Counting counting) {
        if (!getDisabledMetricAttributes().contains(COUNT)) {
            append(b, COUNT.getCode(), counting.getCount());
        }
    }

    private void appendMetered(StringBuilder b, Metered meter) {
        appendRateIfEnabled(b, M1_RATE, meter::getOneMinuteRate);
        appendRateIfEnabled(b, M5_RATE, meter::getFiveMinuteRate);
        appendRateIfEnabled(b, M15_RATE,  meter::getFifteenMinuteRate);
        appendRateIfEnabled(b, MEAN_RATE,  meter::getMeanRate);
    }

    private void appendRateIfEnabled(StringBuilder b, MetricAttribute metricAttribute, Supplier<Double> rateSupplier) {
        if (!getDisabledMetricAttributes().contains(metricAttribute)) {
            append(b, metricAttribute.getCode(), convertRate(rateSupplier.get()));
        }
    }

    private void append(StringBuilder b, String key, long value) {
        b.append(", ").append(key).append('=').append(value);
    }

    private void append(StringBuilder b, String key, double value) {
        b.append(", ").append(key).append('=').append(value);
    }

    private void append(StringBuilder b, String key, String value) {
        b.append(", ").append(key).append('=').append(value);
    }

    private void append(StringBuilder b, String key, Object value) {
        b.append(", ").append(key).append('=').append(value);
    }

    @Override
    protected String getRateUnit() {
        return "events/" + super.getRateUnit();
    }

    private String prefix(String... components) {
        return MetricRegistry.name(prefix, components);
    }

    /* private class to allow logger configuration */
    static abstract class LoggerProxy {
        protected final Logger logger;

        public LoggerProxy(Logger logger) {
            this.logger = logger;
        }

        abstract void log(Marker marker, String format);

        abstract boolean isEnabled(Marker marker);
    }

    /* private class to allow logger configuration */
    private static class DebugLoggerProxy extends LoggerProxy {
        public DebugLoggerProxy(Logger logger) {
            super(logger);
        }

        @Override
        public void log(Marker marker, String format) {
            logger.debug(marker, format);
        }

        @Override
        public boolean isEnabled(Marker marker) {
            return logger.isDebugEnabled(marker);
        }
    }

    /* private class to allow logger configuration */
    private static class TraceLoggerProxy extends LoggerProxy {
        public TraceLoggerProxy(Logger logger) {
            super(logger);
        }

        @Override
        public void log(Marker marker, String format) {
            logger.trace(marker, format);
        }

        @Override
        public boolean isEnabled(Marker marker) {
            return logger.isTraceEnabled(marker);
        }
    }

    /* private class to allow logger configuration */
    private static class InfoLoggerProxy extends LoggerProxy {
        public InfoLoggerProxy(Logger logger) {
            super(logger);
        }

        @Override
        public void log(Marker marker, String format) {
            logger.info(marker, format);
        }

        @Override
        public boolean isEnabled(Marker marker) {
            return logger.isInfoEnabled(marker);
        }
    }

    /* private class to allow logger configuration */
    private static class WarnLoggerProxy extends LoggerProxy {
        public WarnLoggerProxy(Logger logger) {
            super(logger);
        }

        @Override
        public void log(Marker marker, String format) {
            logger.warn(marker, format);
        }

        @Override
        public boolean isEnabled(Marker marker) {
            return logger.isWarnEnabled(marker);
        }
    }

    /* private class to allow logger configuration */
    private static class ErrorLoggerProxy extends LoggerProxy {
        public ErrorLoggerProxy(Logger logger) {
            super(logger);
        }

        @Override
        public void log(Marker marker, String format) {
            logger.error(marker, format);
        }

        @Override
        public boolean isEnabled(Marker marker) {
            return logger.isErrorEnabled(marker);
        }
    }

}
