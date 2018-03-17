package com.codahale.metrics;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConsoleReporterTest {
    private final Locale locale = Locale.US;
    private final TimeZone timeZone = TimeZone.getTimeZone("America/Los_Angeles");

    private final MetricRegistry registry = mock(MetricRegistry.class);
    private final Clock clock = mock(Clock.class);
    private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    private final PrintStream output = new PrintStream(bytes);
    private final ConsoleReporter reporter = ConsoleReporter.forRegistry(registry)
            .outputTo(output)
            .formattedFor(locale)
            .withClock(clock)
            .formattedFor(timeZone)
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .filter(MetricFilter.ALL)
            .build();
    private String dateHeader;

    @Before
    public void setUp() throws Exception {
        when(clock.getTime()).thenReturn(1363568676000L);
        // JDK9 has changed the java.text.DateFormat API implementation according to Unicode.
        // See http://mail.openjdk.java.net/pipermail/jdk9-dev/2017-April/005732.html
        dateHeader = System.getProperty("java.version").startsWith("1.8") ?
                "3/17/13 6:04:36 PM =============================================================" :
                "3/17/13, 6:04:36 PM ============================================================";
    }

    @Test
    public void reportsGaugeValues() throws Exception {
        final Gauge<Integer> gauge = () -> 1;

        reporter.report(map("gauge", gauge),
                map(),
                map(),
                map(),
                map());

        assertThat(consoleOutput())
                .isEqualTo(lines(
                        dateHeader,
                        "",
                        "-- Gauges ----------------------------------------------------------------------",
                        "gauge",
                        "             value = 1",
                        "",
                        ""
                ));
    }

    @Test
    public void reportsCounterValues() throws Exception {
        final Counter counter = mock(Counter.class);
        when(counter.getCount()).thenReturn(100L);

        reporter.report(map(),
                map("test.counter", counter),
                map(),
                map(),
                map());

        assertThat(consoleOutput())
                .isEqualTo(lines(
                        dateHeader,
                        "",
                        "-- Counters --------------------------------------------------------------------",
                        "test.counter",
                        "             count = 100",
                        "",
                        ""
                ));
    }

    @Test
    public void reportsHistogramValues() throws Exception {
        final Histogram histogram = mock(Histogram.class);
        when(histogram.getCount()).thenReturn(1L);

        final Snapshot snapshot = mock(Snapshot.class);
        when(snapshot.getMax()).thenReturn(2L);
        when(snapshot.getMean()).thenReturn(3.0);
        when(snapshot.getMin()).thenReturn(4L);
        when(snapshot.getStdDev()).thenReturn(5.0);
        when(snapshot.getMedian()).thenReturn(6.0);
        when(snapshot.get75thPercentile()).thenReturn(7.0);
        when(snapshot.get95thPercentile()).thenReturn(8.0);
        when(snapshot.get98thPercentile()).thenReturn(9.0);
        when(snapshot.get99thPercentile()).thenReturn(10.0);
        when(snapshot.get999thPercentile()).thenReturn(11.0);

        when(histogram.getSnapshot()).thenReturn(snapshot);

        reporter.report(map(),
                map(),
                map("test.histogram", histogram),
                map(),
                map());

        assertThat(consoleOutput())
                .isEqualTo(lines(
                        dateHeader,
                        "",
                        "-- Histograms ------------------------------------------------------------------",
                        "test.histogram",
                        "             count = 1",
                        "               min = 4",
                        "               max = 2",
                        "              mean = 3.00",
                        "            stddev = 5.00",
                        "            median = 6.00",
                        "              75% <= 7.00",
                        "              95% <= 8.00",
                        "              98% <= 9.00",
                        "              99% <= 10.00",
                        "            99.9% <= 11.00",
                        "",
                        ""
                ));
    }

    @Test
    public void reportsMeterValues() throws Exception {
        final Meter meter = mock(Meter.class);
        when(meter.getCount()).thenReturn(1L);
        when(meter.getMeanRate()).thenReturn(2.0);
        when(meter.getOneMinuteRate()).thenReturn(3.0);
        when(meter.getFiveMinuteRate()).thenReturn(4.0);
        when(meter.getFifteenMinuteRate()).thenReturn(5.0);

        reporter.report(map(),
                map(),
                map(),
                map("test.meter", meter),
                map());

        assertThat(consoleOutput())
                .isEqualTo(lines(
                        dateHeader,
                        "",
                        "-- Meters ----------------------------------------------------------------------",
                        "test.meter",
                        "             count = 1",
                        "         mean rate = 2.00 events/second",
                        "     1-minute rate = 3.00 events/second",
                        "     5-minute rate = 4.00 events/second",
                        "    15-minute rate = 5.00 events/second",
                        "",
                        ""
                ));
    }

    @Test
    public void reportsTimerValues() throws Exception {
        final Timer timer = mock(Timer.class);
        when(timer.getCount()).thenReturn(1L);
        when(timer.getMeanRate()).thenReturn(2.0);
        when(timer.getOneMinuteRate()).thenReturn(3.0);
        when(timer.getFiveMinuteRate()).thenReturn(4.0);
        when(timer.getFifteenMinuteRate()).thenReturn(5.0);

        final Snapshot snapshot = mock(Snapshot.class);
        when(snapshot.getMax()).thenReturn(TimeUnit.MILLISECONDS.toNanos(100));
        when(snapshot.getMean()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(200));
        when(snapshot.getMin()).thenReturn(TimeUnit.MILLISECONDS.toNanos(300));
        when(snapshot.getStdDev()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(400));
        when(snapshot.getMedian()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(500));
        when(snapshot.get75thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(600));
        when(snapshot.get95thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(700));
        when(snapshot.get98thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(800));
        when(snapshot.get99thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(900));
        when(snapshot.get999thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS
                .toNanos(1000));

        when(timer.getSnapshot()).thenReturn(snapshot);

        reporter.report(map(),
                map(),
                map(),
                map(),
                map("test.another.timer", timer));

        assertThat(consoleOutput())
                .isEqualTo(lines(
                        dateHeader,
                        "",
                        "-- Timers ----------------------------------------------------------------------",
                        "test.another.timer",
                        "             count = 1",
                        "         mean rate = 2.00 calls/second",
                        "     1-minute rate = 3.00 calls/second",
                        "     5-minute rate = 4.00 calls/second",
                        "    15-minute rate = 5.00 calls/second",
                        "               min = 300.00 milliseconds",
                        "               max = 100.00 milliseconds",
                        "              mean = 200.00 milliseconds",
                        "            stddev = 400.00 milliseconds",
                        "            median = 500.00 milliseconds",
                        "              75% <= 600.00 milliseconds",
                        "              95% <= 700.00 milliseconds",
                        "              98% <= 800.00 milliseconds",
                        "              99% <= 900.00 milliseconds",
                        "            99.9% <= 1000.00 milliseconds",
                        "",
                        ""
                ));
    }

    @Test
    public void reportMeterWithDisabledAttributes() throws Exception {
        Set<MetricAttribute> disabledMetricAttributes = EnumSet.of(MetricAttribute.M15_RATE, MetricAttribute.M5_RATE, MetricAttribute.COUNT);

        final ConsoleReporter customReporter = ConsoleReporter.forRegistry(registry)
                .outputTo(output)
                .formattedFor(locale)
                .withClock(clock)
                .formattedFor(timeZone)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .filter(MetricFilter.ALL)
                .disabledMetricAttributes(disabledMetricAttributes)
                .build();

        final Meter meter = mock(Meter.class);
        when(meter.getCount()).thenReturn(1L);
        when(meter.getMeanRate()).thenReturn(2.0);
        when(meter.getOneMinuteRate()).thenReturn(3.0);
        when(meter.getFiveMinuteRate()).thenReturn(4.0);
        when(meter.getFifteenMinuteRate()).thenReturn(5.0);

        customReporter.report(map(),
                map(),
                map(),
                map("test.meter", meter),
                map());

        assertThat(consoleOutput())
                .isEqualTo(lines(
                        dateHeader,
                        "",
                        "-- Meters ----------------------------------------------------------------------",
                        "test.meter",
                        "         mean rate = 2.00 events/second",
                        "     1-minute rate = 3.00 events/second",
                        "",
                        ""
                ));
    }

    @Test
    public void reportTimerWithDisabledAttributes() throws Exception {
        Set<MetricAttribute> disabledMetricAttributes = EnumSet.of(MetricAttribute.P50, MetricAttribute.P999, MetricAttribute.M5_RATE, MetricAttribute.MAX);

        final ConsoleReporter customReporter = ConsoleReporter.forRegistry(registry)
                .outputTo(output)
                .formattedFor(locale)
                .withClock(clock)
                .formattedFor(timeZone)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .filter(MetricFilter.ALL)
                .disabledMetricAttributes(disabledMetricAttributes)
                .build();

        final Timer timer = mock(Timer.class);
        when(timer.getCount()).thenReturn(1L);
        when(timer.getMeanRate()).thenReturn(2.0);
        when(timer.getOneMinuteRate()).thenReturn(3.0);
        when(timer.getFiveMinuteRate()).thenReturn(4.0);
        when(timer.getFifteenMinuteRate()).thenReturn(5.0);

        final Snapshot snapshot = mock(Snapshot.class);
        when(snapshot.getMax()).thenReturn(TimeUnit.MILLISECONDS.toNanos(100));
        when(snapshot.getMean()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(200));
        when(snapshot.getMin()).thenReturn(TimeUnit.MILLISECONDS.toNanos(300));
        when(snapshot.getStdDev()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(400));
        when(snapshot.getMedian()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(500));
        when(snapshot.get75thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(600));
        when(snapshot.get95thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(700));
        when(snapshot.get98thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(800));
        when(snapshot.get99thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(900));
        when(snapshot.get999thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS
                .toNanos(1000));

        when(timer.getSnapshot()).thenReturn(snapshot);

        customReporter.report(map(),
                map(),
                map(),
                map(),
                map("test.another.timer", timer));

        assertThat(consoleOutput())
                .isEqualTo(lines(
                        dateHeader,
                        "",
                        "-- Timers ----------------------------------------------------------------------",
                        "test.another.timer",
                        "             count = 1",
                        "         mean rate = 2.00 calls/second",
                        "     1-minute rate = 3.00 calls/second",
                        "    15-minute rate = 5.00 calls/second",
                        "               min = 300.00 milliseconds",
                        "              mean = 200.00 milliseconds",
                        "            stddev = 400.00 milliseconds",
                        "              75% <= 600.00 milliseconds",
                        "              95% <= 700.00 milliseconds",
                        "              98% <= 800.00 milliseconds",
                        "              99% <= 900.00 milliseconds",
                        "",
                        ""
                ));
    }

    @Test
    public void reportHistogramWithDisabledAttributes() throws Exception {
        Set<MetricAttribute> disabledMetricAttributes = EnumSet.of(MetricAttribute.MIN, MetricAttribute.MAX, MetricAttribute.STDDEV, MetricAttribute.P95);

        final ConsoleReporter customReporter = ConsoleReporter.forRegistry(registry)
                .outputTo(output)
                .formattedFor(locale)
                .withClock(clock)
                .formattedFor(timeZone)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .filter(MetricFilter.ALL)
                .disabledMetricAttributes(disabledMetricAttributes)
                .build();

        final Histogram histogram = mock(Histogram.class);
        when(histogram.getCount()).thenReturn(1L);

        final Snapshot snapshot = mock(Snapshot.class);
        when(snapshot.getMax()).thenReturn(2L);
        when(snapshot.getMean()).thenReturn(3.0);
        when(snapshot.getMin()).thenReturn(4L);
        when(snapshot.getStdDev()).thenReturn(5.0);
        when(snapshot.getMedian()).thenReturn(6.0);
        when(snapshot.get75thPercentile()).thenReturn(7.0);
        when(snapshot.get95thPercentile()).thenReturn(8.0);
        when(snapshot.get98thPercentile()).thenReturn(9.0);
        when(snapshot.get99thPercentile()).thenReturn(10.0);
        when(snapshot.get999thPercentile()).thenReturn(11.0);

        when(histogram.getSnapshot()).thenReturn(snapshot);

        customReporter.report(map(),
                map(),
                map("test.histogram", histogram),
                map(),
                map());

        assertThat(consoleOutput())
                .isEqualTo(lines(
                        dateHeader,
                        "",
                        "-- Histograms ------------------------------------------------------------------",
                        "test.histogram",
                        "             count = 1",
                        "              mean = 3.00",
                        "            median = 6.00",
                        "              75% <= 7.00",
                        "              98% <= 9.00",
                        "              99% <= 10.00",
                        "            99.9% <= 11.00",
                        "",
                        ""
                ));
    }

    private String lines(String... lines) {
        final StringBuilder builder = new StringBuilder();
        for (String line : lines) {
            builder.append(line).append(String.format("%n"));
        }
        return builder.toString();
    }

    private String consoleOutput() throws UnsupportedEncodingException {
        return bytes.toString("UTF-8");
    }

    private <T> SortedMap<String, T> map() {
        return new TreeMap<>();
    }

    private <T> SortedMap<String, T> map(String name, T metric) {
        final TreeMap<String, T> map = new TreeMap<>();
        map.put(name, metric);
        return map;
    }
}
