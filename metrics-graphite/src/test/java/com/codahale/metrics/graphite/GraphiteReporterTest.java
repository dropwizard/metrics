package com.codahale.metrics.graphite;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricAttribute;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
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
        reporter.report(map("gauge", gauge("value")),
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
        reporter.report(map("gauge", gauge((byte) 1)),
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
        reporter.report(map("gauge", gauge((short) 1)),
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
        reporter.report(map("gauge", gauge(1)),
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
        reporter.report(map("gauge", gauge(1L)),
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
        reporter.report(map("gauge", gauge(1.1f)),
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
        reporter.report(map("gauge", gauge(1.1)),
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
            reportGaugeValue(graphiteReporter, 1.13574);
            verifyGraphiteSentCorrectMetricValue("prefix.gauge", "1.1357", timestamp);
            verifyNoMoreInteractions(graphite);
        }
    }

    @Test
    public void reportDoubleGaugeValuesUsingCustomFormatter() throws Exception {
        DecimalFormat formatter = new DecimalFormat("##.##########", DecimalFormatSymbols.getInstance(Locale.US));

        try (GraphiteReporter graphiteReporter = GraphiteReporter.forRegistry(registry)
                .withClock(clock)
                .prefixedWith("prefix")
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .filter(MetricFilter.ALL)
                .disabledMetricAttributes(Collections.emptySet())
                .withFloatingPointFormatter(formatter::format)
                .build(graphite)) {
            reportGaugeValue(graphiteReporter, 0.000045322);
            verifyGraphiteSentCorrectMetricValue("prefix.gauge", "0.000045322", timestamp);
            verifyNoMoreInteractions(graphite);
        }
    }

    private void reportGaugeValue(GraphiteReporter graphiteReporter, double value) {
        graphiteReporter.report(map("gauge", gauge(value)),
                map(),
                map(),
                map(),
                map());
    }

    private void verifyGraphiteSentCorrectMetricValue(String metricName, String value, long timestamp) throws Exception {
        final InOrder inOrder = inOrder(graphite);
        inOrder.verify(graphite).connect();
        inOrder.verify(graphite).send(metricName, value, timestamp);
        inOrder.verify(graphite).flush();
        inOrder.verify(graphite).close();
    }

    @Test
    public void reportsBooleanGaugeValues() throws Exception {
        reporter.report(map("gauge", gauge(true)),
            map(),
            map(),
            map(),
            map());

        reporter.report(map("gauge", gauge(false)),
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
            map("counter", counter),
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
            map("histogram", histogram),
            map(),
            map());

        final InOrder inOrder = inOrder(graphite);
        inOrder.verify(graphite).connect();
        inOrder.verify(graphite).send("prefix.histogram.count", "1", timestamp);
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
        when(meter.getOneMinuteRate()).thenReturn(2.0);
        when(meter.getFiveMinuteRate()).thenReturn(3.0);
        when(meter.getFifteenMinuteRate()).thenReturn(4.0);
        when(meter.getMeanRate()).thenReturn(5.0);

        reporter.report(map(),
            map(),
            map(),
            map("meter", meter),
            map());

        final InOrder inOrder = inOrder(graphite);
        inOrder.verify(graphite).connect();
        inOrder.verify(graphite).send("prefix.meter.count", "1", timestamp);
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
        when(meter.getOneMinuteRate()).thenReturn(2.0);
        when(meter.getFiveMinuteRate()).thenReturn(3.0);
        when(meter.getFifteenMinuteRate()).thenReturn(4.0);
        when(meter.getMeanRate()).thenReturn(5.0);

        minuteRateReporter.report(this.map(),
            this.map(),
            this.map(),
            this.map("meter", meter),
            this.map());

        final InOrder inOrder = inOrder(graphite);
        inOrder.verify(graphite).connect();
        inOrder.verify(graphite).send("prefix.meter.count", "1", timestamp);
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
            map("timer", timer));

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
        reporter.report(map("gauge", gauge(1)),
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
            map("counter", counter),
            map(),
            map("meter", meter),
            map());

        final InOrder inOrder = inOrder(graphite);
        inOrder.verify(graphite).connect();
        inOrder.verify(graphite).send("prefix.counter.count", "11", timestamp);
        inOrder.verify(graphite).send("prefix.meter.count", "1", timestamp);
        inOrder.verify(graphite).send("prefix.meter.m1_rate", "2.00", timestamp);
        inOrder.verify(graphite).send("prefix.meter.mean_rate", "5.00", timestamp);
        inOrder.verify(graphite).flush();
        inOrder.verify(graphite).close();

        verifyNoMoreInteractions(graphite);
    }

    @Test
    public void sendsMetricAttributesAsTagsIfEnabled() throws Exception {
        final Counter counter = mock(Counter.class);
        when(counter.getCount()).thenReturn(100L);

        getReporterThatSendsMetricAttributesAsTags().report(map(),
                map("counter", counter),
                map(),
                map(),
                map());

        final InOrder inOrder = inOrder(graphite);
        inOrder.verify(graphite).connect();
        inOrder.verify(graphite).send("prefix.counter;metricattribute=count", "100", timestamp);
        inOrder.verify(graphite).flush();
        inOrder.verify(graphite).close();

        verifyNoMoreInteractions(graphite);
    }

    private GraphiteReporter getReporterWithCustomFormat() {
        return new GraphiteReporter(registry, graphite, clock, "prefix",
            TimeUnit.SECONDS, TimeUnit.MICROSECONDS, MetricFilter.ALL, null, false,
            Collections.emptySet(), false) {
            @Override
            protected String format(double v) {
                return String.format(Locale.US, "%4.4f", v);
            }
        };
    }


    private GraphiteReporter getReporterThatSendsMetricAttributesAsTags() {
        return GraphiteReporter.forRegistry(registry)
                .withClock(clock)
                .prefixedWith("prefix")
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .filter(MetricFilter.ALL)
                .disabledMetricAttributes(Collections.emptySet())
                .addMetricAttributesAsTags(true)
                .build(graphite);
    }

    private <T> SortedMap<String, T> map() {
        return new TreeMap<>();
    }

    private <T> SortedMap<String, T> map(String name, T metric) {
        final TreeMap<String, T> map = new TreeMap<>();
        map.put(name, metric);
        return map;
    }

    private <T> Gauge<T> gauge(T value) {
        return () -> value;
    }
}
