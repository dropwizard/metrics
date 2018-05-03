package io.dropwizard.metrics5.graphite;

import io.dropwizard.metrics5.Clock;
import io.dropwizard.metrics5.Counter;
import io.dropwizard.metrics5.Gauge;
import io.dropwizard.metrics5.Histogram;
import io.dropwizard.metrics5.Meter;
import io.dropwizard.metrics5.MetricAttribute;
import io.dropwizard.metrics5.MetricFilter;
import io.dropwizard.metrics5.MetricName;
import io.dropwizard.metrics5.MetricRegistry;
import io.dropwizard.metrics5.Snapshot;
import io.dropwizard.metrics5.Timer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.net.UnknownHostException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class GraphiteReporterTest {
    private static final MetricName GAUGE = MetricName.build("gauge");
    private static final MetricName METER = MetricName.build("meter");
    private static final MetricName COUNTER = MetricName.build("counter");

    private final long timestamp = 1000198;
    private final Clock clock = mock(Clock.class);
    private final Graphite graphite = mock(Graphite.class);
    private final MetricRegistry registry = mock(MetricRegistry.class);
    private final GraphiteReporter reporter = GraphiteReporter.forRegistry(registry)
        .withClock(clock)
        .prefixedWith("prefix")
        .convertRatesTo(TimeUnit.SECONDS)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .filter(MetricFilter.ALL)
        .disabledMetricAttributes(Collections.emptySet())
        .build(graphite);

    private final GraphiteReporter minuteRateReporter = GraphiteReporter
        .forRegistry(registry)
        .withClock(clock)
        .prefixedWith("prefix")
        .convertRatesTo(TimeUnit.MINUTES)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .filter(MetricFilter.ALL)
        .disabledMetricAttributes(Collections.emptySet())
        .build(graphite);

    @Before
    public void setUp() {
        when(clock.getTime()).thenReturn(timestamp * 1000);
    }

    @Test
    public void doesNotReportStringGaugeValues() throws Exception {
        reporter.report(map(GAUGE, gauge("value")),
            map(),
            map(),
            map(),
            map());

        final InOrder inOrder = inOrder(graphite);
        inOrder.verify(graphite).connect();
        inOrder.verify(graphite, never()).send("prefix.gauge", "value", timestamp);
        inOrder.verify(graphite).flush();
        inOrder.verify(graphite).close();

        verifyNoMoreInteractions(graphite);
    }

    @Test
    public void reportsByteGaugeValues() throws Exception {
        reporter.report(map(GAUGE, gauge((byte) 1)),
            map(),
            map(),
            map(),
            map());

        final InOrder inOrder = inOrder(graphite);
        inOrder.verify(graphite).connect();
        inOrder.verify(graphite).send("prefix.gauge", "1", timestamp);
        inOrder.verify(graphite).flush();
        inOrder.verify(graphite).close();

        verifyNoMoreInteractions(graphite);
    }

    @Test
    public void reportsShortGaugeValues() throws Exception {
        reporter.report(map(GAUGE, gauge((short) 1)),
            map(),
            map(),
            map(),
            map());

        final InOrder inOrder = inOrder(graphite);
        inOrder.verify(graphite).connect();
        inOrder.verify(graphite).send("prefix.gauge", "1", timestamp);
        inOrder.verify(graphite).flush();
        inOrder.verify(graphite).close();

        verifyNoMoreInteractions(graphite);
    }

    @Test
    public void reportsIntegerGaugeValues() throws Exception {
        reporter.report(map(GAUGE, gauge(1)),
            map(),
            map(),
            map(),
            map());

        final InOrder inOrder = inOrder(graphite);
        inOrder.verify(graphite).connect();
        inOrder.verify(graphite).send("prefix.gauge", "1", timestamp);
        inOrder.verify(graphite).flush();
        inOrder.verify(graphite).close();

        verifyNoMoreInteractions(graphite);
    }

    @Test
    public void reportsLongGaugeValues() throws Exception {
        reporter.report(map(GAUGE, gauge(1L)),
            map(),
            map(),
            map(),
            map());

        final InOrder inOrder = inOrder(graphite);
        inOrder.verify(graphite).connect();
        inOrder.verify(graphite).send("prefix.gauge", "1", timestamp);
        inOrder.verify(graphite).flush();
        inOrder.verify(graphite).close();

        verifyNoMoreInteractions(graphite);
    }

    @Test
    public void reportsFloatGaugeValues() throws Exception {
        reporter.report(map(GAUGE, gauge(1.1f)),
            map(),
            map(),
            map(),
            map());

        final InOrder inOrder = inOrder(graphite);
        inOrder.verify(graphite).connect();
        inOrder.verify(graphite).send("prefix.gauge", "1.10", timestamp);
        inOrder.verify(graphite).flush();
        inOrder.verify(graphite).close();

        verifyNoMoreInteractions(graphite);
    }

    @Test
    public void reportsDoubleGaugeValues() throws Exception {
        reporter.report(map(GAUGE, gauge(1.1)),
            map(),
            map(),
            map(),
            map());

        final InOrder inOrder = inOrder(graphite);
        inOrder.verify(graphite).connect();
        inOrder.verify(graphite).send("prefix.gauge", "1.10", timestamp);
        inOrder.verify(graphite).flush();
        inOrder.verify(graphite).close();

        verifyNoMoreInteractions(graphite);
    }

    @Test
    public void reportsDoubleGaugeValuesWithCustomFormat() throws Exception {
        try (final GraphiteReporter graphiteReporter = getReporterWithCustomFormat()) {
            graphiteReporter.report(map(GAUGE, gauge(1.13574)),
                map(),
                map(),
                map(),
                map());

            final InOrder inOrder = inOrder(graphite);
            inOrder.verify(graphite).connect();
            inOrder.verify(graphite).send("prefix.gauge", "1.1357", timestamp);
            inOrder.verify(graphite).flush();
            inOrder.verify(graphite).close();

            verifyNoMoreInteractions(graphite);
        }
    }

    @Test
    public void reportsBooleanGaugeValues() throws Exception {
        reporter.report(map(GAUGE, gauge(true)),
            map(),
            map(),
            map(),
            map());

        reporter.report(map(GAUGE, gauge(false)),
            map(),
            map(),
            map(),
            map());
        final InOrder inOrder = inOrder(graphite);
        inOrder.verify(graphite).connect();
        inOrder.verify(graphite).send("prefix.gauge", "1", timestamp);
        inOrder.verify(graphite).flush();
        inOrder.verify(graphite).close();
        inOrder.verify(graphite).connect();
        inOrder.verify(graphite).send("prefix.gauge", "0", timestamp);
        inOrder.verify(graphite).flush();
        inOrder.verify(graphite).close();

        verifyNoMoreInteractions(graphite);
    }

    @Test
    public void reportsCounters() throws Exception {
        final Counter counter = mock(Counter.class);
        when(counter.getCount()).thenReturn(100L);

        reporter.report(map(),
            map(COUNTER, counter),
            map(),
            map(),
            map());

        final InOrder inOrder = inOrder(graphite);
        inOrder.verify(graphite).connect();
        inOrder.verify(graphite).send("prefix.counter.count", "100", timestamp);
        inOrder.verify(graphite).flush();
        inOrder.verify(graphite).close();

        verifyNoMoreInteractions(graphite);
    }

    @Test
    public void reportsHistograms() throws Exception {
        final Histogram histogram = mock(Histogram.class);
        when(histogram.getCount()).thenReturn(1L);
        when(histogram.getSum()).thenReturn(12L);

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
            map(MetricName.build("histogram"), histogram),
            map(),
            map());

        final InOrder inOrder = inOrder(graphite);
        inOrder.verify(graphite).connect();
        inOrder.verify(graphite).send("prefix.histogram.count", "1", timestamp);
        inOrder.verify(graphite).send("prefix.histogram.sum", "12", timestamp);
        inOrder.verify(graphite).send("prefix.histogram.max", "2", timestamp);
        inOrder.verify(graphite).send("prefix.histogram.mean", "3.00", timestamp);
        inOrder.verify(graphite).send("prefix.histogram.min", "4", timestamp);
        inOrder.verify(graphite).send("prefix.histogram.stddev", "5.00", timestamp);
        inOrder.verify(graphite).send("prefix.histogram.p50", "6.00", timestamp);
        inOrder.verify(graphite).send("prefix.histogram.p75", "7.00", timestamp);
        inOrder.verify(graphite).send("prefix.histogram.p95", "8.00", timestamp);
        inOrder.verify(graphite).send("prefix.histogram.p98", "9.00", timestamp);
        inOrder.verify(graphite).send("prefix.histogram.p99", "10.00", timestamp);
        inOrder.verify(graphite).send("prefix.histogram.p999", "11.00", timestamp);
        inOrder.verify(graphite).flush();
        inOrder.verify(graphite).close();

        verifyNoMoreInteractions(graphite);
    }

    @Test
    public void reportsMeters() throws Exception {
        final Meter meter = mock(Meter.class);
        when(meter.getCount()).thenReturn(1L);
        when(meter.getSum()).thenReturn(6L);
        when(meter.getOneMinuteRate()).thenReturn(2.0);
        when(meter.getFiveMinuteRate()).thenReturn(3.0);
        when(meter.getFifteenMinuteRate()).thenReturn(4.0);
        when(meter.getMeanRate()).thenReturn(5.0);

        reporter.report(map(),
            map(),
            map(),
            map(METER, meter),
            map());

        final InOrder inOrder = inOrder(graphite);
        inOrder.verify(graphite).connect();
        inOrder.verify(graphite).send("prefix.meter.count", "1", timestamp);
        inOrder.verify(graphite).send("prefix.meter.sum", "6.00", timestamp);
        inOrder.verify(graphite).send("prefix.meter.m1_rate", "2.00", timestamp);
        inOrder.verify(graphite).send("prefix.meter.m5_rate", "3.00", timestamp);
        inOrder.verify(graphite).send("prefix.meter.m15_rate", "4.00", timestamp);
        inOrder.verify(graphite).send("prefix.meter.mean_rate", "5.00", timestamp);
        inOrder.verify(graphite).flush();
        inOrder.verify(graphite).close();

        verifyNoMoreInteractions(graphite);
    }

    @Test
    public void reportsMetersInMinutes() throws Exception {
        final Meter meter = mock(Meter.class);
        when(meter.getCount()).thenReturn(1L);
        when(meter.getSum()).thenReturn(6L);
        when(meter.getOneMinuteRate()).thenReturn(2.0);
        when(meter.getFiveMinuteRate()).thenReturn(3.0);
        when(meter.getFifteenMinuteRate()).thenReturn(4.0);
        when(meter.getMeanRate()).thenReturn(5.0);

        minuteRateReporter.report(this.map(),
            this.map(),
            this.map(),
            this.map(METER, meter),
            this.map());

        final InOrder inOrder = inOrder(graphite);
        inOrder.verify(graphite).connect();
        inOrder.verify(graphite).send("prefix.meter.count", "1", timestamp);
        inOrder.verify(graphite).send("prefix.meter.sum", "6.00", timestamp);
        inOrder.verify(graphite).send("prefix.meter.m1_rate", "120.00", timestamp);
        inOrder.verify(graphite).send("prefix.meter.m5_rate", "180.00", timestamp);
        inOrder.verify(graphite).send("prefix.meter.m15_rate", "240.00", timestamp);
        inOrder.verify(graphite).send("prefix.meter.mean_rate", "300.00", timestamp);
        inOrder.verify(graphite).flush();
        inOrder.verify(graphite).close();

        verifyNoMoreInteractions(graphite);
    }

    @Test
    public void reportsTimers() throws Exception {
        final Timer timer = mock(Timer.class);
        when(timer.getCount()).thenReturn(1L);
        when(timer.getSum()).thenReturn(TimeUnit.MILLISECONDS.toNanos(6));
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
            map(MetricName.build("timer"), timer));

        final InOrder inOrder = inOrder(graphite);
        inOrder.verify(graphite).connect();
        inOrder.verify(graphite).send("prefix.timer.max", "100.00", timestamp);
        inOrder.verify(graphite).send("prefix.timer.mean", "200.00", timestamp);
        inOrder.verify(graphite).send("prefix.timer.min", "300.00", timestamp);
        inOrder.verify(graphite).send("prefix.timer.stddev", "400.00", timestamp);
        inOrder.verify(graphite).send("prefix.timer.p50", "500.00", timestamp);
        inOrder.verify(graphite).send("prefix.timer.p75", "600.00", timestamp);
        inOrder.verify(graphite).send("prefix.timer.p95", "700.00", timestamp);
        inOrder.verify(graphite).send("prefix.timer.p98", "800.00", timestamp);
        inOrder.verify(graphite).send("prefix.timer.p99", "900.00", timestamp);
        inOrder.verify(graphite).send("prefix.timer.p999", "1000.00", timestamp);
        inOrder.verify(graphite).send("prefix.timer.count", "1", timestamp);
        inOrder.verify(graphite).send("prefix.timer.sum", "6.00", timestamp);
        inOrder.verify(graphite).send("prefix.timer.m1_rate", "3.00", timestamp);
        inOrder.verify(graphite).send("prefix.timer.m5_rate", "4.00", timestamp);
        inOrder.verify(graphite).send("prefix.timer.m15_rate", "5.00", timestamp);
        inOrder.verify(graphite).send("prefix.timer.mean_rate", "2.00", timestamp);
        inOrder.verify(graphite).flush();
        inOrder.verify(graphite).close();

        verifyNoMoreInteractions(graphite);

        reporter.close();
    }

    @Test
    public void closesConnectionIfGraphiteIsUnavailable() throws Exception {
        doThrow(new UnknownHostException("UNKNOWN-HOST")).when(graphite).connect();
        reporter.report(map(GAUGE, gauge(1)),
            map(),
            map(),
            map(),
            map());

        final InOrder inOrder = inOrder(graphite);
        inOrder.verify(graphite).connect();
        inOrder.verify(graphite).close();


        verifyNoMoreInteractions(graphite);
    }

    @Test
    public void closesConnectionOnReporterStop() throws Exception {
        reporter.stop();

        verify(graphite).close();

        verifyNoMoreInteractions(graphite);
    }

    @Test
    public void disabledMetricsAttribute() throws Exception {
        final Meter meter = mock(Meter.class);
        when(meter.getCount()).thenReturn(1L);
        when(meter.getSum()).thenReturn(6L);
        when(meter.getOneMinuteRate()).thenReturn(2.0);
        when(meter.getFiveMinuteRate()).thenReturn(3.0);
        when(meter.getFifteenMinuteRate()).thenReturn(4.0);
        when(meter.getMeanRate()).thenReturn(5.0);

        final Counter counter = mock(Counter.class);
        when(counter.getCount()).thenReturn(11L);

        Set<MetricAttribute> disabledMetricAttributes = EnumSet.of(MetricAttribute.M15_RATE, MetricAttribute.M5_RATE);
        GraphiteReporter reporterWithdisabledMetricAttributes = GraphiteReporter.forRegistry(registry)
            .withClock(clock)
            .prefixedWith("prefix")
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .filter(MetricFilter.ALL)
            .disabledMetricAttributes(disabledMetricAttributes)
            .build(graphite);
        reporterWithdisabledMetricAttributes.report(map(),
            map(COUNTER, counter),
            map(),
            map(METER, meter),
            map());

        final InOrder inOrder = inOrder(graphite);
        inOrder.verify(graphite).connect();
        inOrder.verify(graphite).send("prefix.counter.count", "11", timestamp);
        inOrder.verify(graphite).send("prefix.meter.count", "1", timestamp);
        inOrder.verify(graphite).send("prefix.meter.sum", "6.00", timestamp);
        inOrder.verify(graphite).send("prefix.meter.m1_rate", "2.00", timestamp);
        inOrder.verify(graphite).send("prefix.meter.mean_rate", "5.00", timestamp);
        inOrder.verify(graphite).flush();
        inOrder.verify(graphite).close();

        verifyNoMoreInteractions(graphite);
    }

    private GraphiteReporter getReporterWithCustomFormat() {
        return new GraphiteReporter(registry, graphite, clock, "prefix",
            TimeUnit.SECONDS, TimeUnit.MICROSECONDS, MetricFilter.ALL, null, false,
            Collections.emptySet()) {
            @Override
            protected String format(double v) {
                return String.format(Locale.US, "%4.4f", v);
            }
        };
    }

    private <T> SortedMap<MetricName, T> map() {
        return new TreeMap<>();
    }

    private <T> SortedMap<MetricName, T> map(MetricName name, T metric) {
        final TreeMap<MetricName, T> map = new TreeMap<>();
        map.put(name, metric);
        return map;
    }

    private <T> Gauge<T> gauge(T value) {
        return () -> value;
    }
}
