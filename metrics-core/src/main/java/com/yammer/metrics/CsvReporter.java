package com.yammer.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

// TODO: 3/12/13 <coda> -- test and document CsvReporter

public class CsvReporter extends AbstractPollingReporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CsvReporter.class);

    private final File directory;
    private final Locale locale;
    private final Clock clock;
    private final double durationFactor;
    private final String durationUnit;
    private final double rateFactor;
    private final String rateUnit;

    /**
     * Creates a new {@link CsvReporter} instance.
     *
     * @param registry the {@link MetricRegistry} containing the metrics this reporter will report
     * @param directory the directory in which CSV files will be created
     * @param locale    the locale to use for formatting
     */
    public CsvReporter(MetricRegistry registry,
                       File directory,
                       Locale locale,
                       TimeUnit rateUnit,
                       TimeUnit durationUnit,
                       Clock clock) {
        super(registry, "csv-reporter");
        this.directory = directory;
        this.locale = locale;
        this.clock = clock;
        this.rateFactor = rateUnit.toSeconds(1);
        this.rateUnit = calculateRateUnit(rateUnit);
        this.durationFactor = 1.0 / durationUnit.toNanos(1);
        this.durationUnit = durationUnit.toString().toLowerCase(Locale.US);
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {
        final long timestamp = TimeUnit.MILLISECONDS.toSeconds(clock.getTime());

        for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
            reportGauge(timestamp, entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Counter> entry : counters.entrySet()) {
            reportCounter(timestamp, entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
            reportHistogram(timestamp, entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Meter> entry : meters.entrySet()) {
            reportMeter(timestamp, entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Timer> entry : timers.entrySet()) {
            reportTimer(timestamp, entry.getKey(), entry.getValue());
        }
    }

    private void reportTimer(long timestamp, String name, Timer timer) {
        final Snapshot snapshot = timer.getSnapshot();

        report(timestamp,
               name,
               "count,max,mean,min,stddev,p50,p75,p95,p98,p99,p999,mean_rate,m1_rate,m5_rate,m15_rate,rate_unit,duration_unit",
               "%d,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,calls/%s,%s",
               timer.getCount(),
               timer.getMax() * durationFactor,
               timer.getMean() * durationFactor,
               timer.getMin() * durationFactor,
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

    private void reportMeter(long timestamp, String name, Meter meter) {
        report(timestamp,
               name,
               "count,mean_rate,m1_rate,m5_rate,m15_rate,rate_unit",
               "%d,%f,%f,%f,%f,events/%s",
               meter.getCount(),
               meter.getMeanRate() * rateFactor,
               meter.getOneMinuteRate() * rateFactor,
               meter.getFiveMinuteRate() * rateFactor,
               meter.getFifteenMinuteRate() * rateFactor,
               rateUnit);
    }

    private void reportHistogram(long timestamp, String name, Histogram histogram) {
        final Snapshot snapshot = histogram.getSnapshot();

        report(timestamp,
               name,
               "count,max,mean,min,stddev,p50,p75,p95,p98,p99,p999",
               "%d,%d,%f,%d,%f,%f,%f,%f,%f,%f,%f",
               histogram.getCount(),
               histogram.getMax(),
               histogram.getMean(),
               histogram.getMin(),
               histogram.getStdDev(),
               snapshot.getMedian(),
               snapshot.get75thPercentile(),
               snapshot.get95thPercentile(),
               snapshot.get98thPercentile(),
               snapshot.get99thPercentile(),
               snapshot.get999thPercentile());
    }

    private void reportCounter(long timestamp, String name, Counter counter) {
        report(timestamp, name, "count", "%d", counter.getCount());
    }

    private void reportGauge(long timestamp, String name, Gauge gauge) {
        report(timestamp, name, "value", "%s", gauge.getValue());
    }

    private void report(long timestamp, String name, String header, String line, Object... values) {
        try {
            final File file = new File(directory, sanitize(name) + ".csv");
            final boolean fileAlreadyExists = file.exists();
            if (fileAlreadyExists || file.createNewFile()) {
                final PrintWriter out = new PrintWriter(new FileOutputStream(file, true));
                try {
                    if (!fileAlreadyExists) {
                        out.println("t," + header);
                    }
                    out.printf(locale, String.format(locale, "%d,%s%n", timestamp, line), values);
                } finally {
                    out.close();
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Error writing to {}", name, e);
        }
    }

    private String calculateRateUnit(TimeUnit unit) {
        final String s = unit.toString().toLowerCase(Locale.US);
        return s.substring(0, s.length() - 1);
    }

    protected String sanitize(String name) {
        return name;
    }
}
