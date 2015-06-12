package com.codehale.metrics.influxdb;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.codehale.metrics.influxdb.data.InfluxDbPoint;
import com.codehale.metrics.influxdb.data.InfluxDbWriteObject;

public class InfluxDbReporterTest {
    @Mock
    private InfluxDbSender influxDb;
    @Mock
    private InfluxDbWriteObject writeObject;
    @Mock
    private MetricRegistry registry;
    private InfluxDbReporter reporter;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        reporter = InfluxDbReporter
                .forRegistry(registry)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .filter(MetricFilter.ALL)
                .build(influxDb);

    }

    @Test
    public void reportsCounters() throws Exception {
        final Counter counter = mock(Counter.class);
        Mockito.when(counter.getCount()).thenReturn(100L);

        reporter.report(this.<Gauge>map(), this.map("counter", counter), this.<Histogram>map(), this.<Meter>map(), this.<Timer>map());
        final ArgumentCaptor<InfluxDbPoint> influxDbPointCaptor = ArgumentCaptor.forClass(InfluxDbPoint.class);
        Mockito.verify(influxDb, atLeastOnce()).appendPoints(influxDbPointCaptor.capture());
        InfluxDbPoint point = influxDbPointCaptor.getValue();
        assertThat(point.getMeasurement()).isEqualTo("counter");
        assertThat(point.getFields()).isNotEmpty();
        assertThat(point.getFields()).hasSize(1);
        assertThat(point.getFields()).contains(entry("count", 100L));
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

        reporter.report(this.<Gauge>map(), this.<Counter>map(), this.map("histogram", histogram), this.<Meter>map(), this.<Timer>map());

        final ArgumentCaptor<InfluxDbPoint> influxDbPointCaptor = ArgumentCaptor.forClass(InfluxDbPoint.class);
        Mockito.verify(influxDb, atLeastOnce()).appendPoints(influxDbPointCaptor.capture());
        InfluxDbPoint point = influxDbPointCaptor.getValue();

        assertThat(point.getMeasurement()).isEqualTo("histogram");
        assertThat(point.getFields()).isNotEmpty();
        assertThat(point.getFields()).hasSize(13);
        assertThat(point.getFields()).contains(entry("max", 2L));
        assertThat(point.getFields()).contains(entry("mean", 3.0));
        assertThat(point.getFields()).contains(entry("min", 4L));
        assertThat(point.getFields()).contains(entry("std-dev", 5.0));
        assertThat(point.getFields()).contains(entry("median", 6.0));
        assertThat(point.getFields()).contains(entry("75-percentile", 7.0));
        assertThat(point.getFields()).contains(entry("95-percentile", 8.0));
        assertThat(point.getFields()).contains(entry("98-percentile", 9.0));
        assertThat(point.getFields()).contains(entry("99-percentile", 10.0));
        assertThat(point.getFields()).contains(entry("999-percentile", 11.0));
    }

    @Test
    public void reportsMeters() throws Exception {
        final Meter meter = mock(Meter.class);
        when(meter.getCount()).thenReturn(1L);
        when(meter.getOneMinuteRate()).thenReturn(2.0);
        when(meter.getFiveMinuteRate()).thenReturn(3.0);
        when(meter.getFifteenMinuteRate()).thenReturn(4.0);
        when(meter.getMeanRate()).thenReturn(5.0);

        reporter.report(this.<Gauge>map(), this.<Counter>map(), this.<Histogram>map(), this.map("meter", meter), this.<Timer>map());

        final ArgumentCaptor<InfluxDbPoint> influxDbPointCaptor = ArgumentCaptor.forClass(InfluxDbPoint.class);
        Mockito.verify(influxDb, atLeastOnce()).appendPoints(influxDbPointCaptor.capture());
        InfluxDbPoint point = influxDbPointCaptor.getValue();

        assertThat(point.getMeasurement()).isEqualTo("meter");
        assertThat(point.getFields()).isNotEmpty();
        assertThat(point.getFields()).hasSize(5);
        assertThat(point.getFields()).contains(entry("count", 1L));
        assertThat(point.getFields()).contains(entry("one-minute", 2.0));
        assertThat(point.getFields()).contains(entry("five-minute", 3.0));
        assertThat(point.getFields()).contains(entry("fifteen-minute", 4.0));
        assertThat(point.getFields()).contains(entry("mean-rate", 5.0));
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
        when(snapshot.getMin()).thenReturn(TimeUnit.MILLISECONDS.toNanos(100));
        when(snapshot.getMean()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(200));
        when(snapshot.getMax()).thenReturn(TimeUnit.MILLISECONDS.toNanos(300));
        when(snapshot.getStdDev()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(400));
        when(snapshot.getMedian()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(500));
        when(snapshot.get75thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(600));
        when(snapshot.get95thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(700));
        when(snapshot.get98thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(800));
        when(snapshot.get99thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(900));
        when(snapshot.get999thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(1000));

        when(timer.getSnapshot()).thenReturn(snapshot);

        reporter.report(this.<Gauge>map(), this.<Counter>map(), this.<Histogram>map(), this.<Meter>map(), map("timer", timer));

        final ArgumentCaptor<InfluxDbPoint> influxDbPointCaptor = ArgumentCaptor.forClass(InfluxDbPoint.class);
        Mockito.verify(influxDb, atLeastOnce()).appendPoints(influxDbPointCaptor.capture());
        InfluxDbPoint point = influxDbPointCaptor.getValue();

        assertThat(point.getMeasurement()).isEqualTo("timer");
        assertThat(point.getFields()).isNotEmpty();
        assertThat(point.getFields()).hasSize(17);
        assertThat(point.getFields()).contains(entry("count", 1L));
        assertThat(point.getFields()).contains(entry("mean-rate", 2.0));
        assertThat(point.getFields()).contains(entry("one-minute", 3.0));
        assertThat(point.getFields()).contains(entry("five-minute", 4.0));
        assertThat(point.getFields()).contains(entry("fifteen-minute", 5.0));
        assertThat(point.getFields()).contains(entry("min", 100.0));
        assertThat(point.getFields()).contains(entry("mean", 200.0));
        assertThat(point.getFields()).contains(entry("max", 300.0));
        assertThat(point.getFields()).contains(entry("std-dev", 400.0));
        assertThat(point.getFields()).contains(entry("median", 500.0));
        assertThat(point.getFields()).contains(entry("75-percentile", 600.0));
        assertThat(point.getFields()).contains(entry("95-percentile", 700.0));
        assertThat(point.getFields()).contains(entry("98-percentile", 800.0));
        assertThat(point.getFields()).contains(entry("99-percentile", 900.0));
        assertThat(point.getFields()).contains(entry("999-percentile", 1000.0));
    }

    @Test
    public void reportsIntegerGaugeValues() throws Exception {
        reporter.report(map("gauge", gauge(1)), this.<Counter>map(), this.<Histogram>map(), this.<Meter>map(), this.<Timer>map());

        final ArgumentCaptor<InfluxDbPoint> influxDbPointCaptor = ArgumentCaptor.forClass(InfluxDbPoint.class);
        Mockito.verify(influxDb, atLeastOnce()).appendPoints(influxDbPointCaptor.capture());
        InfluxDbPoint point = influxDbPointCaptor.getValue();

        assertThat(point.getMeasurement()).isEqualTo("gauge");
        assertThat(point.getFields()).isNotEmpty();
        assertThat(point.getFields()).hasSize(1);
        assertThat(point.getFields()).contains(entry("value", 1));
    }

    @Test
    public void reportsLongGaugeValues() throws Exception {
        reporter.report(map("gauge", gauge(1L)), this.<Counter>map(), this.<Histogram>map(), this.<Meter>map(), this.<Timer>map());

        final ArgumentCaptor<InfluxDbPoint> influxDbPointCaptor = ArgumentCaptor.forClass(InfluxDbPoint.class);
        Mockito.verify(influxDb, atLeastOnce()).appendPoints(influxDbPointCaptor.capture());
        InfluxDbPoint point = influxDbPointCaptor.getValue();

        assertThat(point.getMeasurement()).isEqualTo("gauge");
        assertThat(point.getFields()).isNotEmpty();
        assertThat(point.getFields()).hasSize(1);
        assertThat(point.getFields()).contains(entry("value", 1L));
    }

    @Test
    public void reportsFloatGaugeValues() throws Exception {
        reporter.report(map("gauge", gauge(1.1f)), this.<Counter>map(), this.<Histogram>map(), this.<Meter>map(), this.<Timer>map());

        final ArgumentCaptor<InfluxDbPoint> influxDbPointCaptor = ArgumentCaptor.forClass(InfluxDbPoint.class);
        Mockito.verify(influxDb, atLeastOnce()).appendPoints(influxDbPointCaptor.capture());
        InfluxDbPoint point = influxDbPointCaptor.getValue();

        assertThat(point.getMeasurement()).isEqualTo("gauge");
        assertThat(point.getFields()).isNotEmpty();
        assertThat(point.getFields()).hasSize(1);
        assertThat(point.getFields()).contains(entry("value", 1.1f));
    }

    @Test
    public void reportsDoubleGaugeValues() throws Exception {
        reporter.report(map("gauge", gauge(1.1)), this.<Counter>map(), this.<Histogram>map(), this.<Meter>map(), this.<Timer>map());

        final ArgumentCaptor<InfluxDbPoint> influxDbPointCaptor = ArgumentCaptor.forClass(InfluxDbPoint.class);
        Mockito.verify(influxDb, atLeastOnce()).appendPoints(influxDbPointCaptor.capture());
        InfluxDbPoint point = influxDbPointCaptor.getValue();

        assertThat(point.getMeasurement()).isEqualTo("gauge");
        assertThat(point.getFields()).isNotEmpty();
        assertThat(point.getFields()).hasSize(1);
        assertThat(point.getFields()).contains(entry("value", 1.1));
    }

    @Test
    public void reportsByteGaugeValues() throws Exception {
        reporter
                .report(map("gauge", gauge((byte) 1)), this.<Counter>map(), this.<Histogram>map(), this.<Meter>map(), this.<Timer>map());

        final ArgumentCaptor<InfluxDbPoint> influxDbPointCaptor = ArgumentCaptor.forClass(InfluxDbPoint.class);
        Mockito.verify(influxDb, atLeastOnce()).appendPoints(influxDbPointCaptor.capture());
        InfluxDbPoint point = influxDbPointCaptor.getValue();

        assertThat(point.getMeasurement()).isEqualTo("gauge");
        assertThat(point.getFields()).isNotEmpty();
        assertThat(point.getFields()).hasSize(1);
        assertThat(point.getFields()).contains(entry("value", (byte) 1));
    }

    private <T> SortedMap<String, T> map() {
        return new TreeMap<>();
    }

    private <T> SortedMap<String, T> map(String name, T metric) {
        final TreeMap<String, T> map = new TreeMap<>();
        map.put(name, metric);
        return map;
    }

    private <T> Gauge gauge(T value) {
        final Gauge gauge = mock(Gauge.class);
        when(gauge.getValue()).thenReturn(value);
        return gauge;
    }
}
