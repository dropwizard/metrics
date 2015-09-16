package io.dropwizard.metrics.atsd;

import io.dropwizard.metrics.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.net.UnknownHostException;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

public class AtsdReporterTest {
    private final long timestamp = 1000198000;
    private final String entity = "entityName";
    private final Clock clock = mock(Clock.class);
    private final AtsdTCPSender atsd = mock(AtsdTCPSender.class);
    private final MetricRegistry registry = mock(MetricRegistry.class);
    private final AtsdReporter reporter = AtsdReporter.forRegistry(registry)
            .setEntity(entity)
            .withClock(clock)
            .setMetricPrefix("prefix")
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .filter(MetricFilter.ALL)
            .build(atsd);

    @Before
    public void setUp() throws Exception {
        when(clock.getTime()).thenReturn(timestamp);
    }

    @Test
    public void doesNotReportStringGaugeValues() throws Exception {
        reporter.report(map("gauge", gauge("value")),
                this.<Counter>map(),
                this.<Histogram>map(),
                this.<Meter>map(),
                this.<Timer>map());

        final InOrder inOrder = inOrder(atsd);
        inOrder.verify(atsd).isConnected();
        inOrder.verify(atsd).connect();
        inOrder.verify(atsd, never()).send(entity, new MetricName("prefix.gauge"), "100", timestamp);
        inOrder.verify(atsd).flush();

        verifyNoMoreInteractions(atsd);
    }

    @Test
    public void reportsByteGaugeValues() throws Exception {
        MetricName metric = new MetricName("prefix.gauge");
        reporter.report(map("gauge", gauge((byte) 1)),
                this.<Counter>map(),
                this.<Histogram>map(),
                this.<Meter>map(),
                this.<Timer>map());

        final InOrder inOrder = inOrder(atsd);
        inOrder.verify(atsd).isConnected();
        inOrder.verify(atsd).connect();
        inOrder.verify(atsd).send(entity, new MetricName("prefix.gauge"), "1", timestamp);
        inOrder.verify(atsd).flush();

        verifyNoMoreInteractions(atsd);
    }

    @Test
    public void reportsShortGaugeValues() throws Exception {
        MetricName metric = new MetricName("prefix.gauge");
        reporter.report(map("gauge", gauge((short) 1)),
                this.<Counter>map(),
                this.<Histogram>map(),
                this.<Meter>map(),
                this.<Timer>map());

        final InOrder inOrder = inOrder(atsd);
        inOrder.verify(atsd).isConnected();
        inOrder.verify(atsd).connect();
        inOrder.verify(atsd).send(entity, new MetricName("prefix.gauge"), "1", timestamp);
        inOrder.verify(atsd).flush();

        verifyNoMoreInteractions(atsd);
    }

    @Test
    public void reportsIntegerGaugeValues() throws Exception {
        MetricName metric = new MetricName("prefix.gauge");
        reporter.report(map("gauge", gauge(1)),
                this.<Counter>map(),
                this.<Histogram>map(),
                this.<Meter>map(),
                this.<Timer>map());

        final InOrder inOrder = inOrder(atsd);
        inOrder.verify(atsd).isConnected();
        inOrder.verify(atsd).connect();
        inOrder.verify(atsd).send(entity, new MetricName("prefix.gauge"), "1", timestamp);
        inOrder.verify(atsd).flush();

        verifyNoMoreInteractions(atsd);
    }

    @Test
    public void reportsLongGaugeValues() throws Exception {
        reporter.report(map("gauge", gauge(1L)),
                this.<Counter>map(),
                this.<Histogram>map(),
                this.<Meter>map(),
                this.<Timer>map());

        final InOrder inOrder = inOrder(atsd);
        inOrder.verify(atsd).isConnected();
        inOrder.verify(atsd).connect();
        inOrder.verify(atsd).send(entity, new MetricName("prefix.gauge"), "1", timestamp);
        inOrder.verify(atsd).flush();

        verifyNoMoreInteractions(atsd);
    }

    @Test
    public void reportsFloatGaugeValues() throws Exception {
        reporter.report(map("gauge", gauge(1.1f)),
                this.<Counter>map(),
                this.<Histogram>map(),
                this.<Meter>map(),
                this.<Timer>map());

        final InOrder inOrder = inOrder(atsd);
        inOrder.verify(atsd).isConnected();
        inOrder.verify(atsd).connect();
        inOrder.verify(atsd).send(entity, new MetricName("prefix.gauge"), "1.1", timestamp);
        inOrder.verify(atsd).flush();

        verifyNoMoreInteractions(atsd);
    }

