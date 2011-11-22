package com.yammer.metrics.reporting;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.*;
import com.yammer.metrics.util.MetricPredicate;
import com.yammer.metrics.util.Utils;

import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * A simple reporters which prints out application metrics to a
 * {@link PrintStream} periodically.
 */
public class ConsoleReporter extends AbstractPollingReporter {
    /**
     * Enables the console reporter for the default metrics registry, and causes it to
     * print to STDOUT with the specified period.
     *
     * @param period the period between successive outputs
     * @param unit   the time unit of {@code period}
     */
    public static void enable(long period, TimeUnit unit) {
        enable(Metrics.defaultRegistry(), period, unit);
    }

    /**
     * Enables the console reporter for the given metrics registry, and causes
     * it to print to STDOUT with the specified period and unrestricted output.
     *
     * @param metricsRegistry the metrics registry
     * @param period          the period between successive outputs
     * @param unit            the time unit of {@code period}
     */
    public static void enable(MetricsRegistry metricsRegistry, long period, TimeUnit unit) {
        final ConsoleReporter reporter = new ConsoleReporter(metricsRegistry, System.out, MetricPredicate.ALL);
        reporter.start(period, unit);
    }

    private final PrintStream out;
    private final MetricPredicate predicate;
    private final Clock clock;
    private final TimeZone timeZone;

    /**
     * Creates a new {@link ConsoleReporter} for the default metrics registry, with unrestricted output.
     *
     * @param out the {@link java.io.PrintStream} to which output will be written
     */
    public ConsoleReporter(PrintStream out) {
        this(Metrics.defaultRegistry(), out, MetricPredicate.ALL);
    }

    /**
     * Creates a new {@link ConsoleReporter} for a given metrics registry.
     *
     * @param metricsRegistry the metrics registry
     * @param out             the {@link java.io.PrintStream} to which output will be written
     * @param predicate       the {@link MetricPredicate} used to determine whether a metric will be output
     */
    public ConsoleReporter(MetricsRegistry metricsRegistry, PrintStream out, MetricPredicate predicate) {
        this(metricsRegistry, out, predicate, Clock.DEFAULT, TimeZone.getDefault());
    }

    /**
     * Creates a new {@link ConsoleReporter} for a given metrics registry.
     *
     * @param metricsRegistry the metrics registry
     * @param out             the {@link java.io.PrintStream} to which output will be written
     * @param predicate       the {@link MetricPredicate} used to determine whether a metric will be output
     * @param clock           the {@link Clock} used to print time
     * @param timeZone        the {@link TimeZone} used to print time
     */
    public ConsoleReporter(MetricsRegistry metricsRegistry,
                           PrintStream out,
                           MetricPredicate predicate,
                           Clock clock,
                           TimeZone timeZone) {
        super(metricsRegistry, "console-reporter");
        this.out = out;
        this.predicate = predicate;
        this.clock = clock;
        this.timeZone = timeZone;
    }

    @Override
    public void run() {
        try {
            final DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
            format.setTimeZone(timeZone);
            final String dateTime = format.format(new Date(clock.time()));
            out.print(dateTime);
            out.print(' ');
            for (int i = 0; i < (80 - dateTime.length() - 1); i++) {
                out.print('=');
            }
            out.println();

            for (Entry<String, Map<String, Metric>> entry : Utils.sortAndFilterMetrics(metricsRegistry.allMetrics(), predicate).entrySet()) {
                out.print(entry.getKey());
                out.println(':');

                for (Entry<String, Metric> subEntry : entry.getValue().entrySet()) {
                    out.print("  ");
                    out.print(subEntry.getKey());
                    out.println(':');

                    final Metric metric = subEntry.getValue();
                    if (metric instanceof GaugeMetric<?>) {
                        printGauge((GaugeMetric<?>) metric);
                    } else if (metric instanceof CounterMetric) {
                        printCounter((CounterMetric) metric);
                    } else if (metric instanceof HistogramMetric) {
                        printHistogram((HistogramMetric) metric);
                    } else if (metric instanceof MeterMetric) {
                        printMetered((MeterMetric) metric);
                    } else if (metric instanceof TimerMetric) {
                        printTimer((TimerMetric) metric);
                    }
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

    private void printGauge(GaugeMetric<?> gauge) {
        out.print("    value = ");
        out.println(gauge.value());
    }

    private void printCounter(CounterMetric counter) {
        out.print("    count = ");
        out.println(counter.count());
    }

    private void printMetered(Metered meter) {
        final String unit = abbrev(meter.rateUnit());
        out.printf("             count = %d\n", meter.count());
        out.printf("         mean rate = %2.2f %s/%s\n", meter.meanRate(), meter.eventType(), unit);
        out.printf("     1-minute rate = %2.2f %s/%s\n", meter.oneMinuteRate(), meter.eventType(), unit);
        out.printf("     5-minute rate = %2.2f %s/%s\n", meter.fiveMinuteRate(), meter.eventType(), unit);
        out.printf("    15-minute rate = %2.2f %s/%s\n", meter.fifteenMinuteRate(), meter.eventType(), unit);
    }

    private void printHistogram(HistogramMetric histogram) {
        final double[] percentiles = histogram.percentiles(0.5, 0.75, 0.95, 0.98, 0.99, 0.999);
        out.printf("               min = %2.2f\n", histogram.min());
        out.printf("               max = %2.2f\n", histogram.max());
        out.printf("              mean = %2.2f\n", histogram.mean());
        out.printf("            stddev = %2.2f\n", histogram.stdDev());
        out.printf("            median = %2.2f\n", percentiles[0]);
        out.printf("              75%% <= %2.2f\n", percentiles[1]);
        out.printf("              95%% <= %2.2f\n", percentiles[2]);
        out.printf("              98%% <= %2.2f\n", percentiles[3]);
        out.printf("              99%% <= %2.2f\n", percentiles[4]);
        out.printf("            99.9%% <= %2.2f\n", percentiles[5]);
    }

    private void printTimer(TimerMetric timer) {
        printMetered(timer);

        final String durationUnit = abbrev(timer.durationUnit());

        final double[] percentiles = timer.percentiles(0.5, 0.75, 0.95, 0.98, 0.99, 0.999);
        out.printf("               min = %2.2f%s\n", timer.min(), durationUnit);
        out.printf("               max = %2.2f%s\n", timer.max(), durationUnit);
        out.printf("              mean = %2.2f%s\n", timer.mean(), durationUnit);
        out.printf("            stddev = %2.2f%s\n", timer.stdDev(), durationUnit);
        out.printf("            median = %2.2f%s\n", percentiles[0], durationUnit);
        out.printf("              75%% <= %2.2f%s\n", percentiles[1], durationUnit);
        out.printf("              95%% <= %2.2f%s\n", percentiles[2], durationUnit);
        out.printf("              98%% <= %2.2f%s\n", percentiles[3], durationUnit);
        out.printf("              99%% <= %2.2f%s\n", percentiles[4], durationUnit);
        out.printf("            99.9%% <= %2.2f%s\n", percentiles[5], durationUnit);
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
        }
        throw new IllegalArgumentException("Unrecognized TimeUnit: " + unit);
    }
}
