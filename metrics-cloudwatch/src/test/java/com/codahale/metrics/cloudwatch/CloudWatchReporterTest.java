package com.codahale.metrics.cloudwatch;

import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.codahale.metrics.*;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.internal.matchers.And;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CloudWatchReporterTest {
    private final long timestamp = 1000198;
    private final Clock clock = mock(Clock.class);
    private final AmazonCloudWatchClient cloudwatch = mock(AmazonCloudWatchClient.class);
    private final MetricRegistry registry = mock(MetricRegistry.class);
    private final CloudWatchReporter reporter = CloudWatchReporter.forRegistry("namespace", registry)
            .withClock(clock)
            .prefixedWith("prefix")
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .filter(MetricFilter.ALL)
            .build(cloudwatch);

    @Before
    public void setUp() throws Exception {
        when(clock.getTime()).thenReturn(timestamp * 1000);
    }

    @Test
    public void doesNotReportStringGaugeValues() throws Exception {
        reporter.report(map("gauge", gauge("value")),
                this.<Counter>map(),
                this.<Histogram>map(),
                this.<Meter>map(),
                this.<Timer>map());

        verifyNoMoreInteractions(cloudwatch);
    }




    @Test
    public void reportsLongGaugeValues() throws Exception {
        reporter.report(map("gauge", gauge(1L)),
                this.<Counter>map(),
                this.<Histogram>map(),
                this.<Meter>map(),
                this.<Timer>map());

        List<Matcher> asserts = new ArrayList<Matcher>();
        asserts.add(hasMetricDatum("prefix.gauge", 1));
        asserts.add(ofSize(asserts.size()));

        verify(cloudwatch).putMetricData(Matchers.<PutMetricDataRequest>argThat(new And(asserts)));

        verifyNoMoreInteractions(cloudwatch);
    }

    @Test
    public void reportsDoubleGaugeValues() throws Exception {
        reporter.report(map("gauge", gauge(1.1)),
                this.<Counter>map(),
                this.<Histogram>map(),
                this.<Meter>map(),
                this.<Timer>map());

        List<Matcher> asserts = new ArrayList<Matcher>();
        asserts.add(hasMetricDatum("prefix.gauge", 1.1));
        asserts.add(ofSize(asserts.size()));

        verify(cloudwatch).putMetricData(Matchers.<PutMetricDataRequest>argThat(new And(asserts)));


        verifyNoMoreInteractions(cloudwatch);
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

        List<Matcher> asserts = new ArrayList<Matcher>();
        asserts.add(hasMetricDatum("prefix.counter.count", 100));
        asserts.add(ofSize(asserts.size()));

        verify(cloudwatch).putMetricData(Matchers.<PutMetricDataRequest>argThat(new And(asserts)));

        verifyNoMoreInteractions(cloudwatch);
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
        
        List<Matcher> asserts = new ArrayList<Matcher>();
        asserts.add(hasMetricDatum("prefix.histogram.count", 1));
        asserts.add(hasMetricDatum("prefix.histogram.max", 2));
        asserts.add(hasMetricDatum("prefix.histogram.mean", 3.00));
        asserts.add(hasMetricDatum("prefix.histogram.min", 4));
        asserts.add(hasMetricDatum("prefix.histogram.stddev", 5.00));
        asserts.add(hasMetricDatum("prefix.histogram.p50", 6.00));
        asserts.add(hasMetricDatum("prefix.histogram.p75", 7.00));
        asserts.add(hasMetricDatum("prefix.histogram.p95", 8.00));
        asserts.add(hasMetricDatum("prefix.histogram.p98", 9.00));
        asserts.add(hasMetricDatum("prefix.histogram.p99", 10.00));
        asserts.add(hasMetricDatum("prefix.histogram.p999", 11.00));
        asserts.add(ofSize(asserts.size()));

        verify(cloudwatch).putMetricData(Matchers.<PutMetricDataRequest>argThat(new And(asserts)));

        verifyNoMoreInteractions(cloudwatch);
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

        List<Matcher> asserts = new ArrayList<Matcher>();
        asserts.add(hasMetricDatum("prefix.meter.count", 1));
        asserts.add(hasMetricDatum("prefix.meter.m1_rate", 2.00));
        asserts.add(hasMetricDatum("prefix.meter.m5_rate", 3.00));
        asserts.add(hasMetricDatum("prefix.meter.m15_rate", 4.00));
        asserts.add(hasMetricDatum("prefix.meter.mean_rate", 5.00));
        asserts.add(ofSize(asserts.size()));

        verify(cloudwatch).putMetricData(Matchers.<PutMetricDataRequest>argThat(new And(asserts)));

        verifyNoMoreInteractions(cloudwatch);
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

        List<Matcher> asserts = new ArrayList<Matcher>();
        asserts.add(hasMetricDatum("prefix.timer.max", 100.00));
        asserts.add(hasMetricDatum("prefix.timer.mean", 200.00));
        asserts.add(hasMetricDatum("prefix.timer.min", 300.00));
        asserts.add(hasMetricDatum("prefix.timer.stddev", 400.00));
        asserts.add(hasMetricDatum("prefix.timer.p50", 500.00));
        asserts.add(hasMetricDatum("prefix.timer.p75", 600.00));
        asserts.add(hasMetricDatum("prefix.timer.p95", 700.00));
        asserts.add(hasMetricDatum("prefix.timer.p98", 800.00));
        asserts.add(hasMetricDatum("prefix.timer.p99", 900.00));
        asserts.add(hasMetricDatum("prefix.timer.p999", 1000.00));
        asserts.add(hasMetricDatum("prefix.timer.count", 1));
        asserts.add(hasMetricDatum("prefix.timer.m1_rate", 3.00));
        asserts.add(hasMetricDatum("prefix.timer.m5_rate", 4.00));
        asserts.add(hasMetricDatum("prefix.timer.m15_rate", 5.00));
        asserts.add(hasMetricDatum("prefix.timer.mean_rate", 2.00));

        asserts.add(ofSize(asserts.size()));

        verify(cloudwatch).putMetricData(Matchers.<PutMetricDataRequest>argThat(new And(asserts)));

        verifyNoMoreInteractions(cloudwatch);
    }

    @Test
    public void reportsMoreThan20MetricData() throws Exception {


        final Timer timer1 = mock(Timer.class);
        when(timer1.getCount()).thenReturn(1L);
        when(timer1.getMeanRate()).thenReturn(2.0);
        when(timer1.getOneMinuteRate()).thenReturn(3.0);
        when(timer1.getFiveMinuteRate()).thenReturn(4.0);
        when(timer1.getFifteenMinuteRate()).thenReturn(5.0);

        final Timer timer2 = mock(Timer.class);
        when(timer1.getCount()).thenReturn(1L);
        when(timer1.getMeanRate()).thenReturn(2.0);
        when(timer1.getOneMinuteRate()).thenReturn(3.0);
        when(timer1.getFiveMinuteRate()).thenReturn(4.0);
        when(timer1.getFifteenMinuteRate()).thenReturn(5.0);


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

        when(timer1.getSnapshot()).thenReturn(snapshot);
        when(timer2.getSnapshot()).thenReturn(snapshot);

        reporter.report(this.<Gauge>map(),
                this.<Counter>map(),
                this.<Histogram>map(),
                this.<Meter>map(),
                map("timer1", timer1, "timer2", timer1));

        List<Matcher> firstCall = new ArrayList<Matcher>();
        firstCall.add(hasMetricDatum("prefix.timer1.max", 100.00));
        firstCall.add(hasMetricDatum("prefix.timer1.mean", 200.00));
        firstCall.add(hasMetricDatum("prefix.timer1.min", 300.00));
        firstCall.add(hasMetricDatum("prefix.timer1.stddev", 400.00));
        firstCall.add(hasMetricDatum("prefix.timer1.p50", 500.00));
        firstCall.add(hasMetricDatum("prefix.timer1.p75", 600.00));
        firstCall.add(hasMetricDatum("prefix.timer1.p95", 700.00));
        firstCall.add(hasMetricDatum("prefix.timer1.p98", 800.00));
        firstCall.add(hasMetricDatum("prefix.timer1.p99", 900.00));
        firstCall.add(hasMetricDatum("prefix.timer1.p999", 1000.00));
        firstCall.add(hasMetricDatum("prefix.timer1.count", 1));
        firstCall.add(hasMetricDatum("prefix.timer1.m1_rate", 3.00));
        firstCall.add(hasMetricDatum("prefix.timer1.m5_rate", 4.00));
        firstCall.add(hasMetricDatum("prefix.timer1.m15_rate", 5.00));
        firstCall.add(hasMetricDatum("prefix.timer1.mean_rate", 2.00));
        firstCall.add(hasMetricDatum("prefix.timer2.max", 100.00));
        firstCall.add(hasMetricDatum("prefix.timer2.mean", 200.00));
        firstCall.add(hasMetricDatum("prefix.timer2.min", 300.00));
        firstCall.add(hasMetricDatum("prefix.timer2.stddev", 400.00));
        firstCall.add(hasMetricDatum("prefix.timer2.p50", 500.00));
        firstCall.add(ofSize(20));

        List<Matcher> secondCall = new ArrayList<Matcher>();
        secondCall.add(hasMetricDatum("prefix.timer2.p75", 600.00));
        secondCall.add(hasMetricDatum("prefix.timer2.p95", 700.00));
        secondCall.add(hasMetricDatum("prefix.timer2.p98", 800.00));
        secondCall.add(hasMetricDatum("prefix.timer2.p99", 900.00));
        secondCall.add(hasMetricDatum("prefix.timer2.p999", 1000.00));
        secondCall.add(hasMetricDatum("prefix.timer2.count", 1));
        secondCall.add(hasMetricDatum("prefix.timer2.m1_rate", 3.00));
        secondCall.add(hasMetricDatum("prefix.timer2.m5_rate", 4.00));
        secondCall.add(hasMetricDatum("prefix.timer2.m15_rate", 5.00));
        secondCall.add(hasMetricDatum("prefix.timer2.mean_rate", 2.00));
        secondCall.add(ofSize(secondCall.size()));



        ArgumentCaptor<PutMetricDataRequest> captor = ArgumentCaptor.forClass(PutMetricDataRequest.class);
        verify(cloudwatch, times(2)).putMetricData(captor.capture());

        assertTrue(new And(firstCall).matches(captor.getAllValues().get(0)));
        assertTrue(new And(secondCall).matches(captor.getAllValues().get(1)));




        verifyNoMoreInteractions(cloudwatch);
    }



    private Matcher ofSize(final int size) {
        return new TypeSafeMatcher<PutMetricDataRequest>() {
            @Override
            protected boolean matchesSafely(PutMetricDataRequest item) {
                assertEquals(item.getMetricData().size(), size);
                return true;
            }

            @Override
            public void describeTo(Description description) {
            }
        };
    }

    private <T> SortedMap<String, T> map() {
        return new TreeMap<String, T>();
    }

    private <T> SortedMap<String, T> map(String name, T metric) {
        final TreeMap<String, T> map = new TreeMap<String, T>();
        map.put(name, metric);
        return map;
    }

    private <T> SortedMap<String, T> map(String name1, T metric1, String name2, T metric2) {
        final TreeMap<String, T> map = new TreeMap<String, T>();
        map.put(name1, metric1);
        map.put(name2, metric2);
        return map;
    }


    private <T> Gauge gauge(T value) {
        final Gauge gauge = mock(Gauge.class);
        when(gauge.getValue()).thenReturn(value);
        return gauge;
    }

    private Matcher<PutMetricDataRequest> hasMetricDatum(final String name, final double value) {
        return new TypeSafeMatcher<PutMetricDataRequest>() {
            @Override
            protected boolean matchesSafely(PutMetricDataRequest item) {
                assertEquals(item.getNamespace(), "namespace");
                MetricDatum metricDatum = findByName(item, name);

                assertEquals(metricDatum.getMetricName(), (name));
                assertEquals(metricDatum.getValue(), Double.valueOf(value));
                assertEquals(metricDatum.getDimensions().get(0).getName(), "hostname");
                assertEquals(metricDatum.getDimensions().size(), 1);
                assertNotNull(metricDatum.getDimensions().get(0).getValue());
                return true;
            }

            private MetricDatum findByName(PutMetricDataRequest item, String name) {
                List<MetricDatum> found = new ArrayList<MetricDatum>();
                for (MetricDatum datum : item.getMetricData()) {
                    if(datum.getMetricName().equals(name)){
                        found.add(datum);
                    }
                }
                assertEquals(found.size(), 1);
                return found.get(0);
            }

            @Override
            public void describeTo(Description description) {
            }
        };
    }


}
