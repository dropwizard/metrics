package com.yammer.metrics.reporting.tests;

import com.yammer.metrics.core.*;
import com.yammer.metrics.reporting.AbstractPollingReporter;
import com.yammer.metrics.stats.Snapshot;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public abstract class AbstractPollingReporterTest {

    protected final Clock clock = mock(Clock.class);
    protected AbstractPollingReporter reporter;
    protected ByteArrayOutputStream out;
    protected TestMetricsRegistry registry;

    @Before
    public void init() throws Exception {
        when(clock.getTick()).thenReturn(1234L);
        when(clock.getTime()).thenReturn(5678L);
        registry = new TestMetricsRegistry();
        out = new ByteArrayOutputStream();
        reporter = createReporter(registry, out, clock);
    }

    protected static class TestMetricsRegistry extends MetricsRegistry {
        public <T extends Metric> T add(MetricName name, T metric) {
            return getOrAdd(name, metric);
        }
    }

    protected <T extends Metric> void assertReporterOutput(Callable<T> action, String... expected) throws Exception {
        // Invoke the callable to trigger (ie, mark()/inc()/etc) and return the metric
        final T metric = action.call();
        try {
            // Add the metric to the registry, run the reporter and flush the result
            registry.add(new MetricName(Object.class, "metric"), metric);
            reporter.run();
            out.flush();
            final String[] lines = out.toString().split("\r?\n|\r");
            // Assertions: first check that the line count matches then compare line by line ignoring leading and trailing whitespace
            assertEquals("Line count mismatch, was:\n" + Arrays.toString(lines) + "\nexpected:\n" + Arrays
                    .toString(expected) + "\n", expected.length,
                         lines.length);
            for (int i = 0; i < lines.length; i++) {
                if (!expected[i].trim().equals(lines[i].trim())) {
                    System.err.println("Failure comparing line " + (1 + i));
                    System.err.println("Was:      '" + lines[i] + "'");
                    System.err.println("Expected: '" + expected[i] + "'\n");
                }
                assertEquals(expected[i].trim(), lines[i].trim());
            }
        } finally {
            reporter.shutdown();
        }
    }

    protected abstract AbstractPollingReporter createReporter(MetricsRegistry registry, OutputStream out, Clock clock) throws Exception;

    @Test
    public final void counter() throws Exception {
        final long count = new Random().nextInt(Integer.MAX_VALUE);
        assertReporterOutput(
                new Callable<Counter>() {
                    @Override
                    public Counter call() throws Exception {
                        return createCounter(count);
                    }
                },
                expectedCounterResult(count));
    }

    @Test
    public final void histogram() throws Exception {
        assertReporterOutput(
                new Callable<Histogram>() {
                    @Override
                    public Histogram call() throws Exception {
                        return createHistogram();
                    }
                },
                expectedHistogramResult());
    }

    @Test
    public final void meter() throws Exception {
        assertReporterOutput(
                new Callable<Meter>() {
                    @Override
                    public Meter call() throws Exception {
                        return createMeter();
                    }
                },
                expectedMeterResult());
    }

    @Test
    public final void timer() throws Exception {
        assertReporterOutput(
                new Callable<Timer>() {
                    @Override
                    public Timer call() throws Exception {
                        return createTimer();
                    }
                },
                expectedTimerResult());
    }

    @Test
    public final void gauge() throws Exception {
        final String value = "gaugeValue";
        assertReporterOutput(
                new Callable<Gauge<String>>() {
                    @Override
                    public Gauge<String> call() throws Exception {
                        return createGauge();
                    }
                },
                expectedGaugeResult(value));
    }

    static Counter createCounter(long count) throws Exception {
        final Counter mock = mock(Counter.class);
        when(mock.getCount()).thenReturn(count);
        return mock;
    }

    static Histogram createHistogram() throws Exception {
        final Histogram mock = mock(Histogram.class);
        setupSummarizableMock(mock);
        setupSamplingMock(mock);
        return mock;
    }

    
    static Gauge<String> createGauge() throws Exception {
        @SuppressWarnings("unchecked")
        final Gauge<String> mock = mock(Gauge.class);
        when(mock.getValue()).thenReturn("gaugeValue");
        return mock;
    }


    static Timer createTimer() throws Exception {
        final Timer mock = mock(Timer.class);
        when(mock.getDurationUnit()).thenReturn(TimeUnit.MILLISECONDS);
        setupSummarizableMock(mock);
        setupMeteredMock(mock);
        setupSamplingMock(mock);
        return mock;
    }

    static Meter createMeter() throws Exception {
        final Meter mock = mock(Meter.class);
        setupMeteredMock(mock);
        return mock;
    }

    static abstract class MetricsProcessorAction implements Answer<Object> {
        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            @SuppressWarnings("unchecked")
            final MetricProcessor<Object> processor = (MetricProcessor<Object>) invocation.getArguments()[0];
            final MetricName name = (MetricName) invocation.getArguments()[1];
            final Object context = invocation.getArguments()[2];
            delegateToProcessor(processor, name, context);
            return null;
        }

        abstract void delegateToProcessor(MetricProcessor<Object> processor, MetricName name, Object context) throws Exception;
    }

    static void setupSummarizableMock(Summarizable summarizable) {
        when(summarizable.getMin()).thenReturn(1d);
        when(summarizable.getMax()).thenReturn(3d);
        when(summarizable.getMean()).thenReturn(2d);
        when(summarizable.getStdDev()).thenReturn(1.5d);
    }

    static void setupMeteredMock(Metered metered) {
        when(metered.getCount()).thenReturn(1L);
        when(metered.getOneMinuteRate()).thenReturn(1d);
        when(metered.getFiveMinuteRate()).thenReturn(5d);
        when(metered.getFifteenMinuteRate()).thenReturn(15d);
        when(metered.getMeanRate()).thenReturn(2d);
        when(metered.getEventType()).thenReturn("eventType");
        when(metered.getRateUnit()).thenReturn(TimeUnit.SECONDS);
    }

    static void setupSamplingMock(Sampling sampling) {
        final double[] values = new double[1000];
        for (int i = 0; i < values.length; i++) {
            values[i] = i / 1000.0;
        }
        when(sampling.getSnapshot()).thenReturn(new Snapshot(values));
    }

    public abstract String[] expectedGaugeResult(String value);

    public abstract String[] expectedTimerResult();

    public abstract String[] expectedMeterResult();

    public abstract String[] expectedHistogramResult();

    public abstract String[] expectedCounterResult(long count);

}