    @Test
    public void reportsDoubleGaugeValues() throws Exception {
        reporter.report(map("gauge", gauge(1.1)),
                this.<Counter>map(),
                this.<Histogram>map(),
                this.<Meter>map(),
                this.<Timer>map());

        final InOrder inOrder = inOrder(atsd);
        inOrder.verify(atsd).isConnected();
        inOrder.verify(atsd).connect();
        inOrder.verify(atsd).send(entity, new MetricName("prefix.gauge"), "1.1", timestamp);
        inOrder.verify(atsd).flush();

        verifyNoMoreInteractions(atsd);
    }

    @Test
    public void reportsCounters() throws Exception {
        final Counter counter = mock(Counter.class);
        when(counter.getCount()).thenReturn(100L);

        reporter.report(this.<Gauge>map(),
                this.<Counter>map("counter", counter),
                this.<Histogram>map(),
                this.<Meter>map(),
                this.<Timer>map());

        final InOrder inOrder = inOrder(atsd);
        inOrder.verify(atsd).isConnected();
        inOrder.verify(atsd).connect();
        inOrder.verify(atsd).send(entity, new MetricName("prefix.counter.count"), "100", timestamp);
        inOrder.verify(atsd).flush();

        verifyNoMoreInteractions(atsd);
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

        reporter.report(this.<Gauge>map(),
                this.<Counter>map(),
                this.<Histogram>map("histogram", histogram),
                this.<Meter>map(),
                this.<Timer>map());

        final InOrder inOrder = inOrder(atsd);
        inOrder.verify(atsd).isConnected();
        inOrder.verify(atsd).connect();
        inOrder.verify(atsd).send(entity, new MetricName("prefix.histogram.count"), "1", timestamp);
        inOrder.verify(atsd).send(entity, new MetricName("prefix.histogram.max"), "2", timestamp);
        inOrder.verify(atsd).send(entity, new MetricName("prefix.histogram.mean"), "3.0", timestamp);
        inOrder.verify(atsd).send(entity, new MetricName("prefix.histogram.min"), "4", timestamp);
        inOrder.verify(atsd).send(entity, new MetricName("prefix.histogram.stddev"), "5.0", timestamp);
        inOrder.verify(atsd).send(entity, new MetricName("prefix.histogram.p50"), "6.0", timestamp);
        inOrder.verify(atsd).send(entity, new MetricName("prefix.histogram.p75"), "7.0", timestamp);
        inOrder.verify(atsd).send(entity, new MetricName("prefix.histogram.p95"), "8.0", timestamp);
        inOrder.verify(atsd).send(entity, new MetricName("prefix.histogram.p98"), "9.0", timestamp);
        inOrder.verify(atsd).send(entity, new MetricName("prefix.histogram.p99"), "10.0", timestamp);
        inOrder.verify(atsd).send(entity, new MetricName("prefix.histogram.p999"), "11.0", timestamp);
        inOrder.verify(atsd).flush();

        verifyNoMoreInteractions(atsd);
    }

