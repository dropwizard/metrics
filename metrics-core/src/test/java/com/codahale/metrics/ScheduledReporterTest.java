package com.codahale.metrics;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

public class ScheduledReporterTest {
    private final Gauge gauge = mock(Gauge.class);
    private final Counter counter = mock(Counter.class);
    private final Histogram histogram = mock(Histogram.class);
    private final Meter meter = mock(Meter.class);
    private final Timer timer = mock(Timer.class);

    private final MetricRegistry registry = new MetricRegistry();
    private final ScheduledReporter reporter = spy(
            new ScheduledReporter(registry,
                                  "example",
                                  MetricFilter.ALL,
                                  TimeUnit.SECONDS,
                                  TimeUnit.MILLISECONDS) {
                @Override
                public void report(SortedMap<String, Gauge> gauges,
                                   SortedMap<String, Counter> counters,
                                   SortedMap<String, Histogram> histograms,
                                   SortedMap<String, Meter> meters,
                                   SortedMap<String, Timer> timers) {
                    // nothing doing!
                }
            }
    );

    @Before
    public void setUp() throws Exception {
        registry.register("gauge", gauge);
        registry.register("counter", counter);
        registry.register("histogram", histogram);
        registry.register("meter", meter);
        registry.register("timer", timer);

        reporter.start(200, TimeUnit.MILLISECONDS);
    }

    @After
    public void tearDown() throws Exception {
        reporter.stop();
    }

    @Test
    public void pollsPeriodically() throws Exception {
        verify(reporter, timeout(500).times(2)).report(
                map("gauge", gauge),
                map("counter", counter),
                map("histogram", histogram),
                map("meter", meter),
                map("timer", timer)
        );
    }

    private <T> SortedMap<String, T> map(String name, T value) {
        final SortedMap<String, T> map = new TreeMap<String, T>();
        map.put(name, value);
        return map;
    }
}
