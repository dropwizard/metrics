package com.yammer.metrics.reporting.tests;

import com.yammer.metrics.core.*;
import com.yammer.metrics.reporting.AbstractPollingReporter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.*;

public abstract class AbstractPollingReporterTest {

    protected final Clock clock = mock(Clock.class);
    private AbstractPollingReporter reporter;
    private ByteArrayOutputStream out;
    private TestMetricsRegistry registry;

    @Before
    public void init() throws Exception {
        when(clock.tick()).thenReturn(1234L);
        when(clock.time()).thenReturn(5678L);
        registry = new TestMetricsRegistry();
        out = new ByteArrayOutputStream();
        reporter = createReporter(registry, out, clock);
    }

    private static class TestMetricsRegistry extends MetricsRegistry {
        public <T extends Metric> T add(MetricName name, T metric) {
            return getOrAdd(name, metric);
        }
    }

    protected final <T extends Metric> void assertReporterOutput(Callable<T> action, String... expected) throws Exception {
        // Invoke the callable to trigger (ie, mark()/inc()/etc) and return the metric
        final T metric = action.call();
        try {
            // Add the metric to the registry, run the reporter and flush the result
            registry.add(new MetricName(Object.class, "metric"), metric);
            reporter.run();
            out.flush();
            final String[] lines = out.toString().split("\n");
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
                new Callable<CounterMetric>() {
                    @Override
                    public CounterMetric call() throws Exception {
                        return createCounter(count);
                    }
                },
                expectedCounterResult(count));
    }

    @Test
    public final void histogram() throws Exception {
        assertReporterOutput(
                new Callable<HistogramMetric>() {
                    @Override
                    public HistogramMetric call() throws Exception {
                        return createHistogram();
                    }
                },
                expectedHistogramResult());
    }

    @Test
    public final void meter() throws Exception {
        assertReporterOutput(
                new Callable<MeterMetric>() {
                    @Override
                    public MeterMetric call() throws Exception {
                        return createMeter();
                    }
                },
                expectedMeterResult());
    }

    @Test
    public final void timer() throws Exception {
        assertReporterOutput(
                new Callable<TimerMetric>() {
                    @Override
                    public TimerMetric call() throws Exception {
                        return createTimer();
                    }
                },
                expectedTimerResult());
    }

    @Test
    public final void gauge() throws Exception {
        final String value = "gaugeValue";
        assertReporterOutput(
                new Callable<GaugeMetric<String>>() {
                    @Override
                    public GaugeMetric<String> call() throws Exception {
                        return createGauge();
                    }
                },
                expectedGaugeResult(value));
    }

    @SuppressWarnings("unchecked")
    static CounterMetric createCounter(long count) throws Exception {
        final CounterMetric mock = mock(CounterMetric.class);
        when(mock.count()).thenReturn(count);
        return configureMatcher(mock, doAnswer(new MetricsProcessorAction() {
            @Override
            void delegateToProcessor(MetricsProcessor processor, MetricName name, Object context) throws Exception {
                processor.processCounter(name, mock, context);
            }
        }));
    }

    @SuppressWarnings("unchecked")
    static HistogramMetric createHistogram() throws Exception {
        final HistogramMetric mock = mock(HistogramMetric.class);
        setupSummarizedMock(mock);
        setupPercentiledMock(mock);
        return configureMatcher(mock, doAnswer(new MetricsProcessorAction() {
            @Override
            void delegateToProcessor(MetricsProcessor processor, MetricName name, Object context) throws Exception {
                processor.processHistogram(name, mock, context);
            }
        }));
    }

    @SuppressWarnings("unchecked")
    static GaugeMetric<String> createGauge() throws Exception {
        final GaugeMetric<String> mock = mock(GaugeMetric.class);
        when(mock.value()).thenReturn("gaugeValue");
        return configureMatcher(mock, doAnswer(new MetricsProcessorAction() {
            @Override
            void delegateToProcessor(MetricsProcessor processor, MetricName name, Object context) throws Exception {
                processor.processGauge(name, mock, context);
            }
        }));
    }

    @SuppressWarnings("unchecked")
    static TimerMetric createTimer() throws Exception {
        final TimerMetric mock = mock(TimerMetric.class);
        when(mock.durationUnit()).thenReturn(TimeUnit.MILLISECONDS);
        setupSummarizedMock(mock);
        setupMeteredMock(mock);
        setupPercentiledMock(mock);
        return configureMatcher(mock, doAnswer(new MetricsProcessorAction() {
            @Override
            void delegateToProcessor(MetricsProcessor processor, MetricName name, Object context) throws Exception {
                processor.processTimer(name, mock, context);
            }
        }));
    }

    @SuppressWarnings("unchecked")
    static MeterMetric createMeter() throws Exception {
        final MeterMetric mock = mock(MeterMetric.class);
        setupMeteredMock(mock);
        return configureMatcher(mock, doAnswer(new MetricsProcessorAction() {
            @Override
            void delegateToProcessor(MetricsProcessor processor, MetricName name, Object context) throws Exception {
                processor.processMeter(name, mock, context);
            }
        }));
    }

    @SuppressWarnings("unchecked")
    static <T extends Metric> T configureMatcher(T mock, Stubber stub) throws Exception {
        stub.when(mock).processWith(any(MetricsProcessor.class), any(MetricName.class), any());
        return mock;
    }

    static abstract class MetricsProcessorAction implements Answer<Object> {
        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            final MetricsProcessor<?> processor = (MetricsProcessor<?>) invocation.getArguments()[0];
            final MetricName name = (MetricName) invocation.getArguments()[1];
            final Object context = invocation.getArguments()[2];
            delegateToProcessor(processor, name, context);
            return null;
        }

        abstract void delegateToProcessor(MetricsProcessor<?> processor, MetricName name, Object context) throws Exception;
    }

    static void setupSummarizedMock(Summarized summarized) {
        when(summarized.min()).thenReturn(1d);
        when(summarized.max()).thenReturn(3d);
        when(summarized.mean()).thenReturn(2d);
        when(summarized.stdDev()).thenReturn(1.5d);
    }

    static void setupMeteredMock(Metered metered) {
        when(metered.count()).thenReturn(1L);
        when(metered.oneMinuteRate()).thenReturn(1d);
        when(metered.fiveMinuteRate()).thenReturn(5d);
        when(metered.fifteenMinuteRate()).thenReturn(15d);
        when(metered.meanRate()).thenReturn(2d);
        when(metered.eventType()).thenReturn("eventType");
        when(metered.rateUnit()).thenReturn(TimeUnit.SECONDS);
    }

    static void setupPercentiledMock(Percentiled percentiled) {
        when(percentiled.percentile(anyDouble())).thenAnswer(new Answer<Double>() {
            @Override
            public Double answer(InvocationOnMock invocation) throws Throwable {
                return (Double) invocation.getArguments()[0];
            }
        });
        doAnswer(new Answer<Double[]>() {
            @Override
            public Double[] answer(InvocationOnMock invocation) throws Throwable {
                final Object[] arguments = invocation.getArguments();
                return Arrays.copyOf(arguments, arguments.length, Double[].class);
            }
        }).when(percentiled).percentiles(Mockito.<Double>anyVararg());
    }

    public abstract String[] expectedGaugeResult(String value);

    public abstract String[] expectedTimerResult();

    public abstract String[] expectedMeterResult();

    public abstract String[] expectedHistogramResult();

    public abstract String[] expectedCounterResult(long count);

}