    @Test
    public void reportsMeters() throws Exception {
        final Meter meter = mock(Meter.class);
        when(meter.getCount()).thenReturn(1L);
        when(meter.getOneMinuteRate()).thenReturn(2.0);
        when(meter.getFiveMinuteRate()).thenReturn(3.0);
        when(meter.getFifteenMinuteRate()).thenReturn(4.0);
        when(meter.getMeanRate()).thenReturn(5.0);

        reporter.report(this.<Gauge>map(),
                this.<Counter>map(),
                this.<Histogram>map(),
                this.<Meter>map("meter", meter),
                this.<Timer>map());

        final InOrder inOrder = inOrder(atsd);
        inOrder.verify(atsd).isConnected();
        inOrder.verify(atsd).connect();
        inOrder.verify(atsd).send(entity,new MetricName("prefix.meter.count"), "1", timestamp);
        inOrder.verify(atsd).send(entity,new MetricName("prefix.meter.m1_rate"), "2.0", timestamp);
        inOrder.verify(atsd).send(entity,new MetricName("prefix.meter.m5_rate"), "3.0", timestamp);
        inOrder.verify(atsd).send(entity,new MetricName("prefix.meter.m15_rate"), "4.0", timestamp);
        inOrder.verify(atsd).send(entity,new MetricName("prefix.meter.mean_rate"), "5.0", timestamp);
        inOrder.verify(atsd).flush();

        verifyNoMoreInteractions(atsd);
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

        reporter.report(this.<Gauge>map(),
                this.<Counter>map(),
                this.<Histogram>map(),
                this.<Meter>map(),
                map("timer", timer));

        final InOrder inOrder = inOrder(atsd);
        inOrder.verify(atsd).isConnected();
        inOrder.verify(atsd).connect();
        inOrder.verify(atsd).send(entity, new MetricName("prefix.timer.max"), "100.0", timestamp);
        inOrder.verify(atsd).send(entity, new MetricName("prefix.timer.mean"), "200.0", timestamp);
        inOrder.verify(atsd).send(entity, new MetricName("prefix.timer.min"), "300.0", timestamp);
        inOrder.verify(atsd).send(entity, new MetricName("prefix.timer.stddev"), "400.0", timestamp);
        inOrder.verify(atsd).send(entity, new MetricName("prefix.timer.p50"), "500.0", timestamp);
        inOrder.verify(atsd).send(entity, new MetricName("prefix.timer.p75"), "600.0", timestamp);
        inOrder.verify(atsd).send(entity, new MetricName("prefix.timer.p95"), "700.0", timestamp);
        inOrder.verify(atsd).send(entity, new MetricName("prefix.timer.p98"), "800.0", timestamp);
        inOrder.verify(atsd).send(entity, new MetricName("prefix.timer.p99"), "900.0", timestamp);
        inOrder.verify(atsd).send(entity, new MetricName("prefix.timer.p999"), "1000.0", timestamp);
        inOrder.verify(atsd).send(entity, new MetricName("prefix.timer.count"), "1", timestamp);
        inOrder.verify(atsd).send(entity, new MetricName("prefix.timer.m1_rate"), "3.0", timestamp);
        inOrder.verify(atsd).send(entity, new MetricName("prefix.timer.m5_rate"), "4.0", timestamp);
        inOrder.verify(atsd).send(entity, new MetricName("prefix.timer.m15_rate"), "5.0", timestamp);
        inOrder.verify(atsd).send(entity, new MetricName("prefix.timer.mean_rate"), "2.0", timestamp);
        inOrder.verify(atsd).flush();

        verifyNoMoreInteractions(atsd);
    }

    @Test
    public void closesConnectionIfATSDIsUnavailable() throws Exception {
        doThrow(new UnknownHostException("UNKNOWN-HOST")).when(atsd).connect();
        reporter.report(map("gauge", gauge(1)),
                this.<Counter>map(),
                this.<Histogram>map(),
                this.<Meter>map(),
                this.<Timer>map());

        final InOrder inOrder = inOrder(atsd);
        inOrder.verify(atsd).isConnected();
        inOrder.verify(atsd).connect();
        inOrder.verify(atsd).close();

        verifyNoMoreInteractions(atsd);
    }

    @Test
    public void closesConnectionIfAnUnexpectedExceptionOccurs() throws Exception {
        final Gauge gauge = mock(Gauge.class);
        when(gauge.getValue()).thenThrow(new RuntimeException("MockException"));
        try {
            reporter.report(map("gauge", gauge),
                    this.<Counter>map(),
                    this.<Histogram>map(),
                    this.<Meter>map(),
                    this.<Timer>map());

            final InOrder inOrder = inOrder(atsd);
            inOrder.verify(atsd).isConnected();
            inOrder.verify(atsd).connect();
            inOrder.verify(atsd).close();

            verifyNoMoreInteractions(atsd);

        } catch (RuntimeException e) {
            if(atsd.isConnected()) {
                assert(false);
            } else {
                assert(true);
            }
        }
    }

    @Test
    public void closesConnectionOnReporterStop() throws Exception {
        reporter.stop();

        verify(atsd).close();

        verifyNoMoreInteractions(atsd);
    }

    private <T> SortedMap<MetricName, T> map() {
        return new TreeMap<MetricName, T>();
    }

    private <T> SortedMap<MetricName, T> map(String name, T metric) {
        final TreeMap<MetricName, T> map = new TreeMap<MetricName, T>();
        map.put(MetricName.build(name), metric);
        return map;
    }

    private <T> Gauge gauge(T value) {
        final Gauge gauge = mock(Gauge.class);
        when(gauge.getValue()).thenReturn(value);
        return gauge;
    }
}
