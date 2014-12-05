package com.codahale.metrics;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
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
        private List<Quantile> quantiles;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.output = System.out;
            this.locale = Locale.getDefault();
            this.clock = Clock.defaultClock();
            this.timeZone = TimeZone.getDefault();
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
            this.quantiles = Quantiles.defaultQuantiles();
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
         * Add custom quantile to the set of reported quantiles.
         *
         * @param name Name of the quantile (i.e. p99999)
         * @param value Value of the quantile (i.e. 0.99999)
         * @return {@code this}
         */
        public Builder withQuantile(String name, double value) {
            this.quantiles.add(new Quantile(name, value));
            return this;
        }

        /**
         * Removes all quantiles from the list of reported quantiles including default ones.
         *
         * @return {@code this}
         */
        public Builder withNoQuantiles() {
            this.quantiles.clear();
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
                                       quantiles);
        }
    }

    private static final int CONSOLE_WIDTH = 80;

    private final PrintStream output;
    private final Locale locale;
    private final Clock clock;
    private final DateFormat dateFormat;
    private final DecimalFormat decimalFormat;
    private final List<Quantile> quantiles;

    private ConsoleReporter(MetricRegistry registry,
                            PrintStream output,
                            Locale locale,
                            Clock clock,
                            TimeZone timeZone,
                            TimeUnit rateUnit,
                            TimeUnit durationUnit,
                            MetricFilter filter,
                            List<Quantile> quantiles) {
        super(registry, "console-reporter", filter, rateUnit, durationUnit);
        this.output = output;
        this.locale = locale;
        this.clock = clock;
        this.quantiles = quantiles;
        this.dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT,
                                                         DateFormat.MEDIUM,
                                                         locale);
        dateFormat.setTimeZone(timeZone);
        decimalFormat = new DecimalFormat("#.#####", new DecimalFormatSymbols(locale));
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
        output.printf(locale, "         mean rate = %2.2f events/%s%n", convertRate(meter.getMeanRate()), getRateUnit());
        output.printf(locale, "     1-minute rate = %2.2f events/%s%n", convertRate(meter.getOneMinuteRate()), getRateUnit());
        output.printf(locale, "     5-minute rate = %2.2f events/%s%n", convertRate(meter.getFiveMinuteRate()), getRateUnit());
        output.printf(locale, "    15-minute rate = %2.2f events/%s%n", convertRate(meter.getFifteenMinuteRate()), getRateUnit());
    }

    private void printCounter(Map.Entry<String, Counter> entry) {
        output.printf(locale, "             count = %d%n", entry.getValue().getCount());
    }

    private void printGauge(Map.Entry<String, Gauge> entry) {
        output.printf(locale, "             value = %s%n", entry.getValue().getValue());
    }

    private void printHistogram(Histogram histogram) {
        output.printf(locale, "             count = %d%n", histogram.getCount());
        Snapshot snapshot = histogram.getSnapshot();
        output.printf(locale, "               min = %d%n", snapshot.getMin());
        output.printf(locale, "               max = %d%n", snapshot.getMax());
        output.printf(locale, "              mean = %2.2f%n", snapshot.getMean());
        output.printf(locale, "            stddev = %2.2f%n", snapshot.getStdDev());

        for (Quantile quantile : quantiles) {
            output.printf(locale, "           %6s <= %2.2f%n", convertPercentilValueToString(quantile.getValue()), snapshot.getValue(quantile.getValue()));
        }
    }

    private void printTimer(Timer timer) {
        final Snapshot snapshot = timer.getSnapshot();
        output.printf(locale, "             count = %d%n", timer.getCount());
        output.printf(locale, "         mean rate = %2.2f calls/%s%n", convertRate(timer.getMeanRate()), getRateUnit());
        output.printf(locale, "     1-minute rate = %2.2f calls/%s%n", convertRate(timer.getOneMinuteRate()), getRateUnit());
        output.printf(locale, "     5-minute rate = %2.2f calls/%s%n", convertRate(timer.getFiveMinuteRate()), getRateUnit());
        output.printf(locale, "    15-minute rate = %2.2f calls/%s%n", convertRate(timer.getFifteenMinuteRate()), getRateUnit());

        output.printf(locale, "               min = %2.2f %s%n", convertDuration(snapshot.getMin()), getDurationUnit());
        output.printf(locale, "               max = %2.2f %s%n", convertDuration(snapshot.getMax()), getDurationUnit());
        output.printf(locale, "              mean = %2.2f %s%n", convertDuration(snapshot.getMean()), getDurationUnit());
        output.printf(locale, "            stddev = %2.2f %s%n", convertDuration(snapshot.getStdDev()), getDurationUnit());

        for (Quantile quantile : quantiles) {
            output.printf(locale, "           %6s <= %2.2f %s%n", convertPercentilValueToString(quantile.getValue()), convertDuration(snapshot.getValue(quantile.getValue())), getDurationUnit());
        }
    }

    private void printWithBanner(String s, char c) {
        output.print(s);
        output.print(' ');
        for (int i = 0; i < (CONSOLE_WIDTH - s.length() - 1); i++) {
            output.print(c);
        }
        output.println();
    }

    private String convertPercentilValueToString(double value) {
        return decimalFormat.format(value * 100) +"%";
    }
}
