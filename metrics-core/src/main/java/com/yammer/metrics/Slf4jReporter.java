package com.yammer.metrics;

import org.slf4j.Logger;
import org.slf4j.Marker;

import java.util.Locale;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

/**
 * Metrics reporter class for logging metrics values to a SLF4J {@link Logger} periodically, similar
 * to how {@link ConsoleReporter} or {@link CsvReporter} function, but using the SLF4J framework
 * instead. It also supports specifying a {@link Marker} instance that can be used by custom
 * appenders and filters for the bound logging toolkit to further process metrics reports.
 */
public class Slf4jReporter extends AbstractPollingReporter {
    private final Logger logger;
    private final Marker marker;
    private final double durationFactor;
    private final String durationUnit;
    private final double rateFactor;
    private final String rateUnit;

    /**
     * Construct a new SLF4J reporter.
     *
     * @param registry     Metrics registry to report from.
     * @param logger       SLF4J {@link Logger} instance to send metrics reports to
     * @param marker       SLF4J {@link Marker} instance to log with metrics class, or null if
     *                     none.
     * @param rateUnit     the unit to which rates will be converted
     * @param durationUnit the unit to which durations will be converted
     * @param filter       the metric filter to match
     */
    public Slf4jReporter(MetricRegistry registry,
                         Logger logger,
                         Marker marker,
                         TimeUnit rateUnit,
                         TimeUnit durationUnit,
                         MetricFilter filter) {
        super(registry, "logger-reporter", filter);
        this.logger = logger;
        this.marker = marker;
        this.rateFactor = rateUnit.toSeconds(1);
        this.rateUnit = "events/" + calculateRateUnit(rateUnit);
        this.durationFactor = 1.0 / durationUnit.toNanos(1);
        this.durationUnit = durationUnit.toString().toLowerCase(Locale.US);
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
                    "type=TIMER, name={}, count={}, min={}, max={}, mean={}, stddev={}, median={}, " +
                            "p75={}, p95={}, p98={}, p999={}, mean_rate={}, m1={}, m5={}, m15={}, " +
                            "rate_unit={}, duration_unit={}",
                    name,
                    timer.getCount(),
                    timer.getMin() * durationFactor,
                    timer.getMax() * durationFactor,
                    timer.getMean() * durationFactor,
                    timer.getStdDev() * durationFactor,
                    snapshot.getMedian() * durationFactor,
                    snapshot.get75thPercentile() * durationFactor,
                    snapshot.get95thPercentile() * durationFactor,
                    snapshot.get98thPercentile() * durationFactor,
                    snapshot.get99thPercentile() * durationFactor,
                    snapshot.get999thPercentile() * durationFactor,
                    timer.getMeanRate() * rateFactor,
                    timer.getOneMinuteRate() * rateFactor,
                    timer.getFiveMinuteRate() * rateFactor,
                    timer.getFifteenMinuteRate() * rateFactor,
                    rateUnit,
                    durationUnit);
    }

    private void logMeter(String name, Meter meter) {
        logger.info(marker,
                    "type=METER, name={}, count={}, mean_rate={}, m1={}, m5={}, m15={}, rate_unit={}",
                    name,
                    meter.getCount(),
                    meter.getMeanRate() * rateFactor,
                    meter.getOneMinuteRate() * rateFactor,
                    meter.getFiveMinuteRate() * rateFactor,
                    meter.getFifteenMinuteRate() * rateFactor,
                    rateUnit);
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
        logger.info(marker, "type=COUNTER, name={}, count={}", name, counter.getCount());
    }

    private void logGauge(String name, Gauge gauge) {
        logger.info(marker, "type=GAUGE, name={}, value={}", name, gauge.getValue());
    }

    private String calculateRateUnit(TimeUnit unit) {
        final String s = unit.toString().toLowerCase(Locale.US);
        return s.substring(0, s.length() - 1);
    }
}
