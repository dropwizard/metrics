package io.dropwizard.metrics5.influxdb;

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

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;

public class InfluxDbReporterTest {
    private static final MetricName GAUGE = MetricName.build("gauge");
    private static final MetricName METER = MetricName.build("meter");
    private static final MetricName COUNTER = MetricName.build("counter");

    private final long timestamp = 1000198;
    private final Clock clock = mock(Clock.class);
    private final InfluxDbSender sender = mock(InfluxDbSender.class);
    private final List<String> send = new ArrayList<>();
    private final MetricRegistry registry = mock(MetricRegistry.class);
    private final InfluxDbReporter reporter = InfluxDbReporter.forRegistry(registry)
            .withClock(clock)
            .prefixedWith(new MetricName("prefix", map("foo", "bar")))
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .filter(MetricFilter.ALL)
            .disabledMetricAttributes(Collections.emptySet())
            .build(sender);

    private final InfluxDbReporter minuteRateReporter = InfluxDbReporter
            .forRegistry(registry)
            .withClock(clock)
            .prefixedWith(new MetricName("prefix", map("foo", "bar")))
            .convertRatesTo(TimeUnit.MINUTES)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .filter(MetricFilter.ALL)
            .disabledMetricAttributes(Collections.emptySet())
            .build(sender);

    @Before
    public void setUp() throws IOException {
        when(clock.getTime()).thenReturn(timestamp * 1000);
        send.clear();
        doAnswer(invocation -> send.add(invocation.getArgument(0).toString()))
                .when(sender).send(any(StringBuilder.class));
    }

    @Test
    public void reportsStringGaugeValues() throws Exception {
        reporter.report(map(GAUGE, gauge("value")),
                map(),
                map(),
                map(),
                map());

        final InOrder inOrder = inOrder(sender);
        inOrder.verify(sender).connect();
        inOrder.verify(sender).send(anySb());
        inOrder.verify(sender).flush();
        inOrder.verify(sender).disconnect();

        verifyNoMoreInteractions(sender);
        assertThat(send).first().isEqualTo("prefix.gauge,foo=bar value=\"value\" 1000198000000000\n");
    }

    @Test
    public void reportsByteGaugeValues() throws Exception {
        reporter.report(map(GAUGE, gauge((byte) 1)),
                map(),
                map(),
                map(),
                map());

        final InOrder inOrder = inOrder(sender);
        inOrder.verify(sender).connect();
        inOrder.verify(sender).send(anySb());
        inOrder.verify(sender).flush();
        inOrder.verify(sender).disconnect();

        verifyNoMoreInteractions(sender);
        assertThat(send).first().isEqualTo("prefix.gauge,foo=bar value=1i 1000198000000000\n");
    }

    @Test
    public void reportsShortGaugeValues() throws Exception {
        reporter.report(map(GAUGE, gauge((short) 1)),
                map(),
                map(),
                map(),
                map());

        final InOrder inOrder = inOrder(sender);
        inOrder.verify(sender).connect();
        inOrder.verify(sender).send(anySb());
        inOrder.verify(sender).flush();
        inOrder.verify(sender).disconnect();

        verifyNoMoreInteractions(sender);
        assertThat(send).first().isEqualTo("prefix.gauge,foo=bar value=1i 1000198000000000\n");
    }

    @Test
    public void reportsIntegerGaugeValues() throws Exception {
        reporter.report(map(GAUGE, gauge(1)),
                map(),
                map(),
                map(),
                map());

        final InOrder inOrder = inOrder(sender);
        inOrder.verify(sender).connect();
        inOrder.verify(sender).send(anySb());
        inOrder.verify(sender).flush();
        inOrder.verify(sender).disconnect();

        verifyNoMoreInteractions(sender);
        assertThat(send).first().isEqualTo("prefix.gauge,foo=bar value=1i 1000198000000000\n");
    }

    @Test
    public void reportsLongGaugeValues() throws Exception {
        reporter.report(map(GAUGE, gauge(1L)),
                map(),
                map(),
                map(),
                map());

        final InOrder inOrder = inOrder(sender);
        inOrder.verify(sender).connect();
        inOrder.verify(sender).send(anySb());
        inOrder.verify(sender).flush();
        inOrder.verify(sender).disconnect();

        verifyNoMoreInteractions(sender);
        assertThat(send).first().isEqualTo("prefix.gauge,foo=bar value=1i 1000198000000000\n");
    }

    @Test
    public void reportsFloatGaugeValues() throws Exception {
        reporter.report(map(GAUGE, gauge(1.5f)),
                map(),
                map(),
                map(),
                map());

        final InOrder inOrder = inOrder(sender);
        inOrder.verify(sender).connect();
        inOrder.verify(sender).send(anySb());
        inOrder.verify(sender).flush();
        inOrder.verify(sender).disconnect();

        verifyNoMoreInteractions(sender);
        assertThat(send).first().isEqualTo("prefix.gauge,foo=bar value=1.5 1000198000000000\n");
    }

