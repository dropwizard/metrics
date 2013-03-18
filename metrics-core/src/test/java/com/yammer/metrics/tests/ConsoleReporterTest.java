package com.yammer.metrics.tests;

import com.yammer.metrics.*;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConsoleReporterTest {
    private final MetricRegistry registry = mock(MetricRegistry.class);
    private final Clock clock = mock(Clock.class);
    private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    private final PrintStream output = new PrintStream(bytes);
    private final ConsoleReporter reporter = new ConsoleReporter(registry,
                                                                 output,
                                                                 Locale.US,
                                                                 clock,
                                                                 TimeZone.getTimeZone("PST"),
                                                                 TimeUnit.SECONDS,
                                                                 TimeUnit.MILLISECONDS);

    @Before
    public void setUp() throws Exception {
        when(clock.getTime()).thenReturn(1363568676000L);
    }

    @Test
    public void reportsGaugeValues() throws Exception {
        final Gauge gauge = mock(Gauge.class);
        when(gauge.getValue()).thenReturn(1);

        reporter.report(map("gauge", gauge),
                        this.<Counter>map(),
                        this.<Histogram>map(),
                        this.<Meter>map(),
                        this.<Timer>map());

        assertThat(consoleOutput())
                .isEqualTo(lines(
                        "3/17/13 6:04:36 PM =============================================================",
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

        reporter.report(this.<Gauge>map(),
                        map("test.counter", counter),
                        this.<Histogram>map(),
                        this.<Meter>map(),
                        this.<Timer>map());

        assertThat(consoleOutput())
                .isEqualTo(lines(
                        "3/17/13 6:04:36 PM =============================================================",
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
        when(histogram.getMax()).thenReturn(2L);
        when(histogram.getMean()).thenReturn(3.0);
        when(histogram.getMin()).thenReturn(4L);
        when(histogram.getStdDev()).thenReturn(5.0);

        final Snapshot snapshot = mock(Snapshot.class);
        when(snapshot.getMedian()).thenReturn(6.0);
        when(snapshot.get75thPercentile()).thenReturn(7.0);
        when(snapshot.get95thPercentile()).thenReturn(8.0);
        when(snapshot.get98thPercentile()).thenReturn(9.0);
        when(snapshot.get99thPercentile()).thenReturn(10.0);
        when(snapshot.get999thPercentile()).thenReturn(11.0);

        when(histogram.getSnapshot()).thenReturn(snapshot);

        reporter.report(this.<Gauge>map(),
                        this.<Counter>map(),
                        map("test.histogram", histogram),
                        this.<Meter>map(),
                        this.<Timer>map());

        assertThat(consoleOutput())
                .isEqualTo(lines(
                        "3/17/13 6:04:36 PM =============================================================",
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

        reporter.report(this.<Gauge>map(),
                        this.<Counter>map(),
                        this.<Histogram>map(),
                        map("test.meter", meter),
                        this.<Timer>map());

        assertThat(consoleOutput())
                .isEqualTo(lines(
                        "3/17/13 6:04:36 PM =============================================================",
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
        when(timer.getMax()).thenReturn(TimeUnit.MILLISECONDS.toNanos(100));
        when(timer.getMean()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(200));
        when(timer.getMin()).thenReturn(TimeUnit.MILLISECONDS.toNanos(300));
        when(timer.getStdDev()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(400));

        when(timer.getMeanRate()).thenReturn(2.0);
        when(timer.getOneMinuteRate()).thenReturn(3.0);
        when(timer.getFiveMinuteRate()).thenReturn(4.0);
        when(timer.getFifteenMinuteRate()).thenReturn(5.0);

        final Snapshot snapshot = mock(Snapshot.class);
        when(snapshot.getMedian()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(500));
        when(snapshot.get75thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(600));
        when(snapshot.get95thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(700));
        when(snapshot.get98thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(800));
        when(snapshot.get99thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(900));
        when(snapshot.get999thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS
                                                                        .toNanos(1000));

        when(timer.getSnapshot()).thenReturn(snapshot);

        reporter.report(this.<Gauge>map(),
                        this.<Counter>map(),
                        this.<Histogram>map(),
                        this.<Meter>map(),
                        map("test.another.timer", timer));

        assertThat(consoleOutput())
                .isEqualTo(lines(
                        "3/17/13 6:04:36 PM =============================================================",
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
        return new TreeMap<String, T>();
    }

    private <T> SortedMap<String, T> map(String name, T metric) {
        final TreeMap<String, T> map = new TreeMap<String, T>();
        map.put(name, metric);
        return map;
    }
}
