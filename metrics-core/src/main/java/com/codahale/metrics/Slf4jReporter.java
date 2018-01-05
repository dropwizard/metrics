package com.codahale.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
            return new Slf4jReporter(registry, loggerProxy, marker, prefix, rateUnit, durationUnit, filter, executor, shutdownExecutorOnStop);
        }
    }

    private final LoggerProxy loggerProxy;
    private final Marker marker;
    private final MetricName prefix;

    private Slf4jReporter(MetricRegistry registry,
                          LoggerProxy loggerProxy,
                          Marker marker,
                          String prefix,
                          TimeUnit rateUnit,
                          TimeUnit durationUnit,
                          MetricFilter filter,
                          ScheduledExecutorService executor,
                          boolean shutdownExecutorOnStop) {
        super(registry, "logger-reporter", filter, rateUnit, durationUnit, executor, shutdownExecutorOnStop);
        this.loggerProxy = loggerProxy;
        this.marker = marker;
        this.prefix = MetricName.build(prefix);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void report(SortedMap<MetricName, Gauge> gauges,
                       SortedMap<MetricName, Counter> counters,
                       SortedMap<MetricName, Histogram> histograms,
                       SortedMap<MetricName, Meter> meters,
                       SortedMap<MetricName, Timer> timers) {
        if (loggerProxy.isEnabled(marker)) {
            for (Entry<MetricName, Gauge> entry : gauges.entrySet()) {
                logGauge(entry.getKey(), entry.getValue());
            }

            for (Entry<MetricName, Counter> entry : counters.entrySet()) {
                logCounter(entry.getKey(), entry.getValue());
            }

            for (Entry<MetricName, Histogram> entry : histograms.entrySet()) {
                logHistogram(entry.getKey(), entry.getValue());
            }

            for (Entry<MetricName, Meter> entry : meters.entrySet()) {
                logMeter(entry.getKey(), entry.getValue());
            }

            for (Entry<MetricName, Timer> entry : timers.entrySet()) {
                logTimer(entry.getKey(), entry.getValue());
            }
        }
    }

    private void logTimer(MetricName name, Timer timer) {
        final Snapshot snapshot = timer.getSnapshot();
        loggerProxy.log(marker,
                "type={}, name={}, count={}, min={}, max={}, mean={}, stddev={}, median={}, " +
                        "p75={}, p95={}, p98={}, p99={}, p999={}, mean_rate={}, m1={}, m5={}, " +
                        "m15={}, rate_unit={}, duration_unit={}",
                "TIMER",
                prefix(name),
                timer.getCount(),
                convertDuration(snapshot.getMin()),
                convertDuration(snapshot.getMax()),
                convertDuration(snapshot.getMean()),
                convertDuration(snapshot.getStdDev()),
                convertDuration(snapshot.getMedian()),
                convertDuration(snapshot.get75thPercentile()),
                convertDuration(snapshot.get95thPercentile()),
                convertDuration(snapshot.get98thPercentile()),
                convertDuration(snapshot.get99thPercentile()),
                convertDuration(snapshot.get999thPercentile()),
                convertRate(timer.getMeanRate()),
                convertRate(timer.getOneMinuteRate()),
                convertRate(timer.getFiveMinuteRate()),
                convertRate(timer.getFifteenMinuteRate()),
                getRateUnit(),
                getDurationUnit());
    }

    private void logMeter(MetricName name, Meter meter) {
        loggerProxy.log(marker,
                "type={}, name={}, count={}, mean_rate={}, m1={}, m5={}, m15={}, rate_unit={}",
                "METER",
                prefix(name),
                meter.getCount(),
                convertRate(meter.getMeanRate()),
                convertRate(meter.getOneMinuteRate()),
                convertRate(meter.getFiveMinuteRate()),
                convertRate(meter.getFifteenMinuteRate()),
                getRateUnit());
    }

    private void logHistogram(MetricName name, Histogram histogram) {
        final Snapshot snapshot = histogram.getSnapshot();
        loggerProxy.log(marker,
                "type={}, name={}, count={}, min={}, max={}, mean={}, stddev={}, " +
                        "median={}, p75={}, p95={}, p98={}, p99={}, p999={}",
                "HISTOGRAM",
                prefix(name),
                histogram.getCount(),
                snapshot.getMin(),
                snapshot.getMax(),
                snapshot.getMean(),
                snapshot.getStdDev(),
                snapshot.getMedian(),
                snapshot.get75thPercentile(),
                snapshot.get95thPercentile(),
                snapshot.get98thPercentile(),
                snapshot.get99thPercentile(),
                snapshot.get999thPercentile());
    }

    private void logCounter(MetricName name, Counter counter) {
        loggerProxy.log(marker, "type={}, name={}, count={}", "COUNTER", prefix(name), counter.getCount());
    }

    private void logGauge(MetricName name, Gauge<?> gauge) {
        loggerProxy.log(marker, "type={}, name={}, value={}", "GAUGE", prefix(name), gauge.getValue());
    }

    @Override
    protected String getRateUnit() {
        return "events/" + super.getRateUnit();
    }

    private MetricName prefix(MetricName metricName) {
        return MetricName.join(prefix, metricName);
    }

    /* private class to allow logger configuration */
    static abstract class LoggerProxy {
        protected final Logger logger;

        public LoggerProxy(Logger logger) {
            this.logger = logger;
        }

        abstract void log(Marker marker, String format, Object... arguments);

        abstract boolean isEnabled(Marker marker);
    }

    /* private class to allow logger configuration */
    private static class DebugLoggerProxy extends LoggerProxy {
        public DebugLoggerProxy(Logger logger) {
            super(logger);
        }

        @Override
        public void log(Marker marker, String format, Object... arguments) {
            logger.debug(marker, format, arguments);
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
        public void log(Marker marker, String format, Object... arguments) {
            logger.trace(marker, format, arguments);
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
        public void log(Marker marker, String format, Object... arguments) {
            logger.info(marker, format, arguments);
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
        public void log(Marker marker, String format, Object... arguments) {
            logger.warn(marker, format, arguments);
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
        public void log(Marker marker, String format, Object... arguments) {
            logger.error(marker, format, arguments);
        }

        @Override
        public boolean isEnabled(Marker marker) {
            return logger.isErrorEnabled(marker);
        }
    }

}
