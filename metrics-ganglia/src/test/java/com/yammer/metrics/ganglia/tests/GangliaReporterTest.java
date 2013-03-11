package com.yammer.metrics.ganglia.tests;

import com.yammer.metrics.*;
import com.yammer.metrics.ganglia.GangliaReporter;
import info.ganglia.gmetric4j.gmetric.GMetric;
import info.ganglia.gmetric4j.gmetric.GMetricSlope;
import info.ganglia.gmetric4j.gmetric.GMetricType;
import org.junit.Test;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

public class GangliaReporterTest {
    private final GMetric ganglia = mock(GMetric.class);
    private final MetricRegistry registry = mock(MetricRegistry.class);
    private final GangliaReporter reporter = new GangliaReporter(registry,
                                                                 ganglia,
                                                                 60,
                                                                 0,
                                                                 TimeUnit.SECONDS,
                                                                 TimeUnit.MILLISECONDS);

    @Test
    public void reportsStringGaugeValues() throws Exception {
        reporter.report(map("gauge", gauge("value")),
                        this.<Counter>map(),
                        this.<Histogram>map(),
                        this.<Meter>map(),
                        this.<Timer>map());

        verify(ganglia).announce("gauge", "value", GMetricType.STRING, "", GMetricSlope.BOTH, 60, 0, "");
        verifyNoMoreInteractions(ganglia);
    }

    @Test
    public void reportsByteGaugeValues() throws Exception {
        reporter.report(map("gauge", gauge((byte) 1)),
                        this.<Counter>map(),
                        this.<Histogram>map(),
                        this.<Meter>map(),
                        this.<Timer>map());

        verify(ganglia).announce("gauge", "1", GMetricType.INT8, "", GMetricSlope.BOTH, 60, 0, "");
        verifyNoMoreInteractions(ganglia);
    }

    @Test
    public void reportsShortGaugeValues() throws Exception {
        reporter.report(map("gauge", gauge((short) 1)),
                        this.<Counter>map(),
                        this.<Histogram>map(),
                        this.<Meter>map(),
                        this.<Timer>map());

        verify(ganglia).announce("gauge", "1", GMetricType.INT16, "", GMetricSlope.BOTH, 60, 0, "");
        verifyNoMoreInteractions(ganglia);
    }

    @Test
    public void reportsIntegerGaugeValues() throws Exception {
        reporter.report(map("gauge", gauge(1)),
                        this.<Counter>map(),
                        this.<Histogram>map(),
                        this.<Meter>map(),
                        this.<Timer>map());

        verify(ganglia).announce("gauge", "1", GMetricType.INT32, "", GMetricSlope.BOTH, 60, 0, "");
        verifyNoMoreInteractions(ganglia);
    }

    @Test
    public void reportsLongGaugeValues() throws Exception {
        reporter.report(map("gauge", gauge(1L)),
                        this.<Counter>map(),
                        this.<Histogram>map(),
                        this.<Meter>map(),
                        this.<Timer>map());

        verify(ganglia).announce("gauge", "1", GMetricType.DOUBLE, "", GMetricSlope.BOTH, 60, 0, "");
        verifyNoMoreInteractions(ganglia);
    }

    @Test
    public void reportsFloatGaugeValues() throws Exception {
        reporter.report(map("gauge", gauge(1.0f)),
                        this.<Counter>map(),
                        this.<Histogram>map(),
                        this.<Meter>map(),
                        this.<Timer>map());

        verify(ganglia).announce("gauge", "1.0", GMetricType.FLOAT, "", GMetricSlope.BOTH, 60, 0, "");
        verifyNoMoreInteractions(ganglia);
    }

