package com.yammer.metrics;

import org.slf4j.Logger;
import org.slf4j.Marker;

import java.util.Map.Entry;
import java.util.SortedMap;

/**
 * Metrics reporter class for logging metrics values to a SLF4J {@link Logger} periodically, similar
 * to how {@link ConsoleReporter} or {@link CsvReporter} function, but using the SLF4J framework
 * instead. It also supports specifying a {@link Marker} instance that can be used by custom
 * appenders and filters for the bound logging toolkit to further process metrics reports.
 */
public class LoggerReporter extends AbstractPollingReporter {

    private Logger logger;
    private Marker marker;

    /**
     * Construct a new SLF4J reporter.
     *
     * @param registry Metrics registry to report from.
     * @param logger   SLF4J {@link Logger} instance to send metrics reports to
     */
    public LoggerReporter(MetricRegistry registry, Logger logger) {
        this(registry, logger, null);
    }

    /**
     * Construct a new SLF4J reporter.
     *
     * @param registry        Metrics registry to report from.
     * @param logger SLF4J {@link Logger} instance to send metrics reports to
     * @param marker SLF4J {@link Marker} instance to log with metrics class, or null if
     *                        none.
     */
    public LoggerReporter(MetricRegistry registry, Logger logger, Marker marker) {
        super(registry, "logger-reporter");
        this.logger = logger;
        this.marker = marker;
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {
        for (Entry<String, Gauge> entry : gauges.entrySet()) {
            logGauge(entry.getKey(), entry.getValue());
        }

        for (Entry<String, Counter> entry : counters.entrySet()) {
            logCounter(entry.getKey(), entry.getValue());
        }

        for (Entry<String, Histogram> entry : histograms.entrySet()) {
            logHistogram(entry.getKey(), entry.getValue());
        }

        for (Entry<String, Meter> entry : meters.entrySet()) {
            logMeter(entry.getKey(), entry.getValue());
        }

        for (Entry<String, Timer> entry : timers.entrySet()) {
            logTimer(entry.getKey(), entry.getValue());
        }
    }

    private void logTimer(String name, Timer timer) {
        final Snapshot snapshot = timer.getSnapshot();
        logger.info(marker,
                    "type=TIMER, name={}, count={}, min={}, max={}, mean={}, stddev={}, median={}, p75={}, p95={}, p98={}, p999={}, mean_rate={}, m1={}, m5={}, m15={}",
                    name,
                    timer.getCount(),
                    timer.getMin(),
                    timer.getMax(),
                    timer.getMean(),
                    timer.getStdDev(),
                    snapshot.getMedian(),
                    snapshot.get75thPercentile(),
                    snapshot.get95thPercentile(),
                    snapshot.get98thPercentile(),
                    snapshot.get99thPercentile(),
                    snapshot.get999thPercentile(),
                    timer.getMeanRate(),
                    timer.getOneMinuteRate(),
                    timer.getFiveMinuteRate(),
                    timer.getFifteenMinuteRate());
    }

    private void logMeter(String name, Meter meter) {
        logger.info(marker,
                     "type=METER, name={}, count={}, mean_rate={}, m1={}, m5={}, m15={}",
                     name,
                     meter.getCount(),
                     meter.getMeanRate(),
                     meter.getOneMinuteRate(),
                     meter.getFiveMinuteRate(),
                     meter.getFifteenMinuteRate());
    }

    private void logHistogram(String name, Histogram histogram) {
        final Snapshot snapshot = histogram.getSnapshot();
        logger.info(marker,
                    "type=HISTOGRAM, name={}, count={}, min={}, max={}, mean={}, stddev={}, median={}, p75={}, p95={}, p98={}, p999={}",
                    name,
                    histogram.getCount(),
                    histogram.getMin(),
                    histogram.getMax(),
                    histogram.getMean(),
                    histogram.getStdDev(),
                    snapshot.getMedian(),
                    snapshot.get75thPercentile(),
                    snapshot.get95thPercentile(),
                    snapshot.get98thPercentile(),
                    snapshot.get99thPercentile(),
                    snapshot.get999thPercentile());
    }

    private void logCounter(String name, Counter counter) {
        logger.info(marker, "type=COUNTER, name={}, count={} ", name, counter.getCount());
    }

    private void logGauge(String name, Gauge gauge) {
        logger.info(marker, "type=GAUGE, name={}, value={}", name, gauge.getValue());
    }
}
