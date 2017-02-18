package io.dropwizard.metrics;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.Marker;

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
    
    private Map<String,String> testTags;
    
    @Before
    public void setup() {
    	testTags = new HashMap<>();
    	testTags.put("t1", "v1");
    	testTags.put("k2", "v2");
    	
    }

    @Test
    public void reportsGaugeValuesAtError() throws Exception {
        when(logger.isErrorEnabled(marker)).thenReturn(true);
        errorReporter.report(map("gauge", testTags, gauge("value")),
                this.<Counter>map(),
                this.<Histogram>map(),
                this.<Meter>map(),
                this.<Timer>map());

        verify(logger).error(marker, "type={}, name={}, value={}", new Object[]{"GAUGE", "gauge{t1=v1, k2=v2}", "value"});
    }

    @Test
    public void reportsCounterValuesAtError() throws Exception {
        final Counter counter = mock(Counter.class);
        when(counter.getCount()).thenReturn(100L);
        when(logger.isErrorEnabled(marker)).thenReturn(true);

        errorReporter.report(this.<Gauge>map(),
                map("test.counter", testTags, counter),
                this.<Histogram>map(),
                this.<Meter>map(),
                this.<Timer>map());

        verify(logger).error(marker, "type={}, name={}, count={}", new Object[]{"COUNTER", "test.counter{t1=v1, k2=v2}", 100L});
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
        when(snapshot.getMedian()).thenReturn(6.0);
        when(snapshot.get75thPercentile()).thenReturn(7.0);
        when(snapshot.get95thPercentile()).thenReturn(8.0);
        when(snapshot.get98thPercentile()).thenReturn(9.0);
        when(snapshot.get99thPercentile()).thenReturn(10.0);
        when(snapshot.get999thPercentile()).thenReturn(11.0);

        when(histogram.getSnapshot()).thenReturn(snapshot);
        when(logger.isErrorEnabled(marker)).thenReturn(true);

        errorReporter.report(this.<Gauge>map(),
                this.<Counter>map(),
                map("test.histogram", testTags, histogram),
                this.<Meter>map(),
                this.<Timer>map());

        verify(logger).error(marker,
                "type={}, name={}, count={}, min={}, max={}, mean={}, stddev={}, median={}, p75={}, p95={}, p98={}, p99={}, p999={}",
                "HISTOGRAM",
                "test.histogram{t1=v1, k2=v2}",
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
                11.0);
    }

    @Test
    public void reportsMeterValuesAtError() throws Exception {
        final Meter meter = mock(Meter.class);
        when(meter.getCount()).thenReturn(1L);
        when(meter.getMeanRate()).thenReturn(2.0);
        when(meter.getOneMinuteRate()).thenReturn(3.0);
        when(meter.getFiveMinuteRate()).thenReturn(4.0);
        when(meter.getFifteenMinuteRate()).thenReturn(5.0);
        when(logger.isErrorEnabled(marker)).thenReturn(true);

        errorReporter.report(this.<Gauge>map(),
                this.<Counter>map(),
                this.<Histogram>map(),
                map("test.meter", testTags, meter),
                this.<Timer>map());

        verify(logger).error(marker,
                "type={}, name={}, count={}, mean_rate={}, m1={}, m5={}, m15={}, rate_unit={}",
                "METER",
                "test.meter{t1=v1, k2=v2}",
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
        when(snapshot.getMedian()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(500));
        when(snapshot.get75thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(600));
        when(snapshot.get95thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(700));
        when(snapshot.get98thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(800));
        when(snapshot.get99thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(900));
        when(snapshot.get999thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS
                .toNanos(1000));

        when(timer.getSnapshot()).thenReturn(snapshot);

        when(logger.isErrorEnabled(marker)).thenReturn(true);

        errorReporter.report(this.<Gauge>map(),
                this.<Counter>map(),
                this.<Histogram>map(),
                this.<Meter>map(),
                map("test.another.timer", testTags, timer));

        verify(logger).error(marker,
                "type={}, name={}, count={}, min={}, max={}, mean={}, stddev={}, median={}, p75={}, p95={}, p98={}, p99={}, p999={}, mean_rate={}, m1={}, m5={}, m15={}, rate_unit={}, duration_unit={}",
                "TIMER",
                "test.another.timer{t1=v1, k2=v2}",
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
                2.0,
                3.0,
                4.0,
                5.0,
                "events/second",
                "milliseconds");
    }

    @Test
    public void reportsGaugeValues() throws Exception {
        when(logger.isInfoEnabled(marker)).thenReturn(true);
        infoReporter.report(map("gauge", testTags, gauge("value")),
                this.<Counter>map(),
                this.<Histogram>map(),
                this.<Meter>map(),
                this.<Timer>map());

        verify(logger).info(marker, "type={}, name={}, value={}", new Object[]{"GAUGE", "prefix.gauge{t1=v1, k2=v2}", "value"});
    }

    @Test
    public void reportsCounterValues() throws Exception {
        final Counter counter = mock(Counter.class);
        when(counter.getCount()).thenReturn(100L);
        when(logger.isInfoEnabled(marker)).thenReturn(true);

        infoReporter.report(this.<Gauge>map(),
                map("test.counter", testTags, counter),
                this.<Histogram>map(),
                this.<Meter>map(),
                this.<Timer>map());

        verify(logger).info(marker, "type={}, name={}, count={}", new Object[]{"COUNTER", "prefix.test.counter{t1=v1, k2=v2}", 100L});
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
        when(snapshot.getMedian()).thenReturn(6.0);
        when(snapshot.get75thPercentile()).thenReturn(7.0);
        when(snapshot.get95thPercentile()).thenReturn(8.0);
        when(snapshot.get98thPercentile()).thenReturn(9.0);
        when(snapshot.get99thPercentile()).thenReturn(10.0);
        when(snapshot.get999thPercentile()).thenReturn(11.0);

        when(histogram.getSnapshot()).thenReturn(snapshot);
        when(logger.isInfoEnabled(marker)).thenReturn(true);

        infoReporter.report(this.<Gauge>map(),
                this.<Counter>map(),
                map("test.histogram", testTags, histogram),
                this.<Meter>map(),
                this.<Timer>map());

        verify(logger).info(marker,
                "type={}, name={}, count={}, min={}, max={}, mean={}, stddev={}, median={}, p75={}, p95={}, p98={}, p99={}, p999={}",
                "HISTOGRAM",
                "prefix.test.histogram{t1=v1, k2=v2}",
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
                11.0);
    }

    @Test
    public void reportsMeterValues() throws Exception {
        final Meter meter = mock(Meter.class);
        when(meter.getCount()).thenReturn(1L);
        when(meter.getMeanRate()).thenReturn(2.0);
        when(meter.getOneMinuteRate()).thenReturn(3.0);
        when(meter.getFiveMinuteRate()).thenReturn(4.0);
        when(meter.getFifteenMinuteRate()).thenReturn(5.0);
        when(logger.isInfoEnabled(marker)).thenReturn(true);

        infoReporter.report(this.<Gauge>map(),
                this.<Counter>map(),
                this.<Histogram>map(),
                map("test.meter", testTags, meter),
                this.<Timer>map());

        verify(logger).info(marker,
                "type={}, name={}, count={}, mean_rate={}, m1={}, m5={}, m15={}, rate_unit={}",
                "METER",
                "prefix.test.meter{t1=v1, k2=v2}",
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
        when(snapshot.getMedian()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(500));
        when(snapshot.get75thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(600));
        when(snapshot.get95thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(700));
        when(snapshot.get98thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(800));
        when(snapshot.get99thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(900));
        when(snapshot.get999thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS
                .toNanos(1000));

        when(timer.getSnapshot()).thenReturn(snapshot);
        when(logger.isInfoEnabled(marker)).thenReturn(true);

        infoReporter.report(this.<Gauge>map(),
                this.<Counter>map(),
                this.<Histogram>map(),
                this.<Meter>map(),
                map("test.another.timer", testTags, timer));

        verify(logger).info(marker,
                "type={}, name={}, count={}, min={}, max={}, mean={}, stddev={}, median={}, p75={}, p95={}, p98={}, p99={}, p999={}, mean_rate={}, m1={}, m5={}, m15={}, rate_unit={}, duration_unit={}",
                "TIMER",
                "prefix.test.another.timer{t1=v1, k2=v2}",
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
                2.0,
                3.0,
                4.0,
                5.0,
                "events/second",
                "milliseconds");
    }
    
    @Test
    public void testNameFormatterIsUsed() {
    	Slf4jReporter reporter = Slf4jReporter.forRegistry(registry)
    			.outputTo(logger)
                .markWith(marker)
                .prefixedWith("prefix")
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .withLoggingLevel(Slf4jReporter.LoggingLevel.INFO)
                .filter(MetricFilter.ALL)
    			.withNameFormatter(MetricNameFormatter.APPEND_TAG_VALUES)
    			.build();
    	
    	when(logger.isInfoEnabled(marker)).thenReturn(true);
    	reporter.report(map("gauge", testTags, gauge("value")),
                this.<Counter>map(),
                this.<Histogram>map(),
                this.<Meter>map(),
                this.<Timer>map());

        verify(logger).info(marker, "type={}, name={}, value={}", new Object[]{"GAUGE", "prefix.gauge.v1.v2", "value"});
    }

    private <T> SortedMap<MetricName, T> map() {
        return new TreeMap<MetricName, T>();
    }

    private <T> SortedMap<MetricName, T> map(String name, Map<String,String> tags, T metric) {
        final TreeMap<MetricName, T> map = new TreeMap<MetricName, T>();
        map.put(new MetricName(name,tags), metric);
        return map;
    }

    private <T> Gauge gauge(T value) {
        final Gauge gauge = mock(Gauge.class);
        when(gauge.getValue()).thenReturn(value);
        return gauge;
    }

}
