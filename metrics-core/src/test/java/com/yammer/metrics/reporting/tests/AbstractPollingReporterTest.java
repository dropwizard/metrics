package com.yammer.metrics.reporting.tests;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.CounterMetric;
import com.yammer.metrics.core.GaugeMetric;
import com.yammer.metrics.core.HistogramMetric;
import com.yammer.metrics.core.HistogramMetric.SampleType;
import com.yammer.metrics.core.MeterMetric;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Stoppable;
import com.yammer.metrics.core.TimerMetric;
import com.yammer.metrics.reporting.AbstractPollingReporter;

public abstract class AbstractPollingReporterTest {

    protected final Clock clock = new Clock() {
        @Override
        public long tick() {
            return 1234;
        }

        @Override
        public long time() {
            return 5678;
        };
    };
    private AbstractPollingReporter reporter;
    private ByteArrayOutputStream out;
    private TestMetricsRegistry internalRegistry;
    protected MetricsRegistry registry;

    @Before
    public void init() throws Exception {
        registry = internalRegistry = new TestMetricsRegistry();
        out = new ByteArrayOutputStream();
        reporter = createReporter(internalRegistry, out, clock);
    }

    private static class TestMetricsRegistry extends MetricsRegistry {
        public <T extends Metric> T add(MetricName name, T metric) {
            return getOrAdd(name, metric);
        }
    }

    protected final <T extends Metric> void assertReporterOutput(Callable<T> action, String... expected) throws Exception {
        // Invoke the callable to trigger (ie, mark()/inc()/etc) and return the metric
        final T metric = action.call();
        try{
            // Add the metric to the registry, run the reporter and flush the result
            internalRegistry.add(new MetricName(getClass(), metric.getClass().getSimpleName()), metric);
            reporter.run();
            out.flush();
            final String[] lines = out.toString().split("\n");
            // Assertions: first check that the line count matches then compare line by line ignoring leading and trailing whitespace
            assertEquals("Line count mismatch, was:\n" + Arrays.toString(lines), expected.length, lines.length);
            for(int i = 0; i < lines.length; i++){
                assertEquals(expected[i].trim(), lines[i].trim());
            }
        }
        finally{
            if(metric instanceof Stoppable){
                ((Stoppable)metric).stop();
            }
        }
    }

    protected abstract AbstractPollingReporter createReporter(MetricsRegistry registry, OutputStream out, Clock clock) throws Exception;

    @Test
    public final void counter() throws Exception {
        final int count = new Random().nextInt(Integer.MAX_VALUE);
        assertReporterOutput(
                new Callable<CounterMetric>() {
                    @Override
                    public CounterMetric call() throws Exception {
                        final CounterMetric metric = new CounterMetric();
                        metric.inc(count);
                        return metric;
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
                        final HistogramMetric histogram = new HistogramMetric(SampleType.UNIFORM);
                        histogram.update(1);
                        return histogram;
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
                        final MeterMetric meter = new MeterMetric(registry.newMeterTickThreadPool(), "mock", TimeUnit.MILLISECONDS, clock);
                        meter.mark();
                        return meter;
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
                        // Not doing anything here since untill we have a good way of how to test Timer with fixed result values
                        return new TimerMetric(registry.newMeterTickThreadPool(), TimeUnit.MILLISECONDS, TimeUnit.SECONDS, clock);
                    }
                },
                expectedTimerResult());
    }

    @Test
    public final void gauge() throws Exception {
        assertReporterOutput(
                new Callable<GaugeMetric<String>>() {
                    @Override
                    public GaugeMetric<String> call() throws Exception {
                        return new GaugeMetric<String>() {
                            @Override
                            public String value() {
                                return GaugeMetric.class.getSimpleName();
                            }
                        };
                    }
                },
                expectedGaugeResult());
    }

    public abstract String[] expectedGaugeResult();

    public abstract String[] expectedTimerResult();

    public abstract String[] expectedMeterResult();

    public abstract String[] expectedHistogramResult();

    public abstract String[] expectedCounterResult(int count);

}