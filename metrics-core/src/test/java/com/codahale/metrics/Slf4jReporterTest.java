package com.codahale.metrics;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

public class Slf4jReporterTest {
    private final Logger logger = mock(Logger.class);
    private final Marker marker = mock(Marker.class);
    private final MetricRegistry registry = mock(MetricRegistry.class);
    private final Slf4jReporter infoReporter = Slf4jReporter.forRegistry(registry)
            .outputTo(logger)
            .markWith(marker)
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .withLoggingLevel(Slf4jReporter.LoggingLevel.INFO)
            .filter(MetricFilter.ALL)
            .withQuantile("p233", 0.233)
            .withQuantile("p9988", 0.9988)
            .build();

    private final Slf4jReporter errorReporter = Slf4jReporter.forRegistry(registry)
            .outputTo(logger)
            .markWith(marker)
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .withLoggingLevel(Slf4jReporter.LoggingLevel.ERROR)
            .filter(MetricFilter.ALL)
            .withQuantile("p233", 0.233)
            .withQuantile("p9988", 0.9988)
            .build();

    @Test
    public void reportsGaugeValuesAtError() throws Exception {
        errorReporter.report(map("gauge", gauge("value")),
                this.<Counter>map(),
                this.<Histogram>map(),
                this.<Meter>map(),
                this.<Timer>map());

        verify(logger).error(marker, "type=GAUGE, name={}, value={}", new Object[]{"gauge", "value"});
    }

    @Test
    public void reportsCounterValuesAtError() throws Exception {
        final Counter counter = mock(Counter.class);
        when(counter.getCount()).thenReturn(100L);

        errorReporter.report(this.<Gauge>map(),
                map("test.counter", counter),
                this.<Histogram>map(),
                this.<Meter>map(),
                this.<Timer>map());

        verify(logger).error(marker, "type=COUNTER, name={}, count={}", new Object[]{"test.counter", 100L});
    }

    @Test
    public void reportsHistogramValuesAtError() throws Exception {
        final Histogram histogram = mock(Histogram.class);
        when(histogram.getCount()).thenReturn(1L);

        final Snapshot snapshot = mock(Snapshot.class);
        when(snapshot.getMax()).thenReturn(2L);
        when(snapshot.getMean()).thenReturn(3.0);
        when(snapshot.getMin()).thenReturn(4L);
        when(snapshot.getStdDev()).thenReturn(5.0);
        when(snapshot.getValue(0.50)).thenReturn(6.0);
        when(snapshot.getValue(0.75)).thenReturn(7.0);
        when(snapshot.getValue(0.95)).thenReturn(8.0);
        when(snapshot.getValue(0.98)).thenReturn(9.0);
        when(snapshot.getValue(0.99)).thenReturn(10.0);
        when(snapshot.getValue(0.999)).thenReturn(11.0);
        when(snapshot.getValue(0.233)).thenReturn(3.0);
        when(snapshot.getValue(0.9988)).thenReturn(17.0);

        when(histogram.getSnapshot()).thenReturn(snapshot);

        errorReporter.report(this.<Gauge>map(),
                this.<Counter>map(),
                map("test.histogram", histogram),
                this.<Meter>map(),
                this.<Timer>map());

        verify(logger).error(marker,
                "type=HISTOGRAM, name={}, count={}, min={}, max={}, mean={}, stddev={}, p50={}, p75={}, p95={}, p98={}, p99={}, p999={}, p233={}, p9988={}",
                "test.histogram",
                1L,
                4L,
                2L,
                3.0,
                5.0,
                6.0,
                7.0,
                8.0,
                9.0,
                10.0,
                11.0,
                3.0,
                17.0);
    }

