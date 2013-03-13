package com.yammer.metrics;

import java.io.PrintStream;
import java.text.DateFormat;
import java.util.*;

// TODO: 3/10/13 <coda> -- write tests
// TODO: 3/10/13 <coda> -- write docs

public class ConsoleReporter extends AbstractPollingReporter {
    private static final int CONSOLE_WIDTH = 80;

    private final PrintStream output;
    private final Locale locale;
    private final Clock clock;
    private final DateFormat dateFormat;

    public ConsoleReporter(MetricRegistry registry,
                           PrintStream output,
                           Locale locale,
                           Clock clock,
                           TimeZone timeZone) {
        super(registry, "console-reporter");
        this.output = output;
        this.locale = locale;
        this.clock = clock;
        this.dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT,
                                                         DateFormat.MEDIUM,
                                                         locale);
        dateFormat.setTimeZone(timeZone);
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

        printWithBanner("-- Gauges", '-');
        for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
            output.println(entry.getKey());
            printGauge(entry);
        }
        output.println();

        printWithBanner("-- Counters", '-');
        for (Map.Entry<String, Counter> entry : counters.entrySet()) {
            output.println(entry.getKey());
            printCounter(entry);
        }
        output.println();

        printWithBanner("-- Histograms", '-');
        for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
            output.println(entry.getKey());
            printHistogram(entry.getValue());
        }
        output.println();

        printWithBanner("-- Meters", '-');
        for (Map.Entry<String, Meter> entry : meters.entrySet()) {
            output.println(entry.getKey());
            printMetered(entry.getValue());
        }
        output.println();

        printWithBanner("-- Timers", '-');
        for (Map.Entry<String, Timer> entry : timers.entrySet()) {
            output.println(entry.getKey());
            printTimer(entry.getValue());
        }
        output.println();

        output.println();
        output.flush();
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
        printSnapshot(histogram.getSnapshot());
    }

    private void printTimer(Timer timer) {
        final Snapshot snapshot = timer.getSnapshot();
        printMetered(timer);
        output.printf(locale, "               min = %d%n", timer.getMin());
        output.printf(locale, "               max = %d%n", timer.getMax());
        output.printf(locale, "              mean = %2.2f%n", timer.getMean());
        output.printf(locale, "            stddev = %2.2f%n", timer.getStdDev());
        printSnapshot(snapshot);
    }

    private void printSnapshot(Snapshot snapshot) {
        output.printf(locale, "            median = %2.2f%n", snapshot.getMedian());
        output.printf(locale, "              75%% <= %2.2f%n", snapshot.get75thPercentile());
        output.printf(locale, "              95%% <= %2.2f%n", snapshot.get95thPercentile());
        output.printf(locale, "              98%% <= %2.2f%n", snapshot.get98thPercentile());
        output.printf(locale, "              99%% <= %2.2f%n", snapshot.get99thPercentile());
        output.printf(locale, "            99.9%% <= %2.2f%n", snapshot.get999thPercentile());
    }

    private void printMetered(Metered timer) {
        output.printf(locale, "             count = %d%n", timer.getCount());
        output.printf(locale, "         mean rate = %2.2f%n", timer.getMeanRate());
        output.printf(locale, "     1-minute rate = %2.2f%n", timer.getOneMinuteRate());
        output.printf(locale, "     5-minute rate = %2.2f%n", timer.getFiveMinuteRate());
        output.printf(locale, "    15-minute rate = %2.2f%n", timer.getFifteenMinuteRate());
    }

    private void printWithBanner(String s, char c) {
        output.print(s);
        output.print(' ');
        for (int i = 0; i < (CONSOLE_WIDTH - s.length() - 1); i++) {
            output.print(c);
        }
        output.println();
    }
}
