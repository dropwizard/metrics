package io.dropwizard.metrics5;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.util.EnumSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static io.dropwizard.metrics5.MetricAttribute.COUNT;
import static io.dropwizard.metrics5.MetricAttribute.M1_RATE;
import static io.dropwizard.metrics5.MetricAttribute.MEAN_RATE;
import static io.dropwizard.metrics5.MetricAttribute.MIN;
import static io.dropwizard.metrics5.MetricAttribute.P50;
import static io.dropwizard.metrics5.MetricAttribute.P999;
import static io.dropwizard.metrics5.MetricAttribute.STDDEV;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class Slf4jReporterTest {

    private final Logger logger = mock(Logger.class);
    private final Marker marker = mock(Marker.class);
    private final MetricRegistry registry = mock(MetricRegistry.class);

    /**
     * The set of disabled metric attributes to pass to the Slf4jReporter builder
     * in the default factory methods of {@link #infoReporter}
     * and {@link #errorReporter}.
     *
     * This value can be overridden by tests before calling the {@link #infoReporter}
     * and {@link #errorReporter} factory methods.
     */
    private Set<MetricAttribute> disabledMetricAttributes = null;

    private Slf4jReporter infoReporter() {
        return Slf4jReporter.forRegistry(registry)
                .outputTo(logger)
                .markWith(marker)
                .prefixedWith("prefix")
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .withLoggingLevel(Slf4jReporter.LoggingLevel.INFO)
                .filter(MetricFilter.ALL)
                .disabledMetricAttributes(disabledMetricAttributes)
                .build();
    }

    private Slf4jReporter errorReporter() {
        return Slf4jReporter.forRegistry(registry)
                .outputTo(logger)
                .markWith(marker)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .withLoggingLevel(Slf4jReporter.LoggingLevel.ERROR)
                .filter(MetricFilter.ALL)
                .disabledMetricAttributes(disabledMetricAttributes)
                .build();
    }

    @Test
    void reportsGaugeValuesAtErrorDefault() {
        reportsGaugeValuesAtError();
    }

    @Test
    void reportsGaugeValuesAtErrorAllDisabled() {
        disabledMetricAttributes = EnumSet.allOf(MetricAttribute.class); // has no effect
        reportsGaugeValuesAtError();
    }

    private void reportsGaugeValuesAtError() {
        when(logger.isErrorEnabled(marker)).thenReturn(true);
        errorReporter().report(map("gauge", () -> "value"),
                map(),
                map(),
                map(),
                map());

        verify(logger).error(marker, "type=GAUGE, name=gauge, value=value");
    }


    private Timer timer() {
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
        return timer;
    }

    private Histogram histogram() {
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
        return histogram;
    }

    private Meter meter() {
        final Meter meter = mock(Meter.class);
        when(meter.getCount()).thenReturn(1L);
        when(meter.getMeanRate()).thenReturn(2.0);
        when(meter.getOneMinuteRate()).thenReturn(3.0);
        when(meter.getFiveMinuteRate()).thenReturn(4.0);
        when(meter.getFifteenMinuteRate()).thenReturn(5.0);
        return meter;
    }

    private Counter counter() {
        final Counter counter = mock(Counter.class);
        when(counter.getCount()).thenReturn(100L);
        return counter;
    }

    @Test
    void reportsCounterValuesAtErrorDefault() {
        reportsCounterValuesAtError();
    }

    @Test
    void reportsCounterValuesAtErrorAllDisabled() {
        disabledMetricAttributes = EnumSet.allOf(MetricAttribute.class); // has no effect
        reportsCounterValuesAtError();
    }

    private void reportsCounterValuesAtError() {
        final Counter counter = counter();
        when(logger.isErrorEnabled(marker)).thenReturn(true);

        errorReporter().report(map(),
                map("test.counter", counter),
                map(),
                map(),
                map());

        verify(logger).error(marker, "type=COUNTER, name=test.counter, count=100");
    }

    @Test
    void reportsHistogramValuesAtErrorDefault() {
        reportsHistogramValuesAtError("type=HISTOGRAM, name=test.histogram, count=1, min=4, " +
                "max=2, mean=3.0, stddev=5.0, p50=6.0, p75=7.0, p95=8.0, p98=9.0, p99=10.0, p999=11.0");
    }

    @Test
    void reportsHistogramValuesAtErrorWithDisabledMetricAttributes() {
        disabledMetricAttributes = EnumSet.of(COUNT, MIN, P50);
        reportsHistogramValuesAtError("type=HISTOGRAM, name=test.histogram, max=2, mean=3.0, " +
                "stddev=5.0, p75=7.0, p95=8.0, p98=9.0, p99=10.0, p999=11.0");
    }

    private void reportsHistogramValuesAtError(final String expectedLog) {
        final Histogram histogram = histogram();
        when(logger.isErrorEnabled(marker)).thenReturn(true);

        errorReporter().report(map(),
                map(),
                map("test.histogram", histogram),
                map(),
                map());

        verify(logger).error(marker, expectedLog);
    }

    @Test
    void reportsMeterValuesAtErrorDefault() {
        reportsMeterValuesAtError("type=METER, name=test.meter, count=1, m1_rate=3.0, m5_rate=4.0, " +
                "m15_rate=5.0, mean_rate=2.0, rate_unit=events/second");
    }

    @Test
    void reportsMeterValuesAtErrorWithDisabledMetricAttributes() {
        disabledMetricAttributes = EnumSet.of(MIN, P50, M1_RATE);
        reportsMeterValuesAtError("type=METER, name=test.meter, count=1, m5_rate=4.0, m15_rate=5.0, " +
                "mean_rate=2.0, rate_unit=events/second");
    }

    private void reportsMeterValuesAtError(final String expectedLog) {
        final Meter meter = meter();
        when(logger.isErrorEnabled(marker)).thenReturn(true);

        errorReporter().report(map(),
                map(),
                map(),
                map("test.meter", meter),
                map());

        verify(logger).error(marker, expectedLog);
    }


    @Test
    void reportsTimerValuesAtErrorDefault() {
        reportsTimerValuesAtError("type=TIMER, name=test.another.timer, count=1, min=300.0, max=100.0, " +
                "mean=200.0, stddev=400.0, p50=500.0, p75=600.0, p95=700.0, p98=800.0, p99=900.0, p999=1000.0, " +
                "m1_rate=3.0, m5_rate=4.0, m15_rate=5.0, mean_rate=2.0, rate_unit=events/second, " +
                "duration_unit=milliseconds");
    }

    @Test
    void reportsTimerValuesAtErrorWithDisabledMetricAttributes() {
        disabledMetricAttributes = EnumSet.of(MIN, STDDEV, P999, MEAN_RATE);
        reportsTimerValuesAtError("type=TIMER, name=test.another.timer, count=1, max=100.0, mean=200.0, " +
                "p50=500.0, p75=600.0, p95=700.0, p98=800.0, p99=900.0, m1_rate=3.0, m5_rate=4.0, m15_rate=5.0, " +
                "rate_unit=events/second, duration_unit=milliseconds");
    }

    private void reportsTimerValuesAtError(final String expectedLog) {
        final Timer timer = timer();

        when(logger.isErrorEnabled(marker)).thenReturn(true);

        errorReporter().report(map(),
                map(),
                map(),
                map(),
                map("test.another.timer", timer));

        verify(logger).error(marker, expectedLog);
    }

    @Test
    void reportsGaugeValuesDefault() {
        when(logger.isInfoEnabled(marker)).thenReturn(true);
        infoReporter().report(map("gauge", () -> "value"),
                map(),
                map(),
                map(),
                map());

        verify(logger).info(marker, "type=GAUGE, name=prefix.gauge, value=value");
    }


    @Test
    void reportsCounterValuesDefault() {
        final Counter counter = counter();
        when(logger.isInfoEnabled(marker)).thenReturn(true);

        infoReporter().report(map(),
                map("test.counter", counter),
                map(),
                map(),
                map());

        verify(logger).info(marker, "type=COUNTER, name=prefix.test.counter, count=100");
    }

    @Test
    void reportsHistogramValuesDefault() {
        final Histogram histogram = histogram();
        when(logger.isInfoEnabled(marker)).thenReturn(true);

        infoReporter().report(map(),
                map(),
                map("test.histogram", histogram),
                map(),
                map());

        verify(logger).info(marker, "type=HISTOGRAM, name=prefix.test.histogram, count=1, min=4, max=2, mean=3.0, " +
                "stddev=5.0, p50=6.0, p75=7.0, p95=8.0, p98=9.0, p99=10.0, p999=11.0");
    }

    @Test
    void reportsMeterValuesDefault() {
        final Meter meter = meter();
        when(logger.isInfoEnabled(marker)).thenReturn(true);

        infoReporter().report(map(),
                map(),
                map(),
                map("test.meter", meter),
                map());

        verify(logger).info(marker, "type=METER, name=prefix.test.meter, count=1, m1_rate=3.0, m5_rate=4.0, " +
                "m15_rate=5.0, mean_rate=2.0, rate_unit=events/second");
    }

    @Test
    void reportsTimerValuesDefault() {
        final Timer timer = timer();
        when(logger.isInfoEnabled(marker)).thenReturn(true);

        infoReporter().report(map(),
                map(),
                map(),
                map(),
                map("test.another.timer", timer));

        verify(logger).info(marker, "type=TIMER, name=prefix.test.another.timer, count=1, min=300.0, max=100.0, " +
                "mean=200.0, stddev=400.0, p50=500.0, p75=600.0, p95=700.0, p98=800.0, p99=900.0, p999=1000.0," +
                " m1_rate=3.0, m5_rate=4.0, m15_rate=5.0, mean_rate=2.0, rate_unit=events/second, duration_unit=milliseconds");
    }


    @Test
    void reportsAllMetricsDefault() {
        when(logger.isInfoEnabled(marker)).thenReturn(true);

        infoReporter().report(map("test.gauge", () -> "value"),
                map("test.counter", counter()),
                map("test.histogram", histogram()),
                map("test.meter", meter()),
                map("test.timer", timer()));

        verify(logger).info(marker, "type=GAUGE, name=prefix.test.gauge, value=value");
        verify(logger).info(marker, "type=COUNTER, name=prefix.test.counter, count=100");
        verify(logger).info(marker, "type=HISTOGRAM, name=prefix.test.histogram, count=1, min=4, max=2, mean=3.0, " +
                "stddev=5.0, p50=6.0, p75=7.0, p95=8.0, p98=9.0, p99=10.0, p999=11.0");
        verify(logger).info(marker, "type=METER, name=prefix.test.meter, count=1, m1_rate=3.0, m5_rate=4.0, " +
                "m15_rate=5.0, mean_rate=2.0, rate_unit=events/second");
        verify(logger).info(marker, "type=TIMER, name=prefix.test.timer, count=1, min=300.0, max=100.0, " +
                "mean=200.0, stddev=400.0, p50=500.0, p75=600.0, p95=700.0, p98=800.0, p99=900.0, p999=1000.0," +
                " m1_rate=3.0, m5_rate=4.0, m15_rate=5.0, mean_rate=2.0, rate_unit=events/second, duration_unit=milliseconds");
    }

    private <T> SortedMap<MetricName, T> map() {
        return new TreeMap<>();
    }

    private <T> SortedMap<MetricName, T> map(String name, T metric) {
        final SortedMap<MetricName, T> map = new TreeMap<>();
        map.put(MetricName.build(name), metric);
        return map;
    }

}