    @Test
    public void reportsMeterValuesAtError() throws Exception {
        final Meter meter = mock(Meter.class);
        when(meter.getCount()).thenReturn(1L);
        when(meter.getMeanRate()).thenReturn(2.0);
        when(meter.getOneMinuteRate()).thenReturn(3.0);
        when(meter.getFiveMinuteRate()).thenReturn(4.0);
        when(meter.getFifteenMinuteRate()).thenReturn(5.0);

        errorReporter.report(this.<Gauge>map(),
                this.<Counter>map(),
                this.<Histogram>map(),
                map("test.meter", meter),
                this.<Timer>map());

        verify(logger).error(marker,
                "type=METER, name={}, count={}, mean_rate={}, m1={}, m5={}, m15={}, rate_unit={}",
                "test.meter",
                1L,
                2.0,
                3.0,
                4.0,
                5.0,
                "events/second");
    }

    @Test
    public void reportsTimerValuesAtError() throws Exception {
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
        when(snapshot.getValue(0.50)).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(500));
        when(snapshot.getValue(0.75)).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(600));
        when(snapshot.getValue(0.95)).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(700));
        when(snapshot.getValue(0.98)).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(800));
        when(snapshot.getValue(0.99)).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(900));
        when(snapshot.getValue(0.999)).thenReturn((double) TimeUnit.MILLISECONDS
                .toNanos(1000));
        when(snapshot.getValue(0.233)).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(100));
        when(snapshot.getValue(0.9988)).thenReturn((double) TimeUnit.MILLISECONDS
                .toNanos(2000));

        when(timer.getSnapshot()).thenReturn(snapshot);

        errorReporter.report(this.<Gauge>map(),
                this.<Counter>map(),
                this.<Histogram>map(),
                this.<Meter>map(),
                map("test.another.timer", timer));

        verify(logger).error(marker,
                "type=TIMER, name={}, count={}, min={}, max={}, mean={}, stddev={}, p50={}, p75={}, p95={}, p98={}, p99={}, p999={}, p233={}, p9988={}, mean_rate={}, m1={}, m5={}, m15={}, rate_unit={}, duration_unit={}",
                "test.another.timer",
                1L,
                300.0,
                100.0,
                200.0,
                400.0,
                500.0,
                600.0,
                700.0,
                800.0,
                900.0,
                1000.0,
                100.0,
                2000.0,
                2.0,
                3.0,
                4.0,
                5.0,
                "events/second",
                "milliseconds");
    }

    @Test
    public void reportsGaugeValues() throws Exception {
        infoReporter.report(map("gauge", gauge("value")),
                this.<Counter>map(),
                this.<Histogram>map(),
                this.<Meter>map(),
                this.<Timer>map());

        verify(logger).info(marker, "type=GAUGE, name={}, value={}", new Object[]{"gauge", "value"});
    }

    @Test
    public void reportsCounterValues() throws Exception {
        final Counter counter = mock(Counter.class);
        when(counter.getCount()).thenReturn(100L);

        infoReporter.report(this.<Gauge>map(),
                map("test.counter", counter),
                this.<Histogram>map(),
                this.<Meter>map(),
                this.<Timer>map());

        verify(logger).info(marker, "type=COUNTER, name={}, count={}", new Object[]{"test.counter", 100L});
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
        when(snapshot.getValue(0.50)).thenReturn(6.0);
        when(snapshot.getValue(0.75)).thenReturn(7.0);
        when(snapshot.getValue(0.95)).thenReturn(8.0);
        when(snapshot.getValue(0.98)).thenReturn(9.0);
        when(snapshot.getValue(0.99)).thenReturn(10.0);
        when(snapshot.getValue(0.999)).thenReturn(11.0);
        when(snapshot.getValue(0.233)).thenReturn(3.0);
        when(snapshot.getValue(0.9988)).thenReturn(17.0);

        when(histogram.getSnapshot()).thenReturn(snapshot);

        infoReporter.report(this.<Gauge>map(),
                this.<Counter>map(),
                map("test.histogram", histogram),
                this.<Meter>map(),
                this.<Timer>map());

        verify(logger).info(marker,
                "type=HISTOGRAM, name={}, count={}, min={}, max={}, mean={}, stddev={}, p50={}, p75={}, p95={}, p98={}, p99={}, p999={}, p233={}, p9988={}",
                "test.histogram",
                1L,
                4L,
                2L,
                3.0,
                5.0,
                6.0,
                7.0,
                8.0,
                9.0,
                10.0,
                11.0,
                3.0,
                17.0);
    }

    @Test
    public void reportsHistogramValuesWithOnlyOneQuantile() throws Exception {
        final Histogram histogram = mock(Histogram.class);
        when(histogram.getCount()).thenReturn(1L);

        final Snapshot snapshot = mock(Snapshot.class);
        when(snapshot.getMax()).thenReturn(2L);
        when(snapshot.getMean()).thenReturn(3.0);
        when(snapshot.getMin()).thenReturn(4L);
        when(snapshot.getStdDev()).thenReturn(5.0);
        when(snapshot.getValue(0.233)).thenReturn(3.0);

        when(histogram.getSnapshot()).thenReturn(snapshot);

        Slf4jReporter infoReporter = Slf4jReporter.forRegistry(registry)
                .outputTo(logger)
                .markWith(marker)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .withLoggingLevel(Slf4jReporter.LoggingLevel.INFO)
                .filter(MetricFilter.ALL)
                .withNoQuantiles()
                .withQuantile("p233", 0.233)
                .build();

        infoReporter.report(this.<Gauge>map(),
                this.<Counter>map(),
                map("test.histogram", histogram),
                this.<Meter>map(),
                this.<Timer>map());

        verify(logger).info(marker,
                "type=HISTOGRAM, name={}, count={}, min={}, max={}, mean={}, stddev={}, p233={}",
                "test.histogram",
                1L,
                4L,
                2L,
                3.0,
                5.0,
                3.0);
    }

    @Test
    public void reportsMeterValues() throws Exception {
        final Meter meter = mock(Meter.class);
        when(meter.getCount()).thenReturn(1L);
        when(meter.getMeanRate()).thenReturn(2.0);
        when(meter.getOneMinuteRate()).thenReturn(3.0);
        when(meter.getFiveMinuteRate()).thenReturn(4.0);
        when(meter.getFifteenMinuteRate()).thenReturn(5.0);

        infoReporter.report(this.<Gauge>map(),
                this.<Counter>map(),
                this.<Histogram>map(),
                map("test.meter", meter),
                this.<Timer>map());

        verify(logger).info(marker,
                "type=METER, name={}, count={}, mean_rate={}, m1={}, m5={}, m15={}, rate_unit={}",
                "test.meter",
                1L,
                2.0,
                3.0,
                4.0,
                5.0,
                "events/second");
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
        when(snapshot.getValue(0.50)).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(500));
        when(snapshot.getValue(0.75)).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(600));
        when(snapshot.getValue(0.95)).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(700));
        when(snapshot.getValue(0.98)).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(800));
        when(snapshot.getValue(0.99)).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(900));
        when(snapshot.getValue(0.999)).thenReturn((double) TimeUnit.MILLISECONDS
                .toNanos(1000));
        when(snapshot.getValue(0.233)).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(100));
        when(snapshot.getValue(0.9988)).thenReturn((double) TimeUnit.MILLISECONDS
                .toNanos(2000));
        when(timer.getSnapshot()).thenReturn(snapshot);

        infoReporter.report(this.<Gauge>map(),
                this.<Counter>map(),
                this.<Histogram>map(),
                this.<Meter>map(),
                map("test.another.timer", timer));

        verify(logger).info(marker,
                "type=TIMER, name={}, count={}, min={}, max={}, mean={}, stddev={}, p50={}, p75={}, p95={}, p98={}, p99={}, p999={}, p233={}, p9988={}, mean_rate={}, m1={}, m5={}, m15={}, rate_unit={}, duration_unit={}",
                "test.another.timer",
                1L,
                300.0,
                100.0,
                200.0,
                400.0,
                500.0,
                600.0,
                700.0,
                800.0,
                900.0,
                1000.0,
                100.0,
                2000.0,
                2.0,
                3.0,
                4.0,
                5.0,
                "events/second",
                "milliseconds");
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
