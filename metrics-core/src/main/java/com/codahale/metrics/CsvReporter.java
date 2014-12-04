package com.codahale.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

/**
 * A reporter which creates a comma-separated values file of the measurements for each metric.
 */
public class CsvReporter extends ScheduledReporter {
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
        private List<Quantile> quantiles;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.locale = Locale.getDefault();
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.clock = Clock.defaultClock();
            this.filter = MetricFilter.ALL;
            this.quantiles = Quantiles.defaultQuantiles();
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
         * Add custom quantile to the set of reported quantiles.
         *
         * @param name Name of the quantile (i.e. p99999)
         * @param value Value of the quantile (i.e. 0.99999)
         * @return {@code this}
         */
        public Builder withCustomQuantile(String name, double value) {
            this.quantiles.add(new Quantile(name, value));
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
                                   filter,
                                   quantiles);
        }
    }

    static class CsvLineBuilder {

        private final List<String> labels;
        private final List<String> formats;
        private final List<Object> values;

        public CsvLineBuilder() {
            labels = new ArrayList<String>();
            formats = new ArrayList<String>();
            values = new ArrayList<Object>();
        }

        public void append(String label, String format, Object value) {
            labels.add(label);
            formats.add(format);
            values.add(value);
        }

        public String getLabels() {
            return join(labels, ",");
        }

        public String getFormattedValues(Locale locale) {
            return String.format(locale, join(formats, ","), values.toArray());
        }

        private static String join(List<String> s, String delimiter) {
            if (s == null || s.isEmpty()) return "";
            Iterator<String> iter = s.iterator();
            StringBuilder builder = new StringBuilder(iter.next());
            while( iter.hasNext() )
            {
                builder.append(delimiter).append(iter.next());
            }
            return builder.toString();
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(CsvReporter.class);
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private final File directory;
    private final Locale locale;
    private final Clock clock;
    private final List<Quantile> quantiles;

    private CsvReporter(MetricRegistry registry,
                        File directory,
                        Locale locale,
                        TimeUnit rateUnit,
                        TimeUnit durationUnit,
                        Clock clock,
                        MetricFilter filter,
                        List<Quantile> quantiles) {
        super(registry, "csv-reporter", filter, rateUnit, durationUnit);
        this.directory = directory;
        this.locale = locale;
        this.clock = clock;
        this.quantiles = quantiles;
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

        CsvLineBuilder lineBuilder = new CsvLineBuilder();
        lineBuilder.append("count", "%d", timer.getCount());
        lineBuilder.append("max", "%f", convertDuration(snapshot.getMax()));
        lineBuilder.append("mean", "%f", convertDuration(snapshot.getMean()));
        lineBuilder.append("min", "%f", convertDuration(snapshot.getMin()));
        lineBuilder.append("stddev", "%f", convertDuration(snapshot.getStdDev()));

        for (Quantile quantile : quantiles) {
            lineBuilder.append(quantile.getName(), "%f", convertDuration(snapshot.getValue(quantile.getValue())));
        }

        lineBuilder.append("mean_rate", "%f", convertRate(timer.getMeanRate()));
        lineBuilder.append("m1_rate", "%f", convertRate(timer.getOneMinuteRate()));
        lineBuilder.append("m5_rate", "%f", convertRate(timer.getFiveMinuteRate()));
        lineBuilder.append("m15_rate", "%f", convertRate(timer.getFifteenMinuteRate()));
        lineBuilder.append("rate_unit", "calls/%s", getRateUnit());
        lineBuilder.append("duration_unit", "%s", getDurationUnit());

        reportFormattedValues(timestamp,
                name,
                lineBuilder.getLabels(),
                lineBuilder.getFormattedValues(locale));
    }

    private void reportMeter(long timestamp, String name, Meter meter) {

        String formattedLine = String.format(locale, "%d,%f,%f,%f,%f,events/%s",
                                                meter.getCount(),
                                                convertRate(meter.getMeanRate()),
                                                convertRate(meter.getOneMinuteRate()),
                                                convertRate(meter.getFiveMinuteRate()),
                                                convertRate(meter.getFifteenMinuteRate()),
                                                getRateUnit());

        reportFormattedValues(timestamp, name, "count,mean_rate,m1_rate,m5_rate,m15_rate,rate_unit", formattedLine);
    }

    private void reportHistogram(long timestamp, String name, Histogram histogram) {
        final Snapshot snapshot = histogram.getSnapshot();

        CsvLineBuilder lineBuilder = new CsvLineBuilder();
        lineBuilder.append("count", "%d", histogram.getCount());
        lineBuilder.append("max", "%d", snapshot.getMax());
        lineBuilder.append("mean", "%f", snapshot.getMean());
        lineBuilder.append("min", "%d", snapshot.getMin());
        lineBuilder.append("stddev", "%f", snapshot.getStdDev());

        for (Quantile quantile : quantiles) {
            lineBuilder.append(quantile.getName(), "%f", snapshot.getValue(quantile.getValue()));
        }

        reportFormattedValues(timestamp,
                name,
                lineBuilder.getLabels(),
                lineBuilder.getFormattedValues(locale));
    }

    private void reportCounter(long timestamp, String name, Counter counter) {
        report(timestamp, name, "count", "%d", counter.getCount());
    }

    private void reportGauge(long timestamp, String name, Gauge gauge) {
        report(timestamp, name, "value", "%s", gauge.getValue());
    }

    private void report(long timestamp, String name, String header, String line, Object... values) {
        reportFormattedValues(timestamp, name, header, String.format(line, values));
    }

    private void reportFormattedValues(long timestamp, String name, String header, String formattedValues) {
        try {
            final File file = new File(directory, sanitize(name) + ".csv");
            final boolean fileAlreadyExists = file.exists();
            if (fileAlreadyExists || file.createNewFile()) {
                final PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file,true), UTF_8));
                try {
                    if (!fileAlreadyExists) {
                        out.println("t," + header);
                    }
                    out.printf(locale, String.format(locale, "%d,%s%n", timestamp, formattedValues));
                } finally {
                    out.close();
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Error writing to {}", name, e);
        }
    }

    protected String sanitize(String name) {
        return name;
    }
}
