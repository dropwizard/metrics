package com.yammer.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

/**
 * A reporter which creates a comma-separated values file of the measurements for each metric.
 */
public class CsvReporter extends AbstractPollingReporter {
    /**
     * Returns a new {@link Builder} for {@link CsvReporter}.
     *
     * @param registry the registry to report
     * @return a {@link Builder} instance for a {@link CsvReporter}
     */
    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }

    /**
     * A builder for {@link CsvReporter} instances. Defaults to using the default locale, converting
     * rates to events/second, converting durations to milliseconds, and not filtering metrics.
     */
    public static class Builder {
        private final MetricRegistry registry;
        private Locale locale;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private Clock clock;
        private MetricFilter filter;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.locale = Locale.getDefault();
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.clock = Clock.defaultClock();
            this.filter = MetricFilter.ALL;
        }

        /**
         * Format numbers for the given {@link Locale}.
         *
         * @param locale a {@link Locale}
         * @return {@code this}
         */
        public Builder formatFor(Locale locale) {
            this.locale = locale;
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
         * Builds a {@link CsvReporter} with the given properties, writing {@code .csv} files to the
         * given directory.
         *
         * @param directory the directory in which the {@code .csv} files will be created
         * @return a {@link CsvReporter}
         */
        public CsvReporter build(File directory) {
            return new CsvReporter(registry,
                                   directory,
                                   locale,
                                   rateUnit,
                                   durationUnit,
                                   clock,
                                   filter);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(CsvReporter.class);
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private final File directory;
    private final Locale locale;
    private final Clock clock;
    private final double durationFactor;
    private final String durationUnit;
    private final double rateFactor;
    private final String rateUnit;

    private CsvReporter(MetricRegistry registry,
                        File directory,
                        Locale locale,
                        TimeUnit rateUnit,
                        TimeUnit durationUnit,
                        Clock clock,
                        MetricFilter filter) {
        super(registry, "csv-reporter", filter);
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
                final PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file,true), UTF_8));
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
