package io.dropwizard.metrics5;

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
        errorReporter.report(map(MetricName.build("gauge"), () -> "value"),
                map(),
                map(),
                map(),
                map());

        verify(logger).error(marker, "type={}, name={}, value={}", "GAUGE", MetricName.build("gauge"), "value");
    }

    @Test
    public void reportsCounterValuesAtError() {
        final Counter counter = mock(Counter.class);
        when(counter.getCount()).thenReturn(100L);
        when(logger.isErrorEnabled(marker)).thenReturn(true);

        errorReporter.report(map(),
                map(MetricName.build("test.counter"), counter),
                map(),
                map(),
                map());

        verify(logger).error(marker, "type={}, name={}, count={}", "COUNTER", MetricName.build("test.counter"), 100L);
    }

    @Test
    public void reportsHistogramValuesAtError() {
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
        when(logger.isErrorEnabled(marker)).thenReturn(true);

        errorReporter.report(map(),
                map(),
                map(MetricName.build("test.histogram"), histogram),
                map(),
                map());

        verify(logger).error(marker,
                "type={}, name={}, count={}, sum={}, min={}, max={}, mean={}, stddev={}, median={}, p75={}, p95={}, p98={}, p99={}, p999={}",
                "HISTOGRAM",
                MetricName.build( "test.histogram"),
                1L,
                12L,
                4L,
                2L,
                3.0,
                5.0,
                6.0,
                7.0,
                8.0,
                9.0,
                10.0,
                11.0);
    }

    @Test
    public void reportsMeterValuesAtError() {
        final Meter meter = mock(Meter.class);
        when(meter.getCount()).thenReturn(1L);
        when(meter.getSum()).thenReturn(6L);
        when(meter.getMeanRate()).thenReturn(2.0);
        when(meter.getOneMinuteRate()).thenReturn(3.0);
        when(meter.getFiveMinuteRate()).thenReturn(4.0);
        when(meter.getFifteenMinuteRate()).thenReturn(5.0);
        when(logger.isErrorEnabled(marker)).thenReturn(true);

        errorReporter.report(map(),
                map(),
                map(),
                map(MetricName.build("test.meter"), meter),
                map());

        verify(logger).error(marker,
                "type={}, name={}, count={}, sum={}, mean_rate={}, m1={}, m5={}, m15={}, rate_unit={}",
                "METER",
                MetricName.build("test.meter"),
                1L,
                6L,
                2.0,
                3.0,
                4.0,
                5.0,
                "events/second");
    }

    @Test
    public void reportsTimerValuesAtError() {
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

        when(logger.isErrorEnabled(marker)).thenReturn(true);

        errorReporter.report(map(),
                map(),
                map(),
                map(),
                map(MetricName.build("test.another.timer"), timer));

        verify(logger).error(marker,
                "type={}, name={}, count={}, sum={}, min={}, max={}, mean={}, stddev={}, median={}, p75={}, p95={}, p98={}, p99={}, p999={}, mean_rate={}, m1={}, m5={}, m15={}, rate_unit={}, duration_unit={}",
                "TIMER",
                MetricName.build("test.another.timer"),
                1L,
                6L,
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
                2.0,
                3.0,
                4.0,
                5.0,
                "events/second",
                "milliseconds");
    }

    @Test
    public void reportsGaugeValues() {
        when(logger.isInfoEnabled(marker)).thenReturn(true);
        infoReporter.report(map(MetricName.build("gauge"), () -> "value"),
                map(),
                map(),
                map(),
                map());

        verify(logger).info(marker, "type={}, name={}, value={}", "GAUGE", MetricName.build("prefix.gauge"), "value");
    }

    @Test
    public void reportsCounterValues() {
        final Counter counter = mock(Counter.class);
        when(counter.getCount()).thenReturn(100L);
        when(logger.isInfoEnabled(marker)).thenReturn(true);

        infoReporter.report(map(),
                map(MetricName.build("test.counter"), counter),
                map(),
                map(),
                map());

        verify(logger).info(marker, "type={}, name={}, count={}", "COUNTER", MetricName.build("prefix.test.counter"), 100L);
    }

    @Test
    public void reportsHistogramValues() {
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
        when(logger.isInfoEnabled(marker)).thenReturn(true);

        infoReporter.report(map(),
                map(),
                map(MetricName.build("test.histogram"), histogram),
                map(),
                map());

        verify(logger).info(marker,
                "type={}, name={}, count={}, sum={}, min={}, max={}, mean={}, stddev={}, median={}, p75={}, p95={}, p98={}, p99={}, p999={}",
                "HISTOGRAM",
                MetricName.build("prefix.test.histogram"),
                1L,
                12L,
                4L,
                2L,
                3.0,
                5.0,
                6.0,
                7.0,
                8.0,
                9.0,
                10.0,
                11.0);
    }

    @Test
    public void reportsMeterValues() {
        final Meter meter = mock(Meter.class);
        when(meter.getCount()).thenReturn(1L);
        when(meter.getSum()).thenReturn(6L);
        when(meter.getMeanRate()).thenReturn(2.0);
        when(meter.getOneMinuteRate()).thenReturn(3.0);
        when(meter.getFiveMinuteRate()).thenReturn(4.0);
        when(meter.getFifteenMinuteRate()).thenReturn(5.0);
        when(logger.isInfoEnabled(marker)).thenReturn(true);

        infoReporter.report(map(),
                map(),
                map(),
                map(MetricName.build("test.meter"), meter),
                map());

        verify(logger).info(marker,
                "type={}, name={}, count={}, sum={}, mean_rate={}, m1={}, m5={}, m15={}, rate_unit={}",
                "METER",
                MetricName.build("prefix.test.meter"),
                1L,
                6L,
                2.0,
                3.0,
                4.0,
                5.0,
                "events/second");
    }

    @Test
    public void reportsTimerValues() {
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
        when(logger.isInfoEnabled(marker)).thenReturn(true);

        infoReporter.report(map(),
                map(),
                map(),
                map(),
                map(MetricName.build("test.another.timer"), timer));

        verify(logger).info(marker,
                "type={}, name={}, count={}, sum={}, min={}, max={}, mean={}, stddev={}, median={}, p75={}, p95={}, p98={}, p99={}, p999={}, mean_rate={}, m1={}, m5={}, m15={}, rate_unit={}, duration_unit={}",
                "TIMER",
                MetricName.build("prefix.test.another.timer"),
                1L,
                6L,
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
                2.0,
                3.0,
                4.0,
                5.0,
                "events/second",
                "milliseconds");
    }

    private <T> SortedMap<MetricName, T> map() {
        return new TreeMap<>();
    }

    private <T> SortedMap<MetricName, T> map(MetricName name, T metric) {
        final TreeMap<MetricName, T> map = new TreeMap<>();
        map.put(name, metric);
        return map;
    }

}