    @Test
    public void reportsDoubleGaugeValues() throws Exception {
        reporter.report(map(GAUGE, gauge(1.1)),
                map(),
                map(),
                map(),
                map());

        final InOrder inOrder = inOrder(sender);
        inOrder.verify(sender).connect();
        inOrder.verify(sender).send(anySb());
        inOrder.verify(sender).flush();
        inOrder.verify(sender).disconnect();

        verifyNoMoreInteractions(sender);
        assertThat(send).first().isEqualTo("prefix.gauge,foo=bar value=1.1 1000198000000000\n");
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
        final InOrder inOrder = inOrder(sender);
        inOrder.verify(sender).connect();
        inOrder.verify(sender).send(anySb());
        inOrder.verify(sender).flush();
        inOrder.verify(sender).disconnect();

        inOrder.verify(sender).connect();
        inOrder.verify(sender).send(anySb());
        inOrder.verify(sender).flush();
        inOrder.verify(sender).disconnect();

        verifyNoMoreInteractions(sender);

        assertThat(send).element(0).isEqualTo("prefix.gauge,foo=bar value=t 1000198000000000\n");
        assertThat(send).element(1).isEqualTo("prefix.gauge,foo=bar value=f 1000198000000000\n");
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

        final InOrder inOrder = inOrder(sender);
        inOrder.verify(sender).connect();
        inOrder.verify(sender).send(anySb());
        inOrder.verify(sender).flush();
        inOrder.verify(sender).disconnect();

        verifyNoMoreInteractions(sender);
        assertThat(send).first().isEqualTo("prefix.counter,foo=bar count=100i 1000198000000000\n");
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

        final InOrder inOrder = inOrder(sender);
        inOrder.verify(sender).connect();
        inOrder.verify(sender).send(anySb());
        inOrder.verify(sender).flush();
        inOrder.verify(sender).disconnect();

        verifyNoMoreInteractions(sender);
        assertThat(send).first().isEqualTo("prefix.histogram,foo=bar count=1i,sum=12i,max=2i,mean=3.0,min=4i,stddev=5.0,p50=6.0,p75=7.0,p95=8.0,p98=9.0,p99=10.0,p999=11.0 1000198000000000\n");

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

        final InOrder inOrder = inOrder(sender);
        inOrder.verify(sender).connect();
        inOrder.verify(sender).send(anySb());
        inOrder.verify(sender).flush();
        inOrder.verify(sender).disconnect();

        verifyNoMoreInteractions(sender);
        assertThat(send).first().isEqualTo("prefix.meter,foo=bar count=1i,sum=6i,m1_rate=2.0,m5_rate=3.0,m15_rate=4.0,mean_rate=5.0 1000198000000000\n");
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

        final InOrder inOrder = inOrder(sender);
        inOrder.verify(sender).connect();
        inOrder.verify(sender).send(anySb());
        inOrder.verify(sender).flush();
        inOrder.verify(sender).disconnect();

        verifyNoMoreInteractions(sender);
        assertThat(send).first().isEqualTo("prefix.meter,foo=bar count=1i,sum=6i,m1_rate=120.0,m5_rate=180.0,m15_rate=240.0,mean_rate=300.0 1000198000000000\n");
    }

    @Test
    public void reportsTimers() throws Exception {
        final Timer timer = mock(Timer.class);
        when(timer.getCount()).thenReturn(1L);
        when(timer.getSum()).thenReturn(6L);
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

        final InOrder inOrder = inOrder(sender);
        inOrder.verify(sender).connect();
        inOrder.verify(sender).send(anySb());
        inOrder.verify(sender).flush();
        inOrder.verify(sender).disconnect();

        verifyNoMoreInteractions(sender);
        assertThat(send).first().isEqualTo("prefix.timer,foo=bar max=100.0,mean=200.0,min=300.0,stddev=400.0,p50=500.0,p75=600.0,p95=700.0,p98=800.0,p99=900.0,p999=1000.0,count=1i,sum=6i,m1_rate=3.0,m5_rate=4.0,m15_rate=5.0,mean_rate=2.0 1000198000000000\n");

        reporter.close();
    }

    @Test
    public void disconnectsIfSenderIsUnavailable() throws Exception {
        doThrow(new UnknownHostException("UNKNOWN-HOST")).when(sender).connect();
        reporter.report(map(GAUGE, gauge(1)),
                map(),
                map(),
                map(),
                map());

        final InOrder inOrder = inOrder(sender);
        inOrder.verify(sender).connect();
        inOrder.verify(sender).disconnect();


        verifyNoMoreInteractions(sender);
    }

    @Test
    public void closesConnectionOnReporterStop() throws Exception {
        reporter.stop();

        verify(sender).close();

        verifyNoMoreInteractions(sender);
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
        InfluxDbReporter reporterWithdisabledMetricAttributes = InfluxDbReporter.forRegistry(registry)
                .withClock(clock)
                .prefixedWith(MetricName.build("prefix"))
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .filter(MetricFilter.ALL)
                .disabledMetricAttributes(disabledMetricAttributes)
                .build(sender);
        reporterWithdisabledMetricAttributes.report(map(),
                map(COUNTER, counter),
                map(),
                map(METER, meter),
                map());

        final InOrder inOrder = inOrder(sender);
        inOrder.verify(sender).connect();
        inOrder.verify(sender, times(2)).send(anySb());
        inOrder.verify(sender).flush();
        inOrder.verify(sender).disconnect();

        verifyNoMoreInteractions(sender);

        assertThat(send).element(0).isEqualTo("prefix.counter count=11i 1000198000000000\n");
        assertThat(send).element(1).isEqualTo("prefix.meter count=1i,sum=6i,m1_rate=2.0,mean_rate=5.0 1000198000000000\n");
    }

    private <T> SortedMap<MetricName, T> map() {
        return new TreeMap<>();
    }

    private <K, V> SortedMap<K, V> map(K key, V value) {
        final TreeMap<K, V> map = new TreeMap<>();
        map.put(key, value);
        return map;
    }

    private <T> Gauge<T> gauge(T value) {
        return () -> value;
    }

    private StringBuilder anySb() {
        return any(StringBuilder.class);
    }
}