    @Test
    public void reportsDoubleGaugeValues() throws Exception {
        reporter.report(map("gauge", gauge(1.0)),
                        this.<Counter>map(),
                        this.<Histogram>map(),
                        this.<Meter>map(),
                        this.<Timer>map());

        verify(ganglia).announce("gauge", "1.0", GMetricType.DOUBLE, "", GMetricSlope.BOTH, 60, 0, "");
        verifyNoMoreInteractions(ganglia);
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

        verify(ganglia).announce("test.counter.count", "100", GMetricType.DOUBLE, "", GMetricSlope.BOTH, 60, 0, "test");
        verifyNoMoreInteractions(ganglia);
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

        verify(ganglia).announce("test.histogram.count", "1", GMetricType.DOUBLE, "", GMetricSlope.BOTH, 60, 0, "test");
        verify(ganglia).announce("test.histogram.max", "2", GMetricType.DOUBLE, "", GMetricSlope.BOTH, 60, 0, "test");
        verify(ganglia).announce("test.histogram.mean", "3.0", GMetricType.DOUBLE, "", GMetricSlope.BOTH, 60, 0, "test");
        verify(ganglia).announce("test.histogram.min", "4", GMetricType.DOUBLE, "", GMetricSlope.BOTH, 60, 0, "test");
        verify(ganglia).announce("test.histogram.stddev", "5.0", GMetricType.DOUBLE, "", GMetricSlope.BOTH, 60, 0, "test");
        verify(ganglia).announce("test.histogram.p50", "6.0", GMetricType.DOUBLE, "", GMetricSlope.BOTH, 60, 0, "test");
        verify(ganglia).announce("test.histogram.p75", "7.0", GMetricType.DOUBLE, "", GMetricSlope.BOTH, 60, 0, "test");
        verify(ganglia).announce("test.histogram.p95", "8.0", GMetricType.DOUBLE, "", GMetricSlope.BOTH, 60, 0, "test");
        verify(ganglia).announce("test.histogram.p98", "9.0", GMetricType.DOUBLE, "", GMetricSlope.BOTH, 60, 0, "test");
        verify(ganglia).announce("test.histogram.p99", "10.0", GMetricType.DOUBLE, "", GMetricSlope.BOTH, 60, 0, "test");
        verify(ganglia).announce("test.histogram.p999", "11.0", GMetricType.DOUBLE, "", GMetricSlope.BOTH, 60, 0, "test");
        verifyNoMoreInteractions(ganglia);
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

        verify(ganglia).announce("test.meter.count", "1", GMetricType.DOUBLE, "events", GMetricSlope.BOTH, 60, 0, "test");
        verify(ganglia).announce("test.meter.mean_rate", "2.0", GMetricType.DOUBLE, "events/second", GMetricSlope.BOTH, 60, 0, "test");
        verify(ganglia).announce("test.meter.m1_rate", "3.0", GMetricType.DOUBLE, "events/second", GMetricSlope.BOTH, 60, 0, "test");
        verify(ganglia).announce("test.meter.m5_rate", "4.0", GMetricType.DOUBLE, "events/second", GMetricSlope.BOTH, 60, 0, "test");
        verify(ganglia).announce("test.meter.m15_rate", "5.0", GMetricType.DOUBLE, "events/second", GMetricSlope.BOTH, 60, 0, "test");
        verifyNoMoreInteractions(ganglia);
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
        when(snapshot.get999thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(1000));

        when(timer.getSnapshot()).thenReturn(snapshot);

        reporter.report(this.<Gauge>map(),
                        this.<Counter>map(),
                        this.<Histogram>map(),
                        this.<Meter>map(),
                        map("test.another.timer", timer));

        verify(ganglia).announce("test.another.timer.max", "100.0", GMetricType.DOUBLE, "milliseconds", GMetricSlope.BOTH, 60, 0, "test.another");
        verify(ganglia).announce("test.another.timer.mean", "200.0", GMetricType.DOUBLE, "milliseconds", GMetricSlope.BOTH, 60, 0, "test.another");
        verify(ganglia).announce("test.another.timer.min", "300.0", GMetricType.DOUBLE, "milliseconds", GMetricSlope.BOTH, 60, 0, "test.another");
        verify(ganglia).announce("test.another.timer.stddev", "400.0", GMetricType.DOUBLE, "milliseconds", GMetricSlope.BOTH, 60, 0, "test.another");
        verify(ganglia).announce("test.another.timer.p50", "500.0", GMetricType.DOUBLE, "milliseconds", GMetricSlope.BOTH, 60, 0, "test.another");
        verify(ganglia).announce("test.another.timer.p75", "600.0", GMetricType.DOUBLE, "milliseconds", GMetricSlope.BOTH, 60, 0, "test.another");
        verify(ganglia).announce("test.another.timer.p95", "700.0", GMetricType.DOUBLE, "milliseconds", GMetricSlope.BOTH, 60, 0, "test.another");
        verify(ganglia).announce("test.another.timer.p98", "800.0", GMetricType.DOUBLE, "milliseconds", GMetricSlope.BOTH, 60, 0, "test.another");
        verify(ganglia).announce("test.another.timer.p99", "900.0", GMetricType.DOUBLE, "milliseconds", GMetricSlope.BOTH, 60, 0, "test.another");
        verify(ganglia).announce("test.another.timer.p999", "1000.0", GMetricType.DOUBLE, "milliseconds", GMetricSlope.BOTH, 60, 0, "test.another");

        verify(ganglia).announce("test.another.timer.count", "1", GMetricType.DOUBLE, "calls", GMetricSlope.BOTH, 60, 0, "test.another");
        verify(ganglia).announce("test.another.timer.mean_rate", "2.0", GMetricType.DOUBLE, "calls/second", GMetricSlope.BOTH, 60, 0, "test.another");
        verify(ganglia).announce("test.another.timer.m1_rate", "3.0", GMetricType.DOUBLE, "calls/second", GMetricSlope.BOTH, 60, 0, "test.another");
        verify(ganglia).announce("test.another.timer.m5_rate", "4.0", GMetricType.DOUBLE, "calls/second", GMetricSlope.BOTH, 60, 0, "test.another");
        verify(ganglia).announce("test.another.timer.m15_rate", "5.0", GMetricType.DOUBLE, "calls/second", GMetricSlope.BOTH, 60, 0, "test.another");

        verifyNoMoreInteractions(ganglia);
    }

    private <T> SortedMap<String, T> map() {
        return new TreeMap<String, T>();
    }

    private <T> SortedMap<String, T> map(String name, T metric) {
        final TreeMap<String, T> map = new TreeMap<String, T>();
        map.put(name, metric);
        return map;
    }

    private <T> Gauge gauge(T value) {
        final Gauge gauge = mock(Gauge.class);
        when(gauge.getValue()).thenReturn(value);
        return gauge;
    }
}
