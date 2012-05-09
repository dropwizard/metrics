package com.yammer.metrics.reporting;

import com.yammer.metrics.core.*;
import com.yammer.metrics.stats.Snapshot;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;

import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class StatsdReporterTest {

    protected final Clock clock = mock(Clock.class);
    protected AbstractPollingReporter reporter;
    protected TestMetricsRegistry registry;
    protected DatagramPacket packet;

    protected static class TestMetricsRegistry extends MetricsRegistry {
        public <T extends Metric> T add(MetricName name, T metric) {
            return getOrAdd(name, metric);
        }
    }

    @Before
    public void init() throws Exception {
        when(clock.getTick()).thenReturn(1234L);
        when(clock.getTime()).thenReturn(5678L);
        registry = new TestMetricsRegistry();
        byte[] data = new byte[65536];
        packet = new DatagramPacket(data, data.length);
        reporter = createReporter(registry, clock);
    }

    protected AbstractPollingReporter createReporter(MetricsRegistry registry, Clock clock) throws Exception {
        final DatagramSocket socket = mock(DatagramSocket.class);
        final StatsdReporter.UDPSocketProvider provider = mock(StatsdReporter.UDPSocketProvider.class);
        when(provider.get()).thenReturn(socket);
        when(provider.newPacket(any(ByteArrayOutputStream.class))).thenReturn(packet);

        final StatsdReporter reporter = new StatsdReporter(registry,
                                                           "prefix",
                                                           MetricPredicate.ALL,
                                                           provider,
                                                           clock);
        reporter.setPrintVMMetrics(false);
        return reporter;
    }

    protected <T extends Metric> void assertReporterOutput(Callable<T> action, String... expected) throws Exception {
        // Invoke the callable to trigger (ie, mark()/inc()/etc) and return the metric
        final T metric = action.call();
        try {
            // Add the metric to the registry, run the reporter and flush the result
            registry.add(new MetricName(Object.class, "metric"), metric);
            reporter.run();

            String packetData = new String(packet.getData());
            final String[] lines = packetData.split("\r?\n|\r");
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

    public String[] expectedGaugeResult(String value) {
        return new String[]{String.format("prefix.java.lang.Object.metric.count:%s|g", value)};
    }

    public String[] expectedTimerResult() {
        return new String[]{
                "prefix.java.lang.Object.metric.count:1|g",
                "prefix.java.lang.Object.metric.meanRate:2.00|ms",
                "prefix.java.lang.Object.metric.1MinuteRate:1.00|ms",
                "prefix.java.lang.Object.metric.5MinuteRate:5.00|ms",
                "prefix.java.lang.Object.metric.15MinuteRate:15.00|ms",
                "prefix.java.lang.Object.metric.min:1.00|ms",
                "prefix.java.lang.Object.metric.max:3.00|ms",
                "prefix.java.lang.Object.metric.mean:2.00|ms",
                "prefix.java.lang.Object.metric.stddev:1.50|ms",
                "prefix.java.lang.Object.metric.median:0.50|ms",
                "prefix.java.lang.Object.metric.75percentile:0.75|ms",
                "prefix.java.lang.Object.metric.95percentile:0.95|ms",
                "prefix.java.lang.Object.metric.98percentile:0.98|ms",
                "prefix.java.lang.Object.metric.99percentile:0.99|ms",
                "prefix.java.lang.Object.metric.999percentile:1.00|ms"
        };
    }

    public String[] expectedMeterResult() {
        return new String[]{
                "prefix.java.lang.Object.metric.count:1|g",
                "prefix.java.lang.Object.metric.meanRate:2.00|ms",
                "prefix.java.lang.Object.metric.1MinuteRate:1.00|ms",
                "prefix.java.lang.Object.metric.5MinuteRate:5.00|ms",
                "prefix.java.lang.Object.metric.15MinuteRate:15.00|ms",
        };
    }

    public String[] expectedHistogramResult() {
        return new String[]{
                "prefix.java.lang.Object.metric.min:1.00|ms",
                "prefix.java.lang.Object.metric.max:3.00|ms",
                "prefix.java.lang.Object.metric.mean:2.00|ms",
                "prefix.java.lang.Object.metric.stddev:1.50|ms",
                "prefix.java.lang.Object.metric.median:0.50|ms",
                "prefix.java.lang.Object.metric.75percentile:0.75|ms",
                "prefix.java.lang.Object.metric.95percentile:0.95|ms",
                "prefix.java.lang.Object.metric.98percentile:0.98|ms",
                "prefix.java.lang.Object.metric.99percentile:0.99|ms",
                "prefix.java.lang.Object.metric.999percentile:1.00|ms"
        };
    }

    public String[] expectedCounterResult(long count) {
        return new String[]{
                String.format("prefix.java.lang.Object.metric.count:%d|g", count)
        };
    }

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
        return configureMatcher(mock, doAnswer(new MetricsProcessorAction() {
            @Override
            void delegateToProcessor(MetricProcessor<Object> processor, MetricName name, Object context) throws Exception {
                processor.processCounter(name, mock, context);
            }
        }));
    }

    static Histogram createHistogram() throws Exception {
        final Histogram mock = mock(Histogram.class);
        setupSummarizableMock(mock);
        setupSamplingMock(mock);
        return configureMatcher(mock, doAnswer(new MetricsProcessorAction() {
            @Override
            void delegateToProcessor(MetricProcessor<Object> processor, MetricName name, Object context) throws Exception {
                processor.processHistogram(name, mock, context);
            }
        }));
    }


    static Gauge<String> createGauge() throws Exception {
        @SuppressWarnings("unchecked")
        final Gauge<String> mock = mock(Gauge.class);
        when(mock.getValue()).thenReturn("gaugeValue");
        return configureMatcher(mock, doAnswer(new MetricsProcessorAction() {
            @Override
            void delegateToProcessor(MetricProcessor<Object> processor, MetricName name, Object context) throws Exception {
                processor.processGauge(name, mock, context);
            }
        }));
    }


    static Timer createTimer() throws Exception {
        final Timer mock = mock(Timer.class);
        when(mock.getDurationUnit()).thenReturn(TimeUnit.MILLISECONDS);
        setupSummarizableMock(mock);
        setupMeteredMock(mock);
        setupSamplingMock(mock);
        return configureMatcher(mock, doAnswer(new MetricsProcessorAction() {
            @Override
            void delegateToProcessor(MetricProcessor<Object> processor, MetricName name, Object context) throws Exception {
                processor.processTimer(name, mock, context);
            }
        }));
    }

    static Meter createMeter() throws Exception {
        final Meter mock = mock(Meter.class);
        setupMeteredMock(mock);
        return configureMatcher(mock, doAnswer(new MetricsProcessorAction() {
            @Override
            void delegateToProcessor(MetricProcessor<Object> processor, MetricName name, Object context) throws Exception {
                processor.processMeter(name, mock, context);
            }
        }));
    }

    @SuppressWarnings("unchecked")
    static <T extends Metric> T configureMatcher(T mock, Stubber stub) throws Exception {
        stub.when(mock).processWith(any(MetricProcessor.class), any(MetricName.class), any());
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
    }}
