package com.yammer.metrics.reporting;

import com.yammer.metrics.core.*;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.stats.Snapshot;

import java.io.PrintStream;
import java.text.DateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 * A simple reporters which prints out application metrics to a {@link PrintStream} periodically.
 */
public class ConsoleReporter extends AbstractPollingReporter implements MetricProcessor<PrintStream> {
    private static final int CONSOLE_WIDTH = 80;

    private final PrintStream out;
    private final MetricPredicate predicate;
    private final Clock clock;
    private final TimeZone timeZone;
    private final Locale locale;


    public static class Builder {
        private final Set<MetricsRegistry> registries;
        private final String name;
        private final long period;
        private final TimeUnit timeUnit;

        private PrintStream out;
        private MetricPredicate predicate;
        private Clock clock;
        private TimeZone timeZone;
        private Locale locale;

        public Builder(Set<MetricsRegistry> registries, String name, long period, TimeUnit unit) {
            this.registries = registries;
            this.name = name;
            this.period = period;
            this.timeUnit = unit;

            //Set mutable items to sensible defaults
            this.out = System.out;
            this.predicate = MetricPredicate.ALL;
            this.clock = Clock.defaultClock();
            this.timeZone = TimeZone.getDefault();
            this.locale = Locale.getDefault();
        }

        public Builder withPrintStream(PrintStream stream) {
            this.out = stream;
            return this;
        }

        public Builder withClock(Clock clock) {
            this.clock = clock;
            return this;
        }

        public Builder withPredicate(MetricPredicate predicate) {
            this.predicate = predicate;
            return this;
        }

        public Builder withLocale(Locale locale) {
            this.locale = locale;
            return this;
        }

        public Builder withTimeZone(TimeZone timeZone) {
            this.timeZone = timeZone;
            return this;
        }

        public ConsoleReporter build() {
            return new ConsoleReporter(this);
        }
    }

    private ConsoleReporter(Builder builder) {
        super(builder.registries, builder.name, builder.period, builder.timeUnit);
        this.clock = builder.clock;
        this.locale = builder.locale;
        this.predicate = builder.predicate;
        this.out = builder.out;
        this.timeZone = builder.timeZone;
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

            for (MetricsRegistry registry : getMetricsRegistries()) {
                for (Entry<String, SortedMap<MetricName, Metric>> entry : registry.groupedMetrics(predicate).entrySet()) {
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
        stream.printf(locale, "    getCount = %d\n", counter.count());
    }

    @Override
    public void processMeter(MetricName name, Metered meter, PrintStream stream) {
        final String unit = abbrev(meter.rateUnit());
        stream.printf(locale, "             getCount = %d\n", meter.getCount());
        stream.printf(locale, "         getMean rate = %2.2f %s/%s\n",
                meter.getMeanRate(),
                meter.eventType(),
                unit);
        stream.printf(locale, "     1-minute rate = %2.2f %s/%s\n",
                meter.getOneMinuteRate(),
                meter.eventType(),
                unit);
        stream.printf(locale, "     5-minute rate = %2.2f %s/%s\n",
                meter.getFiveMinuteRate(),
                meter.eventType(),
                unit);
        stream.printf(locale, "    15-minute rate = %2.2f %s/%s\n",
                meter.getFifteenMinuteRate(),
                meter.eventType(),
                unit);
    }

    @Override
    public void processHistogram(MetricName name, Histogram histogram, PrintStream stream) {
        final Snapshot snapshot = histogram.getSnapshot();
        stream.printf(locale, "               getMin = %2.2f\n", histogram.getMin());
        stream.printf(locale, "               getMax = %2.2f\n", histogram.getMax());
        stream.printf(locale, "              getMean = %2.2f\n", histogram.getMean());
        stream.printf(locale, "            stddev = %2.2f\n", histogram.getStdDev());
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
        stream.printf(locale, "               getMin = %2.2f%s\n", timer.getMin(), durationUnit);
        stream.printf(locale, "               getMax = %2.2f%s\n", timer.getMax(), durationUnit);
        stream.printf(locale, "              getMean = %2.2f%s\n", timer.getMean(), durationUnit);
        stream.printf(locale, "            stddev = %2.2f%s\n", timer.getStdDev(), durationUnit);
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
