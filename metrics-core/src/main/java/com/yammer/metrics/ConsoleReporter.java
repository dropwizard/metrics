package com.yammer.metrics;

import java.io.PrintStream;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * A reporter which outputs measurements to a {@link PrintStream}, like {@code System.out}.
 */
public class ConsoleReporter extends AbstractPollingReporter {
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

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.output = System.out;
            this.locale = Locale.getDefault();
            this.clock = Clock.defaultClock();
            this.timeZone = TimeZone.getDefault();
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
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
                                       filter);
        }
    }

    private static final int CONSOLE_WIDTH = 80;

    private final PrintStream output;
    private final Locale locale;
    private final Clock clock;
    private final DateFormat dateFormat;
    private final double durationFactor;
    private final String durationUnit;
    private final double rateFactor;
    private final String rateUnit;

    private ConsoleReporter(MetricRegistry registry,
                            PrintStream output,
                            Locale locale,
                            Clock clock,
                            TimeZone timeZone,
                            TimeUnit rateUnit,
                            TimeUnit durationUnit,
                            MetricFilter filter) {
        super(registry, "console-reporter", filter);
        this.output = output;
        this.locale = locale;
        this.clock = clock;
        this.dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT,
                                                         DateFormat.MEDIUM,
                                                         locale);
        dateFormat.setTimeZone(timeZone);
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
        final String dateTime = dateFormat.format(new Date(clock.getTime()));
        printWithBanner(dateTime, '=');
        output.println();

        if (!gauges.isEmpty()) {
            printWithBanner("-- Gauges", '-');
            for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
                output.println(entry.getKey());
                printGauge(entry);
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
        output.printf(locale, "             count = %d%n", meter.getCount());
        output.printf(locale, "         mean rate = %2.2f events/%s%n", meter.getMeanRate() * rateFactor, rateUnit);
        output.printf(locale, "     1-minute rate = %2.2f events/%s%n", meter.getOneMinuteRate() * rateFactor, rateUnit);
        output.printf(locale, "     5-minute rate = %2.2f events/%s%n", meter.getFiveMinuteRate() * rateFactor, rateUnit);
        output.printf(locale, "    15-minute rate = %2.2f events/%s%n", meter.getFifteenMinuteRate() * rateFactor, rateUnit);
    }

    private void printCounter(Map.Entry<String, Counter> entry) {
        output.printf(locale, "             count = %d%n", entry.getValue().getCount());
    }

    private void printGauge(Map.Entry<String, Gauge> entry) {
        output.printf(locale, "             value = %s%n", entry.getValue().getValue());
    }

    private void printHistogram(Histogram histogram) {
        output.printf(locale, "             count = %d%n", histogram.getCount());
        output.printf(locale, "               min = %d%n", histogram.getMin());
        output.printf(locale, "               max = %d%n", histogram.getMax());
        output.printf(locale, "              mean = %2.2f%n", histogram.getMean());
        output.printf(locale, "            stddev = %2.2f%n", histogram.getStdDev());
        Snapshot snapshot = histogram.getSnapshot();
        output.printf(locale, "            median = %2.2f%n", snapshot.getMedian());
        output.printf(locale, "              75%% <= %2.2f%n", snapshot.get75thPercentile());
        output.printf(locale, "              95%% <= %2.2f%n", snapshot.get95thPercentile());
        output.printf(locale, "              98%% <= %2.2f%n", snapshot.get98thPercentile());
        output.printf(locale, "              99%% <= %2.2f%n", snapshot.get99thPercentile());
        output.printf(locale, "            99.9%% <= %2.2f%n", snapshot.get999thPercentile());
    }

    private void printTimer(Timer timer) {
        final Snapshot snapshot = timer.getSnapshot();
        output.printf(locale, "             count = %d%n", timer.getCount());
        output.printf(locale, "         mean rate = %2.2f calls/%s%n", timer.getMeanRate() * rateFactor, rateUnit);
        output.printf(locale, "     1-minute rate = %2.2f calls/%s%n", timer.getOneMinuteRate() * rateFactor, rateUnit);
        output.printf(locale, "     5-minute rate = %2.2f calls/%s%n", timer.getFiveMinuteRate() * rateFactor, rateUnit);
        output.printf(locale, "    15-minute rate = %2.2f calls/%s%n", timer.getFifteenMinuteRate() * rateFactor, rateUnit);

        output.printf(locale, "               min = %2.2f %s%n", timer.getMin() * durationFactor, durationUnit);
        output.printf(locale, "               max = %2.2f %s%n", timer.getMax() * durationFactor, durationUnit);
        output.printf(locale, "              mean = %2.2f %s%n", timer.getMean() * durationFactor, durationUnit);
        output.printf(locale, "            stddev = %2.2f %s%n", timer.getStdDev() * durationFactor, durationUnit);
        output.printf(locale, "            median = %2.2f %s%n", snapshot.getMedian() * durationFactor, durationUnit);
        output.printf(locale, "              75%% <= %2.2f %s%n", snapshot.get75thPercentile() * durationFactor, durationUnit);
        output.printf(locale, "              95%% <= %2.2f %s%n", snapshot.get95thPercentile() * durationFactor, durationUnit);
        output.printf(locale, "              98%% <= %2.2f %s%n", snapshot.get98thPercentile() * durationFactor, durationUnit);
        output.printf(locale, "              99%% <= %2.2f %s%n", snapshot.get99thPercentile() * durationFactor, durationUnit);
        output.printf(locale, "            99.9%% <= %2.2f %s%n", snapshot.get999thPercentile() * durationFactor, durationUnit);
    }

    private void printWithBanner(String s, char c) {
        output.print(s);
        output.print(' ');
        for (int i = 0; i < (CONSOLE_WIDTH - s.length() - 1); i++) {
            output.print(c);
        }
        output.println();
    }

    private String calculateRateUnit(TimeUnit unit) {
        final String s = unit.toString().toLowerCase(Locale.US);
        return s.substring(0, s.length() - 1);
    }
}
