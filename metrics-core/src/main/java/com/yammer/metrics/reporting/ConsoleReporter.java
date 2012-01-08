package com.yammer.metrics.reporting;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.*;
import com.yammer.metrics.stats.Snapshot;
import com.yammer.metrics.core.MetricPredicate;

import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * A simple reporters which prints out application metrics to a {@link PrintStream} periodically.
 */
public class ConsoleReporter extends AbstractPollingReporter implements
                                                             MetricProcessor<PrintStream> {
    private static final int CONSOLE_WIDTH = 80;

    /**
     * Enables the console reporter for the default metrics registry, and causes it to print to
     * STDOUT with the specified period.
     *
     * @param period the period between successive outputs
     * @param unit   the time unit of {@code period}
     */
    public static void enable(long period, TimeUnit unit) {
        enable(Metrics.defaultRegistry(), period, unit);
    }

    /**
     * Enables the console reporter for the given metrics registry, and causes it to print to STDOUT
     * with the specified period and unrestricted output.
     *
     * @param metricsRegistry the metrics registry
     * @param period          the period between successive outputs
     * @param unit            the time unit of {@code period}
     */
    public static void enable(MetricsRegistry metricsRegistry, long period, TimeUnit unit) {
        final ConsoleReporter reporter = new ConsoleReporter(metricsRegistry,
                                                             System.out,
                                                             MetricPredicate.ALL);
        reporter.start(period, unit);
    }

    private final PrintStream out;
    private final MetricPredicate predicate;
    private final Clock clock;
    private final TimeZone timeZone;
    private final Locale locale;

    /**
     * Creates a new {@link ConsoleReporter} for the default metrics registry, with unrestricted
     * output.
     *
     * @param out the {@link PrintStream} to which output will be written
     */
    public ConsoleReporter(PrintStream out) {
        this(Metrics.defaultRegistry(), out, MetricPredicate.ALL);
    }

    /**
     * Creates a new {@link ConsoleReporter} for a given metrics registry.
     *
     * @param metricsRegistry the metrics registry
     * @param out             the {@link PrintStream} to which output will be written
     * @param predicate       the {@link MetricPredicate} used to determine whether a metric will be
     *                        output
     */
    public ConsoleReporter(MetricsRegistry metricsRegistry, PrintStream out, MetricPredicate predicate) {
        this(metricsRegistry, out, predicate, Clock.defaultClock(), TimeZone.getDefault());
    }

    /**
     * Creates a new {@link ConsoleReporter} for a given metrics registry.
     *
     * @param metricsRegistry the metrics registry
     * @param out             the {@link PrintStream} to which output will be written
     * @param predicate       the {@link MetricPredicate} used to determine whether a metric will be
     *                        output
     * @param clock           the {@link Clock} used to print time
     * @param timeZone        the {@link TimeZone} used to print time
     */
    public ConsoleReporter(MetricsRegistry metricsRegistry,
                           PrintStream out,
                           MetricPredicate predicate,
                           Clock clock,
                           TimeZone timeZone) {
        this(metricsRegistry, out, predicate, clock, timeZone, Locale.getDefault());
    }

    /**
     * Creates a new {@link ConsoleReporter} for a given metrics registry.
     *
     * @param metricsRegistry the metrics registry
     * @param out             the {@link PrintStream} to which output will be written
     * @param predicate       the {@link MetricPredicate} used to determine whether a metric will be
     *                        output
     * @param clock           the {@link com.yammer.metrics.core.Clock} used to print time
     * @param timeZone        the {@link TimeZone} used to print time
     * @param locale          the {@link Locale} used to print values
     */
    public ConsoleReporter(MetricsRegistry metricsRegistry,
                           PrintStream out,
                           MetricPredicate predicate,
                           Clock clock,
                           TimeZone timeZone, Locale locale) {
        super(metricsRegistry, "console-reporter");
        this.out = out;
        this.predicate = predicate;
        this.clock = clock;
        this.timeZone = timeZone;
        this.locale = locale;
    }

    @Override
    public void run() {
        try {
            final DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT,
                                                                     DateFormat.MEDIUM,
                                                                     locale);
            format.setTimeZone(timeZone);
            final String dateTime = format.format(new Date(clock.time()));
            out.print(dateTime);
            out.print(' ');
            for (int i = 0; i < (CONSOLE_WIDTH - dateTime.length() - 1); i++) {
                out.print('=');
            }
            out.println();
            for (Entry<String, SortedMap<MetricName, Metric>> entry : getMetricsRegistry().groupedMetrics(
                    predicate).entrySet()) {
                out.print(entry.getKey());
                out.println(':');
                for (Entry<MetricName, Metric> subEntry : entry.getValue().entrySet()) {
                    out.print("  ");
                    out.print(subEntry.getKey().getName());
                    out.println(':');
                    subEntry.getValue().processWith(this, subEntry.getKey(), out);
                    out.println();
                }
                out.println();
            }
            out.println();
            out.flush();
        } catch (Exception e) {
            e.printStackTrace(out);
        }
    }

    @Override
    public void processGauge(MetricName name, Gauge<?> gauge, PrintStream stream) {
        stream.printf(locale, "    value = %s\n", gauge.value());
    }

    @Override
    public void processCounter(MetricName name, Counter counter, PrintStream stream) {
        stream.printf(locale, "    count = %d\n", counter.count());
    }

    @Override
    public void processMeter(MetricName name, Metered meter, PrintStream stream) {
        final String unit = abbrev(meter.rateUnit());
        stream.printf(locale, "             count = %d\n", meter.count());
        stream.printf(locale, "         mean rate = %2.2f %s/%s\n",
                      meter.meanRate(),
                      meter.eventType(),
                      unit);
        stream.printf(locale, "     1-minute rate = %2.2f %s/%s\n",
                      meter.oneMinuteRate(),
                      meter.eventType(),
                      unit);
        stream.printf(locale, "     5-minute rate = %2.2f %s/%s\n",
                      meter.fiveMinuteRate(),
                      meter.eventType(),
                      unit);
        stream.printf(locale, "    15-minute rate = %2.2f %s/%s\n",
                      meter.fifteenMinuteRate(),
                      meter.eventType(),
                      unit);
    }

    @Override
    public void processHistogram(MetricName name, Histogram histogram, PrintStream stream) {
        final Snapshot snapshot = histogram.getSnapshot();
        stream.printf(locale, "               min = %2.2f\n", histogram.min());
        stream.printf(locale, "               max = %2.2f\n", histogram.max());
        stream.printf(locale, "              mean = %2.2f\n", histogram.mean());
        stream.printf(locale, "            stddev = %2.2f\n", histogram.stdDev());
        stream.printf(locale, "            median = %2.2f\n", snapshot.getMedian());
        stream.printf(locale, "              75%% <= %2.2f\n", snapshot.get75thPercentile());
        stream.printf(locale, "              95%% <= %2.2f\n", snapshot.get95thPercentile());
        stream.printf(locale, "              98%% <= %2.2f\n", snapshot.get98thPercentile());
        stream.printf(locale, "              99%% <= %2.2f\n", snapshot.get99thPercentile());
        stream.printf(locale, "            99.9%% <= %2.2f\n", snapshot.get999thPercentile());
    }

    @Override
    public void processTimer(MetricName name, Timer timer, PrintStream stream) {
        processMeter(name, timer, stream);
        final String durationUnit = abbrev(timer.durationUnit());
        final Snapshot snapshot = timer.getSnapshot();
        stream.printf(locale, "               min = %2.2f%s\n", timer.min(), durationUnit);
        stream.printf(locale, "               max = %2.2f%s\n", timer.max(), durationUnit);
        stream.printf(locale, "              mean = %2.2f%s\n", timer.mean(), durationUnit);
        stream.printf(locale, "            stddev = %2.2f%s\n", timer.stdDev(), durationUnit);
        stream.printf(locale, "            median = %2.2f%s\n", snapshot.getMedian(), durationUnit);
        stream.printf(locale, "              75%% <= %2.2f%s\n", snapshot.get75thPercentile(), durationUnit);
        stream.printf(locale, "              95%% <= %2.2f%s\n", snapshot.get95thPercentile(), durationUnit);
        stream.printf(locale, "              98%% <= %2.2f%s\n", snapshot.get98thPercentile(), durationUnit);
        stream.printf(locale, "              99%% <= %2.2f%s\n", snapshot.get99thPercentile(), durationUnit);
        stream.printf(locale, "            99.9%% <= %2.2f%s\n", snapshot.get999thPercentile(), durationUnit);
    }

    private String abbrev(TimeUnit unit) {
        switch (unit) {
            case NANOSECONDS:
                return "ns";
            case MICROSECONDS:
                return "us";
            case MILLISECONDS:
                return "ms";
            case SECONDS:
                return "s";
            case MINUTES:
                return "m";
            case HOURS:
                return "h";
            case DAYS:
                return "d";
            default:
                throw new IllegalArgumentException("Unrecognized TimeUnit: " + unit);
        }
    }
}
