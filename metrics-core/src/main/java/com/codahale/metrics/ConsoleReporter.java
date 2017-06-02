package com.codahale.metrics;

import java.io.PrintStream;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A reporter which outputs measurements to a {@link PrintStream}, like {@code System.out}.
 */
public class ConsoleReporter extends ScheduledReporter {
    /**
     * Returns a new {@link Builder} for {@link ConsoleReporter}.
     *
     * @param registry the registry to report
     * @return a {@link Builder} instance for a {@link ConsoleReporter}
     */
    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }

    /**
     * A builder for {@link ConsoleReporter} instances. Defaults to using the default locale and
     * time zone, writing to {@code System.out}, converting rates to events/second, converting
     * durations to milliseconds, and not filtering metrics.
     */
    public static class Builder {
        private final MetricRegistry registry;
        private PrintStream output;
        private Locale locale;
        private Clock clock;
        private TimeZone timeZone;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;
        private ScheduledExecutorService executor;
        private boolean shutdownExecutorOnStop;
        private Set<MetricAttribute> disabledMetricAttributes;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.output = System.out;
            this.locale = Locale.getDefault();
            this.clock = Clock.defaultClock();
            this.timeZone = TimeZone.getDefault();
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
            this.executor = null;
            this.shutdownExecutorOnStop = true;
            disabledMetricAttributes = Collections.emptySet();
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
         * Write to the given {@link PrintStream}.
         *
         * @param output a {@link PrintStream} instance.
         * @return {@code this}
         */
        public Builder outputTo(PrintStream output) {
            this.output = output;
            return this;
        }

        /**
         * Format numbers for the given {@link Locale}.
         *
         * @param locale a {@link Locale}
         * @return {@code this}
         */
        public Builder formattedFor(Locale locale) {
            this.locale = locale;
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
         * Use the given {@link TimeZone} for the time.
         *
         * @param timeZone a {@link TimeZone}
         * @return {@code this}
         */
        public Builder formattedFor(TimeZone timeZone) {
            this.timeZone = timeZone;
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
         * Don't report the passed metric attributes for all metrics (e.g. "p999", "stddev" or "m15").
         * See {@link MetricAttribute}.
         *
         * @param disabledMetricAttributes a {@link MetricFilter}
         * @return {@code this}
         */
        public Builder disabledMetricAttributes(Set<MetricAttribute> disabledMetricAttributes) {
            this.disabledMetricAttributes = disabledMetricAttributes;
            return this;
        }

        /**
         * Builds a {@link ConsoleReporter} with the given properties.
         *
         * @return a {@link ConsoleReporter}
         */
        public ConsoleReporter build() {
            return new ConsoleReporter(registry,
                                       output,
                                       locale,
                                       clock,
                                       timeZone,
                                       rateUnit,
                                       durationUnit,
                                       filter,
                                       executor,
                                       shutdownExecutorOnStop,
                                       disabledMetricAttributes);
        }
    }

    private static final int CONSOLE_WIDTH = 80;

    private final PrintStream output;
    private final Locale locale;
    private final Clock clock;
    private final DateFormat dateFormat;

    private ConsoleReporter(MetricRegistry registry,
                            PrintStream output,
                            Locale locale,
                            Clock clock,
                            TimeZone timeZone,
                            TimeUnit rateUnit,
                            TimeUnit durationUnit,
                            MetricFilter filter,
                            ScheduledExecutorService executor,
                            boolean shutdownExecutorOnStop,
                            Set<MetricAttribute> disabledMetricAttributes) {
        super(registry, "console-reporter", filter, rateUnit, durationUnit, executor, shutdownExecutorOnStop, disabledMetricAttributes);
        this.output = output;
        this.locale = locale;
        this.clock = clock;
        this.dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT,
                                                         DateFormat.MEDIUM,
                                                         locale);
        dateFormat.setTimeZone(timeZone);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {
        final String dateTime = dateFormat.format(new Date(clock.getTime()));
        printWithBanner(dateTime, '=');
        output.println();

        if (!gauges.isEmpty()) {
            printWithBanner("-- Gauges", '-');
            for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
                output.println(entry.getKey());
                printGauge(entry.getValue());
            }
            output.println();
        }

        if (!counters.isEmpty()) {
            printWithBanner("-- Counters", '-');
            for (Map.Entry<String, Counter> entry : counters.entrySet()) {
                output.println(entry.getKey());
                printCounter(entry);
            }
            output.println();
        }

        if (!histograms.isEmpty()) {
            printWithBanner("-- Histograms", '-');
            for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
                output.println(entry.getKey());
                printHistogram(entry.getValue());
            }
            output.println();
        }

        if (!meters.isEmpty()) {
            printWithBanner("-- Meters", '-');
            for (Map.Entry<String, Meter> entry : meters.entrySet()) {
                output.println(entry.getKey());
                printMeter(entry.getValue());
            }
            output.println();
        }

        if (!timers.isEmpty()) {
            printWithBanner("-- Timers", '-');
            for (Map.Entry<String, Timer> entry : timers.entrySet()) {
                output.println(entry.getKey());
                printTimer(entry.getValue());
            }
            output.println();
        }

        output.println();
        output.flush();
    }

    private void printMeter(Meter meter) {
        printIfEnabled(MetricAttribute.COUNT, String.format(locale, "             count = %d", meter.getCount()));
        printIfEnabled(MetricAttribute.MEAN_RATE, String.format(locale, "         mean rate = %2.2f events/%s", convertRate(meter.getMeanRate()), getRateUnit()));
        printIfEnabled(MetricAttribute.M1_RATE, String.format(locale, "     1-minute rate = %2.2f events/%s", convertRate(meter.getOneMinuteRate()), getRateUnit()));
        printIfEnabled(MetricAttribute.M5_RATE, String.format(locale, "     5-minute rate = %2.2f events/%s", convertRate(meter.getFiveMinuteRate()), getRateUnit()));
        printIfEnabled(MetricAttribute.M15_RATE, String.format(locale, "    15-minute rate = %2.2f events/%s", convertRate(meter.getFifteenMinuteRate()), getRateUnit()));
    }

    private void printCounter(Map.Entry<String, Counter> entry) {
        output.printf(locale, "             count = %d%n", entry.getValue().getCount());
    }

    private void printGauge(Gauge<?> gauge) {
        output.printf(locale, "             value = %s%n", gauge.getValue());
    }

    private void printHistogram(Histogram histogram) {
        printIfEnabled(MetricAttribute.COUNT, String.format(locale, "             count = %d", histogram.getCount()));
        Snapshot snapshot = histogram.getSnapshot();
        printIfEnabled(MetricAttribute.MIN, String.format(locale, "               min = %d", snapshot.getMin()));
        printIfEnabled(MetricAttribute.MAX, String.format(locale, "               max = %d", snapshot.getMax()));
        printIfEnabled(MetricAttribute.MEAN, String.format(locale, "              mean = %2.2f", snapshot.getMean()));
        printIfEnabled(MetricAttribute.STDDEV, String.format(locale, "            stddev = %2.2f", snapshot.getStdDev()));
        printIfEnabled(MetricAttribute.P50, String.format(locale, "            median = %2.2f", snapshot.getMedian()));
        printIfEnabled(MetricAttribute.P75, String.format(locale, "              75%% <= %2.2f", snapshot.get75thPercentile()));
        printIfEnabled(MetricAttribute.P95, String.format(locale, "              95%% <= %2.2f", snapshot.get95thPercentile()));
        printIfEnabled(MetricAttribute.P98, String.format(locale, "              98%% <= %2.2f", snapshot.get98thPercentile()));
        printIfEnabled(MetricAttribute.P99, String.format(locale, "              99%% <= %2.2f", snapshot.get99thPercentile()));
        printIfEnabled(MetricAttribute.P999, String.format(locale, "            99.9%% <= %2.2f", snapshot.get999thPercentile()));
    }

    private void printTimer(Timer timer) {
        final Snapshot snapshot = timer.getSnapshot();
        printIfEnabled(MetricAttribute.COUNT, String.format(locale, "             count = %d", timer.getCount()));
        printIfEnabled(MetricAttribute.MEAN_RATE, String.format(locale, "         mean rate = %2.2f calls/%s", convertRate(timer.getMeanRate()), getRateUnit()));
        printIfEnabled(MetricAttribute.M1_RATE, String.format(locale, "     1-minute rate = %2.2f calls/%s", convertRate(timer.getOneMinuteRate()), getRateUnit()));
        printIfEnabled(MetricAttribute.M5_RATE, String.format(locale, "     5-minute rate = %2.2f calls/%s", convertRate(timer.getFiveMinuteRate()), getRateUnit()));
        printIfEnabled(MetricAttribute.M15_RATE, String.format(locale, "    15-minute rate = %2.2f calls/%s", convertRate(timer.getFifteenMinuteRate()), getRateUnit()));

        printIfEnabled(MetricAttribute.MIN, String.format(locale, "               min = %2.2f %s", convertDuration(snapshot.getMin()), getDurationUnit()));
        printIfEnabled(MetricAttribute.MAX, String.format(locale, "               max = %2.2f %s", convertDuration(snapshot.getMax()), getDurationUnit()));
        printIfEnabled(MetricAttribute.MEAN, String.format(locale, "              mean = %2.2f %s", convertDuration(snapshot.getMean()), getDurationUnit()));
        printIfEnabled(MetricAttribute.STDDEV, String.format(locale, "            stddev = %2.2f %s", convertDuration(snapshot.getStdDev()), getDurationUnit()));
        printIfEnabled(MetricAttribute.P50, String.format(locale, "            median = %2.2f %s", convertDuration(snapshot.getMedian()), getDurationUnit()));
        printIfEnabled(MetricAttribute.P75, String.format(locale, "              75%% <= %2.2f %s", convertDuration(snapshot.get75thPercentile()), getDurationUnit()));
        printIfEnabled(MetricAttribute.P95, String.format(locale, "              95%% <= %2.2f %s", convertDuration(snapshot.get95thPercentile()), getDurationUnit()));
        printIfEnabled(MetricAttribute.P98, String.format(locale, "              98%% <= %2.2f %s", convertDuration(snapshot.get98thPercentile()), getDurationUnit()));
        printIfEnabled(MetricAttribute.P99, String.format(locale, "              99%% <= %2.2f %s", convertDuration(snapshot.get99thPercentile()), getDurationUnit()));
        printIfEnabled(MetricAttribute.P999, String.format(locale, "            99.9%% <= %2.2f %s", convertDuration(snapshot.get999thPercentile()), getDurationUnit()));
    }

    private void printWithBanner(String s, char c) {
        output.print(s);
        output.print(' ');
        for (int i = 0; i < (CONSOLE_WIDTH - s.length() - 1); i++) {
            output.print(c);
        }
        output.println();
    }

    /**
     * Print only if the attribute is enabled
     * @param type Metric attribute
     * @param status Status to be logged
     */
    private void printIfEnabled(MetricAttribute type, String status) {
        if(getDisabledMetricAttributes().contains(type)) {
            return;
        }

        output.println(status);
    }
}
