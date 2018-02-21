package com.codahale.metrics;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class Slf4jReporterTest {
    private final Logger logger = mock(Logger.class);
    private final Marker marker = mock(Marker.class);
    private final MetricRegistry registry = mock(MetricRegistry.class);
    private final Slf4jReporter infoReporter = Slf4jReporter.forRegistry(registry)
            .outputTo(logger)
            .markWith(marker)
            .prefixedWith("prefix")
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .withLoggingLevel(Slf4jReporter.LoggingLevel.INFO)
            .filter(MetricFilter.ALL)
            .build();

    private final Slf4jReporter errorReporter = Slf4jReporter.forRegistry(registry)
            .outputTo(logger)
            .markWith(marker)
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .withLoggingLevel(Slf4jReporter.LoggingLevel.ERROR)
            .filter(MetricFilter.ALL)
            .build();

    @Test
    public void reportsGaugeValuesAtError() {
        when(logger.isErrorEnabled(marker)).thenReturn(true);
        errorReporter.report(map("gauge", () -> "value"),
                map(),
                map(),
                map(),
                map());

        verify(logger).error(marker, "type=GAUGE, name=gauge, value=value");
    }

    @Test
    public void reportsCounterValuesAtError() {
        final Counter counter = mock(Counter.class);
        when(counter.getCount()).thenReturn(100L);
        when(logger.isErrorEnabled(marker)).thenReturn(true);

        errorReporter.report(map(),
                map("test.counter", counter),
                map(),
                map(),
                map());

        verify(logger).error(marker, "type=COUNTER, name=test.counter, count=100");
    }

    @Test
    public void reportsHistogramValuesAtError() {
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
        when(logger.isErrorEnabled(marker)).thenReturn(true);

        errorReporter.report(map(),
                map(),
                map("test.histogram", histogram),
                map(),
                map());

        verify(logger).error(marker, "type=HISTOGRAM, name=test.histogram, count=1, min=4, max=2, mean=3.0, stddev=5.0, p50=6.0, p75=7.0, p95=8.0, p98=9.0, p99=10.0, p999=11.0");
    }

    @Test
    public void reportsMeterValuesAtError() {
        final Meter meter = mock(Meter.class);
        when(meter.getCount()).thenReturn(1L);
        when(meter.getMeanRate()).thenReturn(2.0);
        when(meter.getOneMinuteRate()).thenReturn(3.0);
        when(meter.getFiveMinuteRate()).thenReturn(4.0);
        when(meter.getFifteenMinuteRate()).thenReturn(5.0);
        when(logger.isErrorEnabled(marker)).thenReturn(true);

        errorReporter.report(map(),
                map(),
                map(),
                map("test.meter", meter),
                map());

        verify(logger).error(marker,  "type=METER, name=test.meter, count=1, m1_rate=3.0, m5_rate=4.0, m15_rate=5.0, mean_rate=2.0, rate_unit=events/second");
    }

    @Test
    public void reportsTimerValuesAtError() {
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

        when(logger.isErrorEnabled(marker)).thenReturn(true);

        errorReporter.report(map(),
                map(),
                map(),
                map(),
                map("test.another.timer", timer));

        verify(logger).error(marker, "type=TIMER, name=test.another.timer, count=1, min=300.0, max=100.0, mean=200.0, stddev=400.0, p50=500.0, p75=600.0, p95=700.0, p98=800.0, p99=900.0, p999=1000.0, m1_rate=3.0, m5_rate=4.0, m15_rate=5.0, mean_rate=2.0, rate_unit=events/second, duration_unit=milliseconds");
    }

    @Test
    public void reportsGaugeValues() {
        when(logger.isInfoEnabled(marker)).thenReturn(true);
        infoReporter.report(map("gauge", () -> "value"),
                map(),
                map(),
                map(),
                map());

        verify(logger).info(marker, "type=GAUGE, name=prefix.gauge, value=value");
    }

    @Test
    public void reportsCounterValues() {
        final Counter counter = mock(Counter.class);
        when(counter.getCount()).thenReturn(100L);
        when(logger.isInfoEnabled(marker)).thenReturn(true);

        infoReporter.report(map(),
                map("test.counter", counter),
                map(),
                map(),
                map());

        verify(logger).info(marker, "type=COUNTER, name=prefix.test.counter, count=100");
    }

    @Test
    public void reportsHistogramValues() {
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
        when(logger.isInfoEnabled(marker)).thenReturn(true);

        infoReporter.report(map(),
                map(),
                map("test.histogram", histogram),
                map(),
                map());

        verify(logger).info(marker, "type=HISTOGRAM, name=prefix.test.histogram, count=1, min=4, max=2, mean=3.0, stddev=5.0, p50=6.0, p75=7.0, p95=8.0, p98=9.0, p99=10.0, p999=11.0");
    }

    @Test
    public void reportsMeterValues() {
        final Meter meter = mock(Meter.class);
        when(meter.getCount()).thenReturn(1L);
        when(meter.getMeanRate()).thenReturn(2.0);
        when(meter.getOneMinuteRate()).thenReturn(3.0);
        when(meter.getFiveMinuteRate()).thenReturn(4.0);
        when(meter.getFifteenMinuteRate()).thenReturn(5.0);
        when(logger.isInfoEnabled(marker)).thenReturn(true);

        infoReporter.report(map(),
                map(),
                map(),
                map("test.meter", meter),
                map());

        verify(logger).info(marker, "type=METER, name=prefix.test.meter, count=1, m1_rate=3.0, m5_rate=4.0, m15_rate=5.0, mean_rate=2.0, rate_unit=events/second");
    }

    @Test
    public void reportsTimerValues() {
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
        when(logger.isInfoEnabled(marker)).thenReturn(true);

        infoReporter.report(map(),
                map(),
                map(),
                map(),
                map("test.another.timer", timer));

        verify(logger).info(marker, "type=TIMER, name=prefix.test.another.timer, count=1, min=300.0, max=100.0, mean=200.0, stddev=400.0, p50=500.0, p75=600.0, p95=700.0, p98=800.0, p99=900.0, p999=1000.0, m1_rate=3.0, m5_rate=4.0, m15_rate=5.0, mean_rate=2.0, rate_unit=events/second, duration_unit=milliseconds");
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
